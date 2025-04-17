package cliente;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
            // iniciar variables antes de iniciar ^^
            Random random = new Random();

            while (true) {
                //------------------------------------------------------------
                // 0b. Leer llave de archivo
                k_w_public = leerLLave();
                // recorda: la llave publica se genera con llaves.KeyGenerator

                //------------------------------------------------------------
                // 1. "HELLO"
                salida.println("HELLO"); // envio saludo inicial

                //------------------------------------------------------------
                // 2a. Genera un reto: numero aleatorio
                int Reto = random.nextInt(100);
                //------------------------------------------------------------
                // 2b. Reto
                salida.println(Reto + "");

                //------------------------------------------------------------
                // 4. Rta
                String linea = entrada.readLine();
                int Rta = Integer.parseInt(linea);

                //------------------------------------------------------------
                // 5a. Calcula R = D(K_w+, Rta)
                int R = D(k_w_public, Rta);

                //------------------------------------------------------------
                // 5b. Verifica R == Reto
                if (R != Reto) break; // si no coincide, sale

                //------------------------------------------------------------
                // 6. "OK" | "ERROR"
                salida.println("OK");
                //llegua ok al servidor

                //------------------------------------------------------------
                // 8. G
                linea = entrada.readLine();
                int G = Integer.parseInt(linea);

                //------------------------------------------------------------
                // P
                linea = entrada.readLine();
                int P = Integer.parseInt(linea);//P es primo de 1024 bits

                //------------------------------------------------------------
                // G^x
                linea = entrada.readLine();
                int G_x = Integer.parseInt(linea);

                //------------------------------------------------------------
                // F(K_w-, (G,P,G^x))
                byte[] firma = Base64.getDecoder().decode(entrada.readLine());

                //------------------------------------------------------------
                // 9. Verifica F(K_w-, (G,P,G'))
                Signature s = Signature.getInstance("SHA256withRSA");
                s.initVerify(k_w_public);
                s.update(BigInteger.valueOf(G).toByteArray());
                s.update(BigInteger.valueOf(P).toByteArray());
                s.update(BigInteger.valueOf(G_x).toByteArray());

                if (!s.verify(firma)) {
                    salida.println("ERROR");
                    break;
                }

                //------------------------------------------------------------
                // 10. "OK" | "ERROR"
                salida.println("OK"); // fin verificacion firma

                //------------------------------------------------------------
                // 11a. Calcula (G^x)^y
                BigInteger pBi = BigInteger.valueOf(P);
                BigInteger gBi = BigInteger.valueOf(G);
                BigInteger gxBi = BigInteger.valueOf(G_x);
                DHParameterSpec dh = new DHParameterSpec(pBi, gBi);
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
                kpg.initialize(dh);
                KeyPair kp = kpg.generateKeyPair();
                KeyAgreement ka = KeyAgreement.getInstance("DH");
                ka.init(kp.getPrivate());
                PublicKey servPub = KeyFactory.getInstance("DH").generatePublic(
                        new javax.crypto.spec.DHPublicKeySpec(gxBi, pBi, gBi));
                ka.doPhase(servPub, true);

                //------------------------------------------------------------
                // genera llave simetrica para cifrar K_AB1
                byte[] secreto = ka.generateSecret();
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                byte[] dig = md.digest(secreto);
                byte[] kEnc = Arrays.copyOf(dig, 32);

                //------------------------------------------------------------
                // genera llave simetrica para MAC K_AB2
                byte[] kMac = Arrays.copyOfRange(dig, 32, 64);

                //------------------------------------------------------------
                // 11. G^y
                DHPublicKey myPub = (DHPublicKey) kp.getPublic();
                salida.println(myPub.getY().toString());

                //------------------------------------------------------------
                // 12a. genera iv para aes cbc
                byte[] IV = new byte[16];
                random.nextBytes(IV);

                //------------------------------------------------------------
                // 12b. IV
                salida.println(Base64.getEncoder().encodeToString(IV));

                //------------------------------------------------------------
                // 13. C(K_AB1, tabla_ids_servicios)
                byte[] tblCif = Base64.getDecoder().decode(entrada.readLine());

                //------------------------------------------------------------
                // HMAC(K_AB2, tabla_ids_servicios)
                byte[] tblHmac = Base64.getDecoder().decode(entrada.readLine());

                //------------------------------------------------------------
                // 13b. verifica hmac tabla
                Mac m = Mac.getInstance("HmacSHA256");
                m.init(new SecretKeySpec(kMac, "HmacSHA256"));
                byte[] calc = m.doFinal(tblCif);
                if (!Arrays.equals(calc, tblHmac)) {
                    System.out.println("ERROR: hmac tabla invalido");
                    break;
                }

                //------------------------------------------------------------
                // 14. C(K_AB1, id_servicio+IP_cliente)
                Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
                aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kEnc, "AES"), new IvParameterSpec(IV));
                byte[] tblDec = aes.doFinal(tblCif);
                String[] opts = new String(tblDec, StandardCharsets.UTF_8).split(";");
                String scel = opts[random.nextInt(opts.length)];
                String pay = scel + ":" + socketCliente.getLocalAddress().getHostAddress();

                //------------------------------------------------------------
                // HMAC(K_AB2, id_servicio+IP_cliente)
                aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kEnc, "AES"), new IvParameterSpec(IV));
                byte[] payC = aes.doFinal(pay.getBytes(StandardCharsets.UTF_8));
                byte[] payH = m.doFinal(payC);
                salida.println(Base64.getEncoder().encodeToString(payC));
                salida.println(Base64.getEncoder().encodeToString(payH));

                //------------------------------------------------------------
                // 16. C(K_AB1, IP_servidor+puerto_servidor)
                byte[] rspC = Base64.getDecoder().decode(entrada.readLine());

                //------------------------------------------------------------
                // HMAC(K_AB2, IP_servidor+puerto_servidor)
                byte[] rspH = Base64.getDecoder().decode(entrada.readLine());

                byte[] rspDec = aes.doFinal(rspC);
                byte[] rspCh = m.doFinal(rspDec);

                //------------------------------------------------------------
                // 17. verifica hmac y envia ok
                if (!Arrays.equals(rspCh, rspH)) {
                    System.out.println("ERROR: hmac final invalido");
                } else {
                    System.out.println("Servidor delegado: " + new String(rspDec));
                    salida.println("OK"); // 18. "OK"
                }
                break;
            }

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error en el delegado " + nombreDelegado + ": " + e.getMessage());
        } finally {
            try {
                if (socketCliente != null && !socketCliente.isClosed()) {
                    socketCliente.close();
                    System.out.println(nombreDelegado + " cerrO la conexion");
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión en " + nombreDelegado  + e.getMessage());
            }
        }
    }

    private int D(PublicKey llave, int m_cifrado) throws GeneralSecurityException {
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.DECRYPT_MODE, llave);
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES).putInt(m_cifrado);
        byte[] res = rsa.doFinal(buf.array());
        return ByteBuffer.wrap(res).getInt();
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
