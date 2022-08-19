package ch.ethz.infk.pps.zeph.client.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreUtil {

    public KeyStoreUtil() {
    }

    public static KeyStore buildKeyStore(String path, char[] pwd, String type) {
        try {
            KeyStore ks = KeyStore.getInstance(type);

            try {
                FileInputStream in = new FileInputStream(path);

                try {
                    ks.load(in, pwd);
                } catch (Throwable var8) {
                    try {
                        in.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }

                    throw var8;
                }

                in.close();
            } catch (FileNotFoundException var9) {
                System.out.println("KeyStore not found -> Creating new KeyStore");
                ks.load((InputStream)null, pwd);
            }

            return ks;
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException var10) {
            throw new IllegalStateException("failed to build KeyStore", var10);
        }
    }

    public static void flushKeyStore(KeyStore ks, String ksPath, char[] pwd) {
        try {
            FileOutputStream fos = new FileOutputStream(ksPath);

            try {
                ks.store(fos, pwd);
            } catch (Throwable var7) {
                try {
                    fos.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }

                throw var7;
            }

            fos.close();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException var8) {
            throw new IllegalArgumentException("failed to store KeyStore", var8);
        }

        System.out.println("stored keystore to disk: ks="+ksPath);
    }
}
