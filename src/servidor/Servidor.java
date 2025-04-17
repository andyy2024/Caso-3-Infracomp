package servidor;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    private int puerto;
    private ExecutorService poolDeHilos; // Para gestionar los delegados

    public Servidor(int puerto, int numeroMaximoConexiones) {
        this.puerto = puerto;
        this.poolDeHilos = Executors.newFixedThreadPool(numeroMaximoConexiones);
        System.out.println("Servidor iniciado en el puerto: " + puerto);

    }

    public void iniciar() {
        try (ServerSocket socketServidor = new ServerSocket(puerto)) {
            while (true) {
                System.out.println("Esperando nuevas conexiones...");
                Socket socketCliente = socketServidor.accept();
                System.out.println("Nueva conexión aceptada desde: " + socketCliente.getInetAddress().getHostAddress());

                // Crear y ejecutar el delegado para manejar esta conexión
                Runnable delegado = new DelegadoServidor(socketCliente);
                poolDeHilos.execute(delegado);
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        } finally {
            if (poolDeHilos != null) {
                poolDeHilos.shutdown(); // Cierra el pool de hilos al detener el servidor
            }
        }
    }


    public static void main(String[] args) throws IOException {
        int puertoServidor = 12345;
        int maxConexiones = 10; // Define un límite para el número de conexiones concurrentes
        Servidor servidor = new Servidor(puertoServidor, maxConexiones);
        servidor.iniciar();
    }
}