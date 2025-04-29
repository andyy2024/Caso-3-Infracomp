import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import cliente.Cliente;
import servidor.Servidor;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        clearData();

        // escoger que escenario correr:
        
        //escenario1();
        //escenario2();
        estimarVelocidadProcesador();
    }

    private static void escenario2() throws InterruptedException {
        // ---------------------------------------------------------------
        // Servidor y clientes concurrentes.
        // ---------------------------------------------------------------

        int conexiones = 64; // conexiones cliente-servidor
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
            bw.write("accion,tiempo_ns");
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    public static void estimarVelocidadProcesador() {
        long operaciones = 1_000_000_000L; // Mil millones de operaciones
        long contador = 0;
    
        long start = System.nanoTime();
    
        for (long i = 0; i < operaciones; i++) {
            contador += i;
        }
    
        long end = System.nanoTime();
        long tiempoTotalNs = end - start;
        double tiempoTotalSegundos = tiempoTotalNs / 1e9;
    
        double operacionesPorSegundo = operaciones / tiempoTotalSegundos;
        double velocidadGHz = operacionesPorSegundo / 1e9;
    
        System.out.println("Tiempo total (s): " + tiempoTotalSegundos);
        System.out.println("Operaciones por segundo: " + operacionesPorSegundo);
        System.out.println("Velocidad aproximada de CPU (GHz): " + velocidadGHz);
    }
    
}
