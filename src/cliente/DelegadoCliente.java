package cliente;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

class DelegadoCliente implements Runnable {

    private Socket socketCliente;
    private String nombreDelegado;
    private PublicKey k_w_public;

    public DelegadoCliente(Socket socketCliente, String nombreDelegado) {
        this.socketCliente = socketCliente;
        this.nombreDelegado = nombreDelegado;
    }

    @Override
    public void run() {
        try (PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
             ) {

            System.out.println(nombreDelegado + " conectado al servidor.");
            
            // Iniciar variables antes de iniciar ^-^ 
            String mensajeParaServidor, respuestaDelServidor;
            Random random = new Random();

            while (true){
                //------------------------------------------------------------
                // 0b. Leer llave de archivo
                k_w_public = leerLLave();

                //------------------------------------------------------------
                // 1. "HELLO"
                mensajeParaServidor = "HELLO";
                salida.println(mensajeParaServidor);

                //------------------------------------------------------------
                // 2a. Genera un reto: número aleatorio (seudoaleatorio)
                Integer Reto = random.nextInt(100);
                mensajeParaServidor =  Reto + "";

                //------------------------------------------------------------
                // 2b. Reto
                salida.println(mensajeParaServidor);

                //------------------------------------------------------------
                // 4. Rta
                respuestaDelServidor = entrada.readLine();
                Integer Rta = Integer.parseInt(respuestaDelServidor);

                //------------------------------------------------------------
                // 5a. Calcula R = D(K_w+, Rta)
                Integer R = D(k_w_public, Rta);

                //------------------------------------------------------------
                // 5b. Verifica R == Reto
                if (R != Reto){break;}

                //------------------------------------------------------------
                // 6. "OK" | "ERROR"
                // <dale bro>

                //------------------------------------------------------------
                // 8. G
                respuestaDelServidor = entrada.readLine();
                Integer G = Integer.parseInt(respuestaDelServidor);

                //------------------------------------------------------------
                // P
                respuestaDelServidor = entrada.readLine();
                Integer P = Integer.parseInt(respuestaDelServidor);

                //------------------------------------------------------------
                // G^x
                respuestaDelServidor = entrada.readLine();
                Integer G_x = Integer.parseInt(respuestaDelServidor);

                //------------------------------------------------------------
                // F(K_w-, (G,P,G^x))
                // <dale bro>

                //------------------------------------------------------------
                // 9. Verifica F(K_w-, (G,P,G'))
                // <dale bro>

                //------------------------------------------------------------
                // 10. "OK" | "ERROR"
                // <dale bro>

                //------------------------------------------------------------
                // 11a. Calcula (G^x)^y
                // <dale bro>

                //------------------------------------------------------------
                // Genera llave simétrica para cifrar K_AB1
                // <dale bro>

                //------------------------------------------------------------
                // Genera llave simétrica para MAC K_AB2
                // <dale bro>

                //------------------------------------------------------------
                // 11. G^y
                // <dale bro>

                //------------------------------------------------------------
                // 12a. Genera IV
                byte[] IV = new byte[16];
                random.nextBytes(IV);

                //------------------------------------------------------------
                // 12b. IV
                mensajeParaServidor = Base64.getEncoder().encodeToString(IV);
                salida.println(mensajeParaServidor);

                //------------------------------------------------------------
                // 13. C(K_AB1, tabla_ids_servicios)
                // <dale bro>

                //------------------------------------------------------------
                // HMAC(K_AB2, tabla_ids_servicios)
                // <dale bro>

                //------------------------------------------------------------
                // 13b. Verifica HMAC
                // <dale bro>

                //------------------------------------------------------------
                // 14. C(K_AB1, id_servicio+IP_cliente)
                // <dale bro>

                //------------------------------------------------------------
                // HMAC(K_AB2, id_servicio+IP_cliente)
                // <dale bro>

                //------------------------------------------------------------
                // 16. C(K_AB1, IP_servidor+puerto_servidor)
                // <dale bro>

                //------------------------------------------------------------
                // HMAC(K_AB2, IP_servidor+puerto_servidor)
                // <dale bro>

                //------------------------------------------------------------
                // 17. Verifica HMAC, envía la respuesta y termina
                // <dale bro>

                //------------------------------------------------------------
                // 18. "OK" | "ERROR"
                // <dale bro>
            }


        } catch (IOException e) {
            System.err.println("Error en el delegado " + nombreDelegado + ": " + e.getMessage());
        } finally {
            try {
                if (socketCliente != null && !socketCliente.isClosed()) {
                    socketCliente.close();
                    System.out.println(nombreDelegado + " cerró la conexión.");
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión en " + nombreDelegado + ": " + e.getMessage());
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

    private Integer D(PublicKey llave, Integer m_cifrado) {

        byte[] m;
        try {
            Cipher cifrador = Cipher.getInstance("RSA");
            cifrador.init(Cipher.DECRYPT_MODE, llave);

            // convertir el numero m_cifrado en un arreglo de bytes[]
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            byte[] textoCifrado = buffer.putInt(m_cifrado).array();

            m = cifrador.doFinal(textoCifrado);

            // convertir los bytes[] descifrados a un numero
            buffer = ByteBuffer.wrap(m);

            return buffer.getInt();

        } catch (Exception e) {
            System.out.println("Excepcion: " + e.getMessage());
            return null;
        }
    }

    public PublicKey leerLLave() {
        FileInputStream archivo;
        ObjectInputStream ois;
        String nombreArchivo;

        try {
            // cargar llave publica
            nombreArchivo = "keys/llave_publica";
            archivo = new FileInputStream(nombreArchivo);
            ois = new ObjectInputStream(archivo);
            PublicKey key = (PublicKey) ois.readObject();
            ois.close();
            return key;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar las llaves: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}