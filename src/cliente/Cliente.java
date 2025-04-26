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

public class Cliente extends Thread {

    private String host;
    private int puerto;
    private int conexiones;
    private int solicitudes;
    private ExecutorService poolDeHilos;
    private List<DelegadoCliente> delegados;

    public Cliente(String host, int puerto, int conexiones, int solicitudes) {
        this.host = host;
        this.puerto = puerto;
        this.conexiones = conexiones;
        this.solicitudes = solicitudes;
        this.poolDeHilos = Executors.newFixedThreadPool(conexiones);
        this.delegados = new ArrayList<>();
    }

    @Override
    public void run() {

        System.out.println("Iniciando " + conexiones + " conexiones al servidor en " + host + ":" + puerto);
        for (int i = 0; i < conexiones; i++) {
            try {
                Socket socketCliente = new Socket(host, puerto);
                DelegadoCliente delegado = new DelegadoCliente(socketCliente, "Cliente-" + (i + 1), solicitudes);
                delegados.add(delegado);
                poolDeHilos.execute(delegado);

            } catch (IOException e) {
                System.err.println("Error al crear la conexión " + (i + 1) + ": " + e.getMessage());
            }
        }
        poolDeHilos.shutdown(); // No se aceptarán más tareas

    }
}