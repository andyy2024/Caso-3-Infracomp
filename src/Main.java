import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import cliente.Cliente;
import servidor.Servidor;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        clearData();
        
        // escoger que escenario correro:
        // escenario1();
        // escenario2();
    }

    private static void escenario2() throws InterruptedException {
        // ---------------------------------------------------------------
        // Servidor y clientes concurrentes.
        // ---------------------------------------------------------------

        int conexiones = 4; // conexiones cliente-servidor
        int solicitudes = 1; // solicitudes por cliente
        
        Servidor servidor = new Servidor(5000, conexiones, solicitudes);
        Cliente cliente = new Cliente("localhost", 5000, conexiones, solicitudes);

        cliente.start();
        servidor.start();

        cliente.join();
        servidor.join();
    }

    private static void escenario1() throws InterruptedException {
        // ---------------------------------------------------------------
        // Un servidor de consulta y un cliente iterativo
        // ---------------------------------------------------------------

        int conexiones = 1; // conexiones cliente-servidor
        int solicitudes = 32; // solicitudes por cliente
        
        Servidor servidor = new Servidor(5000, conexiones, solicitudes);
        Cliente cliente = new Cliente("localhost", 5000, conexiones, solicitudes);

        cliente.start();
        servidor.start();

        cliente.join();
        servidor.join();
    }

    public static void clearData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("output/data.txt", false))) {
            bw.write("accion,tiempo");
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }
}
