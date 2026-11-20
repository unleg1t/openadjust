package com.adjustclient.utilities.backend.transport;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class HydraPublicKey {
    public static final String ENCODED_PUBLIC_KEY =
            "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAm5DOmUQwkzEugrbCA2W2"
            + "5aqIwCu5rwJi2PaEr3E7Gpt2HmHBNXH1J+KV1qXyIKGVl9jcnjNSFQoFXSXIASij"
            + "O5rbz0okbCcmlZTLPLOX8PQqQj4go5HQNnXutoUReLn1Fw2RLT0AYZ6C+Q3ZlPLp"
            + "nj5TStd2utOxB5xuk073CTQzrlHKpA/prwXCFfUbQY2YNHxBtM2aLTf3yuFnlF2Y"
            + "NT94/Qx42P8zEzF8Gc1ovyR2/v4adC8/8vtRL+jWCAJ3r5yDH1R/poYNrvYXO8IF"
            + "+e0i5vTBpMAg9eaa5E5Y/SoTkPYhraAhB/582BaFaH3tNRy48rDnYO8JD85RRMTv"
            + "UE35bZi2/SusL+G1vnaivcSSe1sk3h+EzqEpAtaRDBFMS189hfLAAWxnABW8eLlS"
            + "mw7do+0/Xtn5n4iYZcjln6b/UXo5jlRWU7qgcARdANICURBT0sKbCAWWzbNtKAzS"
            + "0Yz3d2zwWcHgyraNOsWQNvW8yBbTO72oX/lw5/a1mAZeqzIlG8R3Nt4kIlRnB2Gd"
            + "BN2wdy3rOBTjphRaq7+ACJr+m/fOe2dZ2uP1oVyK+lDRqpHUWurYsbXol917cms8"
            + "Bx9+4enXoeAey3VOPEYqriLB4Vnn5LwSHMq0tiCVyG9mA5SPVF/dbOGlSAnSK0k4"
            + "qK0yTMtrlnmgDxdJ7kQaUssCAwEAAQ==";

    public static PublicKey PUBLIC_KEY;

    public static PublicKey getPublicKey() {
        if (PUBLIC_KEY != null) {
            return PUBLIC_KEY;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(ENCODED_PUBLIC_KEY);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PUBLIC_KEY = kf.generatePublic(spec);
            return PUBLIC_KEY;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA KeyFactory not available", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Could not generate public key", e);
        }
    }
}
