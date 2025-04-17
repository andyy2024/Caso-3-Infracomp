package cliente;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cliente {

    private String host;
    private int puerto;
    private int numeroDeConexiones;
    private ExecutorService poolDeHilos;
    private List<DelegadoCliente> delegados;

    public Cliente(String host, int puerto, int numeroDeConexiones) {
        this.host = host;
        this.puerto = puerto;
        this.numeroDeConexiones = numeroDeConexiones;
        this.poolDeHilos = Executors.newFixedThreadPool(numeroDeConexiones);
        this.delegados = new ArrayList<>();
    }

    public void iniciarConexiones() {
        System.out.println("Iniciando " + numeroDeConexiones + " conexiones al servidor en " + host + ":" + puerto);
        for (int i = 0; i < numeroDeConexiones; i++) {
            try {
                Socket socketCliente = new Socket(host, puerto);
                DelegadoCliente delegado = new DelegadoCliente(socketCliente, "Cliente-" + (i + 1));
                delegados.add(delegado);
                poolDeHilos.execute(delegado);
            } catch (IOException e) {
                System.err.println("Error al crear la conexión " + (i + 1) + ": " + e.getMessage());
            }
        }
        poolDeHilos.shutdown(); // No se aceptarán más tareas
    }

    public static void main(String[] args) {
        String hostServidor = "localhost";
        int puertoServidor = 12345;
        int numConexiones = 3; // Número de delegados/conexiones a crear

        Cliente cliente = new Cliente(hostServidor, puertoServidor, numConexiones);
        cliente.iniciarConexiones();
    }
}