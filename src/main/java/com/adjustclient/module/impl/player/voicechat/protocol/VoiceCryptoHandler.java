package com.adjustclient.module.impl.player.voicechat.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public final class VoiceCryptoHandler {
    public Key publicKey;
    public Callable<Cipher> encryptCipherFactory;
    public Callable<Cipher> decryptCipherFactory;
    public Cipher encryptCipher;
    public Cipher decryptCipher;
    public SecretKey secretKey;
    public int blockSize = -1;

    public VoiceCryptoHandler(String encodedPublicKey) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        publicKey = factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(encodedPublicKey)));
        blockSize = 200;
        encryptCipherFactory = this::createRsaEncryptCipher;
        encryptCipher = createEncryptCipher();
        decryptCipherFactory = null;
    }

    public VoiceCryptoHandler() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        secretKey = generator.generateKey();
        blockSize = -1;
        encryptCipherFactory = this::createAesEncryptCipher;
        encryptCipher = createEncryptCipher();
        decryptCipherFactory = this::createAesDecryptCipher;
        decryptCipher = createAesDecryptCipher();
        byte[] test = "42".getBytes(StandardCharsets.UTF_8);
        if (!Arrays.equals(test, decrypt(encrypt("42".getBytes(StandardCharsets.UTF_8), encryptCipher), decryptCipher))) {
            throw new IllegalArgumentException("Keyfiles don't match");
        }
    }

    public Cipher createAesDecryptCipher() throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher;
    }

    public Cipher createAesEncryptCipher() throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher;
    }

    public Cipher createRsaEncryptCipher() throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher;
    }

    public Cipher createEncryptCipher() throws Exception {
        if (encryptCipherFactory == null) throw new IllegalStateException("No encrypt cipher available");
        return encryptCipherFactory.call();
    }

    public byte[] encrypt(byte[] data, Cipher cipher) throws GeneralSecurityException {
        if (blockSize < 1) return cipher.doFinal(data);
        int blocks = data.length / blockSize;
        if (data.length % blockSize != 0) ++blocks;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < blocks; i++) {
            byte[] block = cipher.doFinal(data, blockSize * i, Math.min(blockSize, data.length - blockSize * i));
            out.write(block.length - 128);
            out.write(block, 0, block.length);
        }
        return out.toByteArray();
    }

    public byte[] decrypt(byte[] data, Cipher cipher) throws GeneralSecurityException {
        if (blockSize < 1) return cipher.doFinal(data);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (in.available() > 0) {
            byte[] block = new byte[in.read() + 128];
            in.read(block, 0, block.length);
            byte[] decoded = cipher.doFinal(block);
            out.write(decoded, 0, decoded.length);
        }
        return out.toByteArray();
    }
}
