package servidor;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;

class DelegadoServidor implements Runnable {

    private Socket socketCliente;
    private PrivateKey k_w_private;
    private PublicKey k_w_public;

    public DelegadoServidor(Socket socketCliente) {
        this.socketCliente = socketCliente;
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
             PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)
            ) {

            System.out.println("Manejando conexión desde: " + socketCliente.getInetAddress().getHostAddress()
                    + " en el hilo: " + Thread.currentThread().getName());

            String mensajeParaCliente, respuestaDelCliente;

            while (true){

                //------------------------------------------------------------
                //0a. Leer llaves de archivo
                Key[] llaves = leerLLaves();
                k_w_private = (PrivateKey) llaves[0];
                k_w_public = (PublicKey) llaves[1];

                //------------------------------------------------------------
                // 1. "HELLO"
                respuestaDelCliente = entrada.readLine();
                if (respuestaDelCliente != "HELLO") {break;}

                //------------------------------------------------------------
                // 2b. Reto
                respuestaDelCliente = entrada.readLine();
                int Reto = Integer.parseInt(respuestaDelCliente);

                //------------------------------------------------------------
                // 3. Calcula Rta = C(K_w-, Reto)
                Integer Rta = C(k_w_private, Reto);

                //------------------------------------------------------------
                // 4. Rta
                mensajeParaCliente = Rta + "";
                salida.println(mensajeParaCliente);

                //------------------------------------------------------------
                // 6. "OK" | "ERROR"
                respuestaDelCliente = entrada.readLine();
                if (respuestaDelCliente == "ERROR"){break;}
                else if (respuestaDelCliente != "OK"){break;}

                //------------------------------------------------------------
                // 7. Genera G, P, G^x


                //------------------------------------------------------------
                // 8. G
                Integer G = 0;
                mensajeParaCliente = G + "";
                salida.println(mensajeParaCliente);

                //------------------------------------------------------------
                // P
                Integer P = 0;
                mensajeParaCliente = P + "";
                salida.println(mensajeParaCliente);

                //------------------------------------------------------------
                // G^x
                Integer G_x = 0;
                mensajeParaCliente = G_x + "";
                salida.println(mensajeParaCliente);

                //------------------------------------------------------------
                // F(K_w-, (G,P,G^x))

                //------------------------------------------------------------
                // 10. "OK" | "ERROR"

                //------------------------------------------------------------
                // 11. G^y

                //------------------------------------------------------------
                // 11b. Calcula (G^y)^x

                //------------------------------------------------------------
                // Genera llave simétrica para cifrar K_AB1

                //------------------------------------------------------------
                // Genera llave simétrica para MAC K_AB2

                //------------------------------------------------------------
                // 12b. IV
                respuestaDelCliente = entrada.readLine();
                byte[] IV = Base64.getDecoder().decode(respuestaDelCliente);
                
                //------------------------------------------------------------
                // 13. C(K_AB1, tabla_ids_servicios)

                //------------------------------------------------------------
                // HMAC(K_AB2, tabla_ids_servicios)

                //------------------------------------------------------------
                // 14. C(K_AB1, id_servicio+IP_cliente)

                //------------------------------------------------------------
                // HMAC(K_AB2, id_servicio+IP_cliente)

                //------------------------------------------------------------
                // 15. Verifica HMAC y responde

                //------------------------------------------------------------
                // 16. C(K_AB1, IP_servidor+puerto_servidor)

                //------------------------------------------------------------
                // HMAC(K_AB2, IP_servidor+puerto_servidor)

                //------------------------------------------------------------
                // 18. "OK" | "ERROR"

            }
            
            

        } catch (IOException e) {
            System.err.println("Error al manejar la conexión con " + socketCliente.getInetAddress().getHostAddress()
                    + ": " + e.getMessage());
        } finally {
            try {
                socketCliente.close();
                System.out.println("Conexión con " + socketCliente.getInetAddress().getHostAddress() + " cerrada.");
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión con " + socketCliente.getInetAddress().getHostAddress()
                        + ": " + e.getMessage());
            }
        }
    }

    private Integer C(PrivateKey llave, int m) {
        byte[] m_Cifrado;

        try {
            Cipher cifrador = Cipher.getInstance("RSA");

            // convertir el numero m en un arreglo de bytes[]
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            byte[] textoClaro = buffer.putInt(m).array();

            cifrador.init(Cipher.ENCRYPT_MODE, llave);
            m_Cifrado = cifrador.doFinal(textoClaro);

            // convertir los bytes[] cifrados a un numero
            buffer = ByteBuffer.wrap(m_Cifrado);

            return buffer.getInt();
        } catch (Exception e) {
            System.out.println("Excepcion: " + e.getMessage());
            return null;
        }

    }

private Key[] leerLLaves() {
    FileInputStream archivo;
    ObjectInputStream ois;
    String nombreArchivo;

    try {
        // cargar llave publica
        nombreArchivo = "keys/llave_publica";
        archivo = new FileInputStream(nombreArchivo);
        ois = new ObjectInputStream(archivo);
        PublicKey publicKey = (PublicKey) ois.readObject();
        ois.close();

        // cargar llave privada
        nombreArchivo = "keys/llave_privada";
        archivo = new FileInputStream(nombreArchivo);
        ois = new ObjectInputStream(archivo);
        PrivateKey privateKey = (PrivateKey) ois.readObject();
        ois.close();

        return new Key[]{privateKey, publicKey};

    } catch (IOException | ClassNotFoundException e) {
        System.err.println("Error al cargar las llaves: " + e.getMessage());
        e.printStackTrace();
    }

    return new Key[]{};
}
}