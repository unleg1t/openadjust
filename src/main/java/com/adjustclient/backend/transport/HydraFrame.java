package com.adjustclient.backend.transport;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;

public final class HydraFrame {
    public final AlgorithmParameterSpec iv;
    public final Key key;
    public final Cipher decryptCipher;
    public final Cipher encryptCipher;
    public Object field20;

    public HydraFrame(AlgorithmParameterSpec iv, Key key) throws GeneralSecurityException {
        this.iv = iv;
        this.key = key;
        Cipher dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
        dec.init(Cipher.DECRYPT_MODE, key, iv);
        this.decryptCipher = dec;
        Cipher enc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        enc.init(Cipher.ENCRYPT_MODE, key, iv);
        this.encryptCipher = enc;
    }
}
