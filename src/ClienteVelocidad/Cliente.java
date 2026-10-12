package cliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Cliente extends JFrame {

    private JTextField txtDistancia;
    private JTextField txtTiempo;

    private JComboBox<String> cmbUnidadDistancia;
    private JComboBox<String> cmbUnidadTiempo;

    private JButton btnCalcular;

    private JTextArea txtResultado;

    private final String HOST = "localhost";
    private final int PUERTO = 5000;

    public Cliente() {

        setTitle("Calculadora de Velocidad Promedio - UDP");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        // Título
        JLabel titulo = new JLabel(
                "CÁLCULO DE VELOCIDAD PROMEDIO",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 18));

        panel.add(titulo, BorderLayout.NORTH);

        // Panel de formulario
        JPanel formulario = new JPanel();
        formulario.setLayout(new GridLayout(5, 2, 10, 10));

        formulario.setBorder(
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        );

        // Distancia
        formulario.add(new JLabel("Distancia recorrida:"));

        txtDistancia = new JTextField();
        formulario.add(txtDistancia);

        // Unidad distancia
        formulario.add(new JLabel("Unidad de distancia:"));

        cmbUnidadDistancia = new JComboBox<>(
                new String[]{"Kilómetros (km)", "Metros (m)"}
        );

        formulario.add(cmbUnidadDistancia);

        // Tiempo
        formulario.add(new JLabel("Tiempo empleado:"));

        txtTiempo = new JTextField();
        formulario.add(txtTiempo);

        // Unidad tiempo
        formulario.add(new JLabel("Unidad de tiempo:"));

        cmbUnidadTiempo = new JComboBox<>(
                new String[]{
                        "Horas (h)",
                        "Minutos (min)",
                        "Segundos (s)"
                }
        );

        formulario.add(cmbUnidadTiempo);

        // Botón
        btnCalcular = new JButton("CALCULAR");

        formulario.add(new JLabel());
        formulario.add(btnCalcular);

        panel.add(formulario, BorderLayout.CENTER);

        // Resultado
        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Arial", Font.BOLD, 15));
        txtResultado.setBorder(
                BorderFactory.createTitledBorder("Resultado")
        );

        panel.add(txtResultado, BorderLayout.SOUTH);

        add(panel);

        // Evento del botón
        btnCalcular.addActionListener(e -> enviarDatos());

        // Permite usar Enter
        getRootPane().setDefaultButton(btnCalcular);
    }

    private void enviarDatos() {

        String distanciaTexto = txtDistancia.getText().trim();
        String tiempoTexto = txtTiempo.getText().trim();

        // Validar campos vacíos
        if (distanciaTexto.isEmpty() || tiempoTexto.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe ingresar la distancia y el tiempo.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        double distancia;
        double tiempo;

        try {

            distancia = Double.parseDouble(distanciaTexto);
            tiempo = Double.parseDouble(tiempoTexto);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "La distancia y el tiempo deben ser valores numéricos.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Validar valores positivos
        if (distancia <= 0 || tiempo <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "La distancia y el tiempo deben ser mayores que cero.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Obtener unidades
        String unidadDistancia = obtenerUnidadDistancia();
        String unidadTiempo = obtenerUnidadTiempo();

        try (DatagramSocket socket = new DatagramSocket()) {

            /*
             * Construir mensaje que será enviado
             * al servidor.
             */
            String mensaje =
                    distancia + "|" +
                    tiempo + "|" +
                    unidadDistancia + "|" +
                    unidadTiempo;

            byte[] datos = mensaje.getBytes();

            /*
             * Obtener dirección del servidor.
             */
            InetAddress direccionServidor =
                    InetAddress.getByName(HOST);

            /*
             * Crear paquete UDP.
             */
            DatagramPacket paqueteEnvio =
                    new DatagramPacket(
                            datos,
                            datos.length,
                            direccionServidor,
                            PUERTO
                    );

            /*
             * Enviar paquete.
             */
            socket.send(paqueteEnvio);

            /*
             * Preparar espacio para recibir
             * la respuesta del servidor.
             */
            byte[] buffer = new byte[1024];

            DatagramPacket paqueteRespuesta =
                    new DatagramPacket(
                            buffer,
                            buffer.length
                    );

            /*
             * Esperar respuesta del servidor.
             */
            socket.receive(paqueteRespuesta);

            /*
             * Convertir respuesta a String.
             */
            String respuesta = new String(
                    paqueteRespuesta.getData(),
                    0,
                    paqueteRespuesta.getLength()
            );

            /*
             * El servidor enviará:
             *
             * true|mensaje
             *
             * o
             *
             * false|mensaje
             */
            String[] partes = respuesta.split("\\|", 2);

            boolean exito = Boolean.parseBoolean(partes[0]);
            String mensajeRespuesta = partes[1];

            if (exito) {

                txtResultado.setText(
                        "Resultado recibido del servidor UDP:\n\n"
                        + mensajeRespuesta
                );

            } else {

                txtResultado.setText(
                        "El servidor informó un error:\n\n"
                        + mensajeRespuesta
                );
            }

        } catch (SocketTimeoutException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "El servidor no respondió a tiempo.",
                    "Error UDP",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible comunicarse con el servidor UDP.\n\n"
                    + "Verifique que el servidor esté ejecutándose.\n"
                    + "Detalle: " + e.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String obtenerUnidadDistancia() {

        int seleccion = cmbUnidadDistancia.getSelectedIndex();

        if (seleccion == 0) {
            return "km";
        } else {
            return "m";
        }
    }

    private String obtenerUnidadTiempo() {

        int seleccion = cmbUnidadTiempo.getSelectedIndex();

        switch (seleccion) {

            case 0:
                return "h";

            case 1:
                return "min";

            case 2:
                return "s";

            default:
                return "";
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Cliente ventana = new Cliente();
            ventana.setVisible(true);

        });
    }
}