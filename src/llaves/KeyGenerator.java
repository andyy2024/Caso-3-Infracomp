package llaves;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyGenerator {

    private final static String ALGORITMO = "RSA";


    public static void main(String[] args) throws Exception {

        // generar llaves
        KeyPairGenerator generator = KeyPairGenerator
                    .getInstance(ALGORITMO);
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey llavePublica = keyPair.getPublic();
        PrivateKey llavePrivada = keyPair.getPrivate();

        // Guardar en un archivo las llaves
        FileOutputStream archivo;
        ObjectOutputStream oos;
        String nombreArchivo;

        nombreArchivo = "keys/llave_publica";
        archivo = new FileOutputStream(nombreArchivo);
        oos = new ObjectOutputStream(archivo);
        oos.writeObject(llavePublica);
        oos.close();

        nombreArchivo = "keys/llave_privada";
        archivo = new FileOutputStream(nombreArchivo);
        oos = new ObjectOutputStream(archivo);
        oos.writeObject(llavePrivada);
        oos.close();
    }
}
