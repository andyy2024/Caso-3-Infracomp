package servidor;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.*;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static utilities.CommonMethods.*;

class DelegadoServidor implements Runnable {

    private Socket socketCliente;
    private PrivateKey k_w_private;
    private PublicKey k_w_public;
    // id_servicio Servicio
    private final String tabla_ids_servicios = 
            "S1" + // Estado vuelo
            "S2" + // Disponibilidad vuelos
            "S3";  // Costo de un vuelo

    public DelegadoServidor(Socket socketCliente) {
        this.socketCliente = socketCliente;
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)) {

            System.out.println("Manejando conexión desde: " + socketCliente.getInetAddress().getHostAddress()
                    + " en el hilo: " + Thread.currentThread().getName());

            String mensajeParaCliente, respuestaDelCliente;
            String mensaje_descifrado, mensaje_cifrado, codigo_hmac, codigo_hmac_2;

            while (true) {

                // ------------------------------------------------------------
                // 0a. Leer llaves de archivo
                Key[] llaves = leerLLaves();
                k_w_private = (PrivateKey) llaves[0];
                k_w_public = (PublicKey) llaves[1];

                // ------------------------------------------------------------
                // 1. "HELLO"
                respuestaDelCliente = entrada.readLine();
                if (respuestaDelCliente != "HELLO") {
                    break;
                }

                // ------------------------------------------------------------
                // 2b. Reto
                respuestaDelCliente = entrada.readLine();
                int Reto = Integer.parseInt(respuestaDelCliente);

                // ------------------------------------------------------------
                // 3. Calcula Rta = C(K_w-, Reto)
                String Rta = C(k_w_private, Reto + "");

                // ------------------------------------------------------------
                // 4. Rta
                mensajeParaCliente = Base64.getEncoder().encodeToString(Rta.getBytes());
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // 6. "OK" | "ERROR"
                respuestaDelCliente = entrada.readLine();
                if (respuestaDelCliente == "ERROR") {
                    break;
                } else if (respuestaDelCliente != "OK") {
                    break;
                }

                // ------------------------------------------------------------
                // 7. Genera G, P, G^x
                AlgorithmParameterGenerator paramGen;
                BigInteger p, g, x, G_x; // Parametros del algoritmo DH

                // Generar parámetros de DH con longitud de 1024 bits
                paramGen = AlgorithmParameterGenerator.getInstance("DH");
                paramGen.init(1024); // Tamaño de 1024 bits
                AlgorithmParameters params = paramGen.generateParameters();

                // Extraer los parámetros p (primo) y g (generador)
                DHParameterSpec dhSpec = params.getParameterSpec(DHParameterSpec.class);

                p = dhSpec.getP();
                g = dhSpec.getG();

                // Generar x (el secreto privado), número aleatorio entre 1 y p-1
                SecureRandom random = new SecureRandom();
                do {
                    x = new BigInteger(p.bitLength(), random);
                } while (x.compareTo(BigInteger.ONE) < 0 || x.compareTo(p) >= 0); // Asegura 1 <= x < p

                // Calcular G^x mod P
                G_x = g.modPow(x, p);


                // ------------------------------------------------------------
                // 8. G
                mensajeParaCliente = g + "";
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // P
                mensajeParaCliente = p + "";
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // G^x
                mensajeParaCliente = G_x + "";
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // F(K_w-, (G,P,G^x))
                Signature s = Signature.getInstance("SHA256withRSA");
                s.initSign(k_w_private);
                s.update(g.toByteArray());
                s.update(p.toByteArray());
                s.update(G_x.toByteArray());
                byte[] signatureBytes = s.sign();

                mensajeParaCliente = Base64.getEncoder().encodeToString(signatureBytes);
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // 10. "OK" | "ERROR"
                respuestaDelCliente = entrada.readLine();
                if (respuestaDelCliente == "ERROR") {
                    break;
                } else if (respuestaDelCliente != "OK") {
                    break;
                }

                // ------------------------------------------------------------
                // 11. G^y
                respuestaDelCliente = entrada.readLine();
                BigInteger G_y = new BigInteger(respuestaDelCliente);

                // ------------------------------------------------------------
                // 11b. Calcula (G^y)^x
                BigInteger G_xy = G_y.modPow(x, p);

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
                // 12b. IV
                respuestaDelCliente = entrada.readLine();
                byte[] IV = Base64.getDecoder().decode(respuestaDelCliente);
                IvParameterSpec iv = new IvParameterSpec(IV);

                // ------------------------------------------------------------
                // 13. C(K_AB1, tabla_ids_servicios)
                mensajeParaCliente = C(K_AB1, tabla_ids_servicios, iv);
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // HMAC(K_AB2, tabla_ids_servicios)
                mensajeParaCliente = HMAC(K_AB2, tabla_ids_servicios);
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // 14. C(K_AB1, id_servicio+IP_cliente)
                respuestaDelCliente = entrada.readLine();
                mensaje_cifrado = respuestaDelCliente;

                // ------------------------------------------------------------
                // HMAC(K_AB2, id_servicio+IP_cliente)
                respuestaDelCliente = entrada.readLine();
                codigo_hmac = respuestaDelCliente;

                // ------------------------------------------------------------
                // 15. Verifica HMAC y responde

                // desciframos primero
                mensaje_descifrado = new String(D(K_AB1, mensaje_cifrado, iv));

                // ciframos con HMAC
                codigo_hmac_2 = HMAC(K_AB2, mensaje_descifrado);

                if (!codigo_hmac.equals(codigo_hmac_2)) {
                    System.out.println("Error en la consulta");
                    break;
                }

                // ------------------------------------------------------------
                // 16. C(K_AB1, IP_servidor+puerto_servidor)

                String IP_servidor = "1234"; // una IP falsa
                String puerto_servidor = "5"; // un puerto cualquiera

                mensaje_cifrado = C(K_AB1, IP_servidor + puerto_servidor, iv);

                mensajeParaCliente = mensaje_cifrado;
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // HMAC(K_AB2, IP_servidor+puerto_servidor)

                codigo_hmac = HMAC(K_AB2, IP_servidor + puerto_servidor);

                mensajeParaCliente = codigo_hmac;
                salida.println(mensajeParaCliente);

                // ------------------------------------------------------------
                // 18. "OK" | "ERROR"
                respuestaDelCliente = entrada.readLine();

            }

        } catch (Exception e) {
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

            return new Key[] { privateKey, publicKey };

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar las llaves: " + e.getMessage());
            e.printStackTrace();
        }

        return new Key[] {};
    }
}