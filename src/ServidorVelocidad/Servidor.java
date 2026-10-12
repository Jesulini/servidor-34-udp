package servidor;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Servidor extends JFrame {

    private JTextField txtHost;
    private JTextField txtPuerto;

    private JButton btnServidor;

    private JLabel lblEstado;

    private JTextArea txtRegistro;

    private DatagramSocket servidor;

    private Thread hiloServidor;

    private volatile boolean servidorActivo = false;

    public Servidor() {

        setTitle("Servidor UDP - Velocidad Promedio");

        setSize(700, 550);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        JPanel panelPrincipal =
                new JPanel(new BorderLayout(10, 10));

        panelPrincipal.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        JLabel titulo =
                new JLabel(
                        "SERVIDOR UDP - VELOCIDAD PROMEDIO",
                        SwingConstants.CENTER
                );

        titulo.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        panelPrincipal.add(
                titulo,
                BorderLayout.NORTH
        );

        JPanel panelConfiguracion =
                new JPanel(
                        new GridLayout(4, 2, 10, 10)
                );

        panelConfiguracion.setBorder(
                BorderFactory.createTitledBorder(
                        "Configuración del servidor"
                )
        );

        panelConfiguracion.add(
                new JLabel("Host / IP:")
        );

        txtHost =
                new JTextField("localhost");

        panelConfiguracion.add(
                txtHost
        );

        panelConfiguracion.add(
                new JLabel("Puerto UDP:")
        );

        txtPuerto =
                new JTextField("5000");

        panelConfiguracion.add(
                txtPuerto
        );

        panelConfiguracion.add(
                new JLabel("Estado:")
        );

        lblEstado =
                new JLabel("● APAGADO");

        lblEstado.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        panelConfiguracion.add(
                lblEstado
        );

        panelConfiguracion.add(
                new JLabel()
        );

        btnServidor =
                new JButton(
                        "ENCENDER SERVIDOR"
                );

        panelConfiguracion.add(
                btnServidor
        );

        panelPrincipal.add(
                panelConfiguracion,
                BorderLayout.CENTER
        );

        txtRegistro =
                new JTextArea();

        txtRegistro.setEditable(false);

        txtRegistro.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        13
                )
        );

        txtRegistro.setLineWrap(true);

        txtRegistro.setWrapStyleWord(true);

        JScrollPane scroll =
                new JScrollPane(
                        txtRegistro
                );

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Registro del servidor"
                )
        );

        panelPrincipal.add(
                scroll,
                BorderLayout.SOUTH
        );

        add(panelPrincipal);

        btnServidor.addActionListener(
                e -> cambiarEstadoServidor()
        );

        agregarRegistro(
                "=========================================="
        );

        agregarRegistro(
                "SERVIDOR UDP"
        );

        agregarRegistro(
                "Servidor actualmente APAGADO."
        );

        agregarRegistro(
                "Configure el Host/IP y el puerto."
        );

        agregarRegistro(
                "Luego presione ENCENDER SERVIDOR."
        );

        agregarRegistro(
                "=========================================="
        );
    }

    // 
    // ENCENDER / APAGAR SERVIDOR
    // 

    private void cambiarEstadoServidor() {

        if (!servidorActivo) {

            iniciarServidor();

        } else {

            detenerServidor();
        }
    }

    private void iniciarServidor() {

        String host =
                txtHost.getText().trim();

        String puertoTexto =
                txtPuerto.getText().trim();

        if (host.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe ingresar un Host o una dirección IP.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        int puerto;

        try {

            puerto =
                    Integer.parseInt(
                            puertoTexto
                    );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "El puerto debe ser un número.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (puerto < 1 || puerto > 65535) {

            JOptionPane.showMessageDialog(
                    this,
                    "El puerto debe estar entre 1 y 65535.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        try {

            /*
             * ======================================
             * CREAR SOCKET UDP
             * ======================================
             *
             * Si se utiliza localhost:
             *
             * localhost
             * 127.0.0.1
             *
             * se refiere al mismo computador.
             */

            InetAddress direccion =
                    InetAddress.getByName(host);

            servidor =
                    new DatagramSocket(
                            puerto,
                            direccion
                    );

            servidorActivo = true;

            lblEstado.setText(
                    "● ACTIVO"
            );

            btnServidor.setText(
                    "APAGAR SERVIDOR"
            );

            txtHost.setEnabled(false);

            txtPuerto.setEnabled(false);

            agregarRegistro("");

            agregarRegistro(
                    "=========================================="
            );

            agregarRegistro(
                    "SERVIDOR UDP ENCENDIDO"
            );

            agregarRegistro(
                    "Host/IP: " + host
            );

            agregarRegistro(
                    "Puerto: " + puerto
            );

            agregarRegistro(
                    "Esperando clientes..."
            );

            agregarRegistro(
                    "=========================================="
            );



            hiloServidor =
                    new Thread(
                            this::escucharClientes
                    );

            hiloServidor.start();

        } catch (Exception e) {

            servidorActivo = false;

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible iniciar el servidor.\n\n"
                    + "Detalle: "
                    + e.getMessage(),
                    "Error del servidor",
                    JOptionPane.ERROR_MESSAGE
            );

            agregarRegistro(
                    "ERROR AL INICIAR: "
                    + e.getMessage()
            );
        }
    }

    // =
    // DETENER SERVIDOR
    // =

    private void detenerServidor() {

        servidorActivo = false;

        /*
         * Cerrar el socket provoca que el método
         * receive() deje de esperar.
         */

        if (servidor != null &&
                !servidor.isClosed()) {

            servidor.close();
        }

        lblEstado.setText(
                "● APAGADO"
        );

        btnServidor.setText(
                "ENCENDER SERVIDOR"
        );

        txtHost.setEnabled(true);

        txtPuerto.setEnabled(true);

        agregarRegistro("");

        agregarRegistro(
                "=========================================="
        );

        agregarRegistro(
                "SERVIDOR UDP APAGADO"
        );

        agregarRegistro(
                "El puerto fue liberado."
        );

        agregarRegistro(
                "=========================================="
        );
    }

    // =
    // ESCUCHAR CLIENTES
    // =

    private void escucharClientes() {

        while (servidorActivo) {

            try {

                byte[] buffer =
                        new byte[1024];

                DatagramPacket paqueteRecibido =
                        new DatagramPacket(
                                buffer,
                                buffer.length
                        );

                servidor.receive(
                        paqueteRecibido
                );

                if (!servidorActivo) {
                    break;
                }

                String direccionCliente =
                        paqueteRecibido
                                .getAddress()
                                .getHostAddress();

                int puertoCliente =
                        paqueteRecibido.getPort();

                String mensaje =
                        new String(
                                paqueteRecibido.getData(),
                                0,
                                paqueteRecibido.getLength(),
                                StandardCharsets.UTF_8
                        );

                agregarRegistro("");

                agregarRegistro(
                        "------------------------------------------"
                );

                agregarRegistro(
                        "SOLICITUD RECIBIDA"
                );

                agregarRegistro(
                        "Cliente: "
                        + direccionCliente
                        + ":"
                        + puertoCliente
                );

                agregarRegistro(
                        "Datos: "
                        + mensaje
                );

                procesarSolicitud(
                        mensaje,
                        paqueteRecibido
                );

            } catch (Exception e) {

                /*
                 * Cuando apagamos el servidor,
                 * DatagramSocket se cierra y receive()
                 * genera una excepción.
                 *
                 * Si el servidor fue apagado
                 * intencionalmente, no mostramos
                 * el error.
                 */

                if (servidorActivo) {

                    agregarRegistro(
                            "Error al recibir datos: "
                            + e.getMessage()
                    );
                }
            }
        }
    }

    private void procesarSolicitud(
            String mensaje,
            DatagramPacket paqueteRecibido) {

        String respuesta;

        /*
         * Formato:
         *
         * distancia|tiempo|unidadDistancia|unidadTiempo
         */

        String[] datos =
                mensaje.split("\\|");

        if (datos.length != 4) {

            respuesta =
                    "false|Error: formato de datos incorrecto.";

        } else {

            try {

                double distancia =
                        Double.parseDouble(
                                datos[0]
                        );

                double tiempo =
                        Double.parseDouble(
                                datos[1]
                        );

                String unidadDistancia =
                        datos[2];

                String unidadTiempo =
                        datos[3];

                agregarRegistro(
                        "Distancia: "
                        + distancia
                        + " "
                        + unidadDistancia
                );

                agregarRegistro(
                        "Tiempo: "
                        + tiempo
                        + " "
                        + unidadTiempo
                );

                if (distancia <= 0 ||
                        tiempo <= 0) {

                    respuesta =
                            "false|Error: la distancia y el tiempo deben ser mayores que cero.";

                    agregarRegistro(
                            "Error: valores menores o iguales a cero."
                    );

                } else {

                    double velocidadPromedio =
                            distancia / tiempo;

                    String unidadVelocidad =
                            unidadDistancia
                            + "/"
                            + unidadTiempo;

                    String resultado =
                            String.format(
                                    "La velocidad promedio es: %.2f %s",
                                    velocidadPromedio,
                                    unidadVelocidad
                            );

                    agregarRegistro(
                            "Cálculo:"
                    );

                    agregarRegistro(
                            distancia
                            + " / "
                            + tiempo
                            + " = "
                            + String.format(
                                    "%.2f",
                                    velocidadPromedio
                            )
                    );

                    agregarRegistro(
                            "Resultado: "
                            + resultado
                    );

                    respuesta =
                            "true|" + resultado;
                }

            } catch (NumberFormatException e) {

                respuesta =
                        "false|Error: los valores recibidos no son numéricos.";

                agregarRegistro(
                        "Error: valores no numéricos."
                );
            }
        }

        try {

            byte[] datosRespuesta =
                    respuesta.getBytes(
                            StandardCharsets.UTF_8
                    );

            DatagramPacket paqueteRespuesta =
                    new DatagramPacket(
                            datosRespuesta,
                            datosRespuesta.length,
                            paqueteRecibido.getAddress(),
                            paqueteRecibido.getPort()
                    );

            servidor.send(
                    paqueteRespuesta
            );

            agregarRegistro(
                    "Respuesta enviada:"
            );

            agregarRegistro(
                    respuesta
            );

            agregarRegistro(
                    "Esperando siguiente solicitud..."
            );

        } catch (Exception e) {

            if (servidorActivo) {

                agregarRegistro(
                        "Error al enviar respuesta: "
                        + e.getMessage()
                );
            }
        }
    }

    private void agregarRegistro(
            String mensaje) {

        SwingUtilities.invokeLater(() -> {

            txtRegistro.append(
                    mensaje + "\n"
            );

            txtRegistro.setCaretPosition(
                    txtRegistro
                            .getDocument()
                            .getLength()
            );
        });
    }

    @Override
    public void dispose() {

        /*
         * Si se cierra la ventana mientras el
         * servidor está encendido, primero
         * cerramos el socket.
         */

        if (servidorActivo) {

            detenerServidor();
        }

        super.dispose();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Servidor ventana =
                    new Servidor();

            ventana.setVisible(true);
        });
    }
}