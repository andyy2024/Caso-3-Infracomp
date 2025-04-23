package utilities;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CommonMethods {

    // cifrado asimetrico (byte[] m)
    public static String C(PrivateKey llave, byte[] m) throws Exception {
        byte[] m_cifrado;
        Cipher cifrador = Cipher.getInstance("RSA");

        cifrador.init(Cipher.ENCRYPT_MODE, llave);
        m_cifrado = cifrador.doFinal(m);

        return Base64.getEncoder().encodeToString(m_cifrado);
    }

    // cifrado asimetrico (String m)
    public static String C(PrivateKey llave, String m) throws Exception {
        byte[] m_cifrado;
        Cipher cifrador = Cipher.getInstance("RSA");

        cifrador.init(Cipher.ENCRYPT_MODE, llave);
        m_cifrado = cifrador.doFinal(m.getBytes());

        return Base64.getEncoder().encodeToString(m_cifrado);
    }

    // cifrado simetrico (byte[] m)
    public static String C(SecretKey llave, byte[] m, IvParameterSpec IV) throws Exception {
        byte[] m_cifrado;

        String PADDING = "AES/CBC/PKCS5Padding";
        Cipher cifrador = Cipher.getInstance(PADDING);

        cifrador.init(Cipher.ENCRYPT_MODE, llave, IV);
        m_cifrado = cifrador.doFinal(m);

        return Base64.getEncoder().encodeToString(m_cifrado);
    }

    // cifrado simetrico (String m)
    public static String C(SecretKey llave, String m, IvParameterSpec IV) throws Exception {
        byte[] m_cifrado;

        String PADDING = "AES/CBC/PKCS5Padding";
        Cipher cifrador = Cipher.getInstance(PADDING);

        cifrador.init(Cipher.ENCRYPT_MODE, llave, IV);
        m_cifrado = cifrador.doFinal(m.getBytes());

        return Base64.getEncoder().encodeToString(m_cifrado);
    }

    // descifrado asimetrico (byte[] m_cifrado)
    static public byte[] D(PublicKey llave, byte[] m_cifrado) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.DECRYPT_MODE, llave);
        return rsa.doFinal(m_cifrado);
    }

    // descifrado simetrico (byte[] m_cifrado)
    static public byte[] D(SecretKey llave, byte[] m_cifrado, IvParameterSpec IV) throws Exception {
        String PADDING = "AES/CBC/PKCS5Padding";
        Cipher cifrador = Cipher.getInstance(PADDING);
        cifrador.init(Cipher.DECRYPT_MODE, llave, IV);
        return cifrador.doFinal(m_cifrado);
    }

    // descifrado simetrico (String m_cifrado *en base64)
    static public byte[] D(SecretKey llave, String m_cifrado, IvParameterSpec IV) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(m_cifrado);
        String PADDING = "AES/CBC/PKCS5Padding";
        Cipher cifrador = Cipher.getInstance(PADDING);
        cifrador.init(Cipher.DECRYPT_MODE, llave, IV);
        
        return cifrador.doFinal(bytes);
    }

    // HMAC (String m)
    static public String HMAC(SecretKey llave, String m) throws Exception {
        Mac mac = Mac.getInstance("HMACSHA256");
        mac.init(llave);
        byte[] bytes = mac.doFinal(m.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }

    // HMAC (byte[] m)
    public static String HMAC(SecretKey llave, byte[] m) throws Exception {
        Mac mac = Mac.getInstance("HMACSHA256");
        mac.init(llave);
        byte[] hmacBytes = mac.doFinal(m);

        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    // Convierte un int a byte[]
    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    // Convierte un byte[] a int
    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("El arreglo de bytes debe tener exactamente 4 elementos");
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

}
