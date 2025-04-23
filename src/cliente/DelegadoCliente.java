package cliente;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static utilities.CommonMethods.*;

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
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));) {

            System.out.println(nombreDelegado + " conectado al servidor.");
            // iniciar variables antes de iniciar ^^
            Random random = new Random();
            String linea;
            String mensaje_descifrado, mensaje_cifrado, codigo_hmac, codigo_hmac_2;

            while (true) {
                // ------------------------------------------------------------
                // 0b. Leer llave de archivo
                k_w_public = leerLLave();
                // recorda: la llave publica se genera con llaves.KeyGenerator

                // ------------------------------------------------------------
                // 1. "HELLO"
                salida.println("HELLO"); // envio saludo inicial

                // ------------------------------------------------------------
                // 2a. Genera un reto: numero aleatorio
                String Reto = random.nextInt(100) + "";
                // ------------------------------------------------------------
                // 2b. Reto
                salida.println(Reto);

                // ------------------------------------------------------------
                // 4. Rta
                linea = entrada.readLine();
                byte[] Rta = Base64.getDecoder().decode(linea);

                // ------------------------------------------------------------
                // 5a. Calcula R = D(K_w+, Rta)
                String R = new String(D(k_w_public, Rta));

                // ------------------------------------------------------------
                // 5b. Verifica R == Reto
                if (R != Reto)
                    break; // si no coincide, sale

                // ------------------------------------------------------------
                // 6. "OK" | "ERROR"
                salida.println("OK");
                // llegua ok al servidor

                // ------------------------------------------------------------
                // 8. G
                linea = entrada.readLine();
                BigInteger g = new BigInteger(linea);

                // ------------------------------------------------------------
                // P
                linea = entrada.readLine();
                BigInteger p = new BigInteger(linea);// P es primo de 1024 bits

                // ------------------------------------------------------------
                // G^x
                linea = entrada.readLine();
                BigInteger G_x = new BigInteger(linea);

                // ------------------------------------------------------------
                // F(K_w-, (G,P,G^x))
                byte[] firma = Base64.getDecoder().decode(entrada.readLine());

                // ------------------------------------------------------------
                // 9. Verifica F(K_w-, (G,P,G'))
                Signature s = Signature.getInstance("SHA256withRSA");
                s.initVerify(k_w_public);
                s.update(g.toByteArray());
                s.update(p.toByteArray());
                s.update(G_x.toByteArray());

                if (!s.verify(firma)) {
                    salida.println("ERROR");
                    break;
                }

                // ------------------------------------------------------------
                // 10. "OK" | "ERROR"
                salida.println("OK"); // fin verificacion firma

                // ------------------------------------------------------------
                // 11a. Calcula (G^x)^y
                BigInteger y, G_xy;
                SecureRandom sRandom = new SecureRandom();

                // Generar y (el secreto privado), número aleatorio entre 1 y p-1
                do {
                    y = new BigInteger(p.bitLength(), sRandom);
                } while (y.compareTo(BigInteger.ONE) < 0 || y.compareTo(p) >= 0); // Asegura 1 <= y < p

                // Calcular (G^x)^y mod P
                G_xy = G_x.modPow(y, p);

                // ------------------------------------------------------------
                // (1) genera llave simetrica para cifrar K_AB1
                // (2) genera llave simetrica para MAC K_AB2

                // Calcular el digest con SHA-512
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                byte[] masterKeyBytes = G_xy.toByteArray();
                byte[] hashBytes = digest.digest(masterKeyBytes);

                // Partir el digest en dos mitades
                int halfLength = hashBytes.length / 2;
                byte[] encryptionKeyBytes = Arrays.copyOfRange(hashBytes, 0, halfLength);
                byte[] hmacKeyBytes = Arrays.copyOfRange(hashBytes, halfLength, hashBytes.length);

                // crear las llaves simetricas a partir de los bytes
                SecretKey K_AB1 = new SecretKeySpec(encryptionKeyBytes, "AES");
                SecretKey K_AB2 = new SecretKeySpec(hmacKeyBytes, "HMACSHA256");

                // ------------------------------------------------------------
                // 11. G^y
                BigInteger G_y = g.modPow(y, p);
                salida.println(G_y + "");

                // ------------------------------------------------------------
                // 12a. Genera IV
                byte[] IV = new byte[16]; // el vector como tal
                random.nextBytes(IV);
                IvParameterSpec iv = new IvParameterSpec(IV); 

                // ------------------------------------------------------------
                // 12b. IV
                salida.println(Base64.getEncoder().encodeToString(IV));

                // ------------------------------------------------------------
                // 13. C(K_AB1, tabla_ids_servicios)
                mensaje_cifrado = entrada.readLine();

                // ------------------------------------------------------------
                // HMAC(K_AB2, tabla_ids_servicios)
                codigo_hmac = entrada.readLine();

                // ------------------------------------------------------------
                // 13b. Verifica HMAC

                // desciframos la tabla primero
                String tabla_ids_servicios = new String(D(K_AB1, mensaje_cifrado, iv));

                // ciframos la tabla
                codigo_hmac_2 = HMAC(K_AB2, tabla_ids_servicios);

                if (!codigo_hmac.equals(codigo_hmac_2)) {
                    System.out.println("ERROR: hmac tabla invalido");
                    break;
                }

                // ------------------------------------------------------------
                // 14. C(K_AB1, id_servicio+IP_cliente)

                String IP_cliente = "12345"; // IP falso

                // Escoge al azar un servicio
                String[] opciones = tabla_ids_servicios.split(" ");
                String id_servicio = opciones[random.nextInt(opciones.length)];
                
                mensaje_cifrado = C(K_AB1, id_servicio + IP_cliente, iv);
                salida.println(mensaje_cifrado);

                // ------------------------------------------------------------
                // HMAC(K_AB2, id_servicio+IP_cliente)

                codigo_hmac = HMAC(K_AB2, id_servicio + IP_cliente);
                salida.println(codigo_hmac);

                // ------------------------------------------------------------
                // 16. C(K_AB1, IP_servidor+puerto_servidor)

                mensaje_cifrado = entrada.readLine();

                // ------------------------------------------------------------
                // HMAC(K_AB2, IP_servidor+puerto_servidor)

                codigo_hmac = entrada.readLine();

                // ------------------------------------------------------------
                // 17 y 18. Verifica HMAC y evnia respuesta

                // desciframos primero
                mensaje_descifrado = new String(D(K_AB1, mensaje_cifrado, iv));

                // ciframos
                codigo_hmac = HMAC(K_AB2, mensaje_descifrado);

                if (!codigo_hmac.equals(codigo_hmac_2)) {
                    salida.println("ERROR");
                } else {
                    salida.println("OK");
                }

            }

        } catch (Exception e) {
            System.err.println("Error en el delegado " + nombreDelegado + ": " + e.getMessage());
        } finally {
            try {
                if (socketCliente != null && !socketCliente.isClosed()) {
                    socketCliente.close();
                    System.out.println(nombreDelegado + " cerrO la conexion");
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión en " + nombreDelegado + e.getMessage());
            }
        }
    }

    private PublicKey leerLLave() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("keys/llave_publica"))) {
            return (PublicKey) ois.readObject();
        } catch (Exception e) {
            System.err.println("error al cargar llaves: " + e.getMessage());
            return null;
        }
    }
}
