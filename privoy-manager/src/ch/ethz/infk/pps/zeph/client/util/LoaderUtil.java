package ch.ethz.infk.pps.zeph.client.util;

import ch.ethz.infk.pps.shared.avro.util.CertificateFileStore;
import ch.ethz.infk.pps.shared.avro.util.FileStoreNames;
import ch.ethz.infk.pps.shared.avro.util.ProducerKeyFileStore;
import ch.ethz.infk.pps.shared.avro.util.SharedKeyFileStore;
import ch.ethz.infk.pps.zeph.client.loader.FileLoader;
import ch.ethz.infk.pps.zeph.crypto.DiffieHellmanKeyExchange;
import ch.ethz.infk.pps.zeph.shared.CryptoUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.ImmutableUnorderedPair;
import ch.ethz.infk.pps.zeph.shared.pojo.ProducerIdentity;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import javax.crypto.SecretKey;
import java.io.File;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LoaderUtil {

    private static KeyStore ks;
    private static SecretKey fileKey;

    private static File certFile;
    private static File keyFile;

    static{
        try{
            Security.removeProvider("BC");
            Security.addProvider(new BouncyCastleProvider());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void init(String keystorePassword){
        char[] ksPwd=keystorePassword.toCharArray();
        String ksPath= PathUtil.getGlobalKeystorePath();
        ks = KeyStoreUtil.buildKeyStore(ksPath, ksPwd, "pkcs12");

        ConcurrentHashMap<Long, Certificate> globalCertificates = new ConcurrentHashMap();
        Map<Long, ProducerIdentity> producers = new ConcurrentHashMap();
        certFile = PathUtil.getCertificatesFile();
        if (certFile.exists()) {
            globalCertificates.putAll(CertificateFileStore.load(certFile));
            System.out.println("Loaded "+globalCertificates.size()+"(global) certificates from: "+certFile.getPath());
        }
        try {
            fileKey = (SecretKey)ks.getKey("local-producers-key", ksPwd);
            keyFile=PathUtil.getKeysFile();
            if (keyFile.exists()) {
                producers.putAll(ProducerKeyFileStore.load(keyFile, fileKey));
                System.out.println("Loaded "+producers.size()+" producers from: "+keyFile.getPath());
            } else if (fileKey == null) {
                fileKey = CryptoUtil.generateKey();
                ks.setEntry("local-producers-key", new KeyStore.SecretKeyEntry(fileKey), new KeyStore.PasswordProtection(ksPwd));
                KeyStoreUtil.flushKeyStore(ks, ksPath, ksPwd);
                System.out.println("Generated new local-producers-key and updated KeyStore: " + ksPath);
            }
            producers.forEach((pId,pInfo)->{
                if (globalCertificates.containsKey(pId)) {
                    System.out.println("certificate for this producer id exists in global certificates list"+pId);
                } else {
                    globalCertificates.put(pId, pInfo.getCertificate());
                    System.out.println("certificate add producer"+pId);
                }
            });

            FileLoader.getInstance().setProducers(producers);
            FileLoader.getInstance().setGlobalCertificates(globalCertificates);
            if (producers.size() > 0 && globalCertificates.size() > 0) {
                System.out.println("already init producers and globalCertificates");
                ProducerKeyFileStore.store(keyFile, fileKey, producers);
                CertificateFileStore.store(certFile, globalCertificates);
                System.out.println("Running Generate Shared Keys Phase...");
                producers.forEach((pId, pInfo) -> {
                    PrivateKey privateKey = pInfo.getPrivateKey();
                    SecretKey sharedKeysFileKey = pInfo.getSharedKeysFileKey();
                    ConcurrentHashMap<Long, SecretKey> sharedKeys = new ConcurrentHashMap();
                    File sharedKeysFile = PathUtil.getSharedKeysFile(pId);
                    DiffieHellmanKeyExchange keyExchange = new DiffieHellmanKeyExchange();
                    globalCertificates.forEach((otherProducerId, certificate) -> {
                        if (!sharedKeys.containsKey(otherProducerId)) {
                            SecretKey sharedKey = keyExchange.generateSharedKey(privateKey, certificate);
                            sharedKeys.put(otherProducerId, sharedKey);
                        }

                    });
                    SharedKeyFileStore.store(sharedKeysFile, "sk" + pId, sharedKeysFileKey, sharedKeys);
                    System.out.println("Generated " + sharedKeys.size() + " shared keys");
                });
            }

        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException var11) {
            throw new IllegalStateException("Failed to update KeyStore", var11);
        }

    }


    public static ConcurrentHashMap<Long, Certificate> getCertificate(){
        ConcurrentHashMap<Long, Certificate> certificates = new ConcurrentHashMap();
        certificates.putAll(CertificateFileStore.load(certFile));
        return certificates;
    }
    public static ProducerIdentity getProducerIdentity(Long pId,String keystorePassword){
        return ProducerKeyFileStore.load(keyFile, fileKey).get(pId);

    }

    public static Map<Long, ProducerIdentity> getAllProducerIdentity(String keystorePassword){
        return ProducerKeyFileStore.load(keyFile, fileKey);

    }

    public static void loadAllProducerIdentity(String keystorePassword,Map<Long, ProducerIdentity> producers){
        ProducerKeyFileStore.store(keyFile,fileKey,producers);
    }

    public static Map<ImmutableUnorderedPair<Long>, SecretKey> getProducerSharedKeys(Long pId,ProducerIdentity pInfo){
        File sharedKeysFile = PathUtil.getSharedKeysFile(pId);
        Map<Long, SecretKey> sharedKeys = SharedKeyFileStore.load(sharedKeysFile, FileStoreNames.getSharedKeyId(pId), pInfo.getSharedKeysFileKey());
        sharedKeys.remove(pId);
        return (Map)sharedKeys.entrySet().stream().collect(Collectors.toMap((e) -> {
            return ImmutableUnorderedPair.of((Long)e.getKey(), pId);
        }, (e) -> {
            return (SecretKey)e.getValue();
        }));
        //return sharedKeys;
    }
}
