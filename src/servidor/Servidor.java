package servidor;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor extends Thread {

    private int puerto;
    private ExecutorService poolDeHilos; // Para gestionar los delegados
    private int conexiones;
    private int solicitudes;

    public Servidor(int puerto, int conexiones, int solicitudes) {
        this.puerto = puerto;
        this.poolDeHilos = Executors.newFixedThreadPool(conexiones);
        this.conexiones = conexiones;
        this.solicitudes = solicitudes;
        System.out.println("Servidor iniciado en el puerto: " + puerto);

    }

    @Override
    public void run() {
        try (ServerSocket socketServidor = new ServerSocket(puerto)) {
            int clientes = 0;
            while (true) {
                System.out.println("Esperando nuevas conexiones...");
                Socket socketCliente = socketServidor.accept();
                System.out.println("Nueva conexión aceptada desde: " + socketCliente.getInetAddress().getHostAddress());

                // Crear y ejecutar el delegado para manejar esta conexión
                Runnable delegado = new DelegadoServidor(socketCliente, solicitudes);
                poolDeHilos.execute(delegado);

                clientes++;
                if (conexiones == clientes ){
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        } finally {
            if (poolDeHilos != null) {
                poolDeHilos.shutdown(); // Cierra el pool de hilos al detener el servidor
            }
        }
    }
}