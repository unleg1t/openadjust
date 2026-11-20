package com.adjustclient.utilities.lunar.websocket;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

final class LunarAuthListener implements WebSocket.Listener {
    private final Session session;
    final CompletableFuture<String> jwt = new CompletableFuture<>();
    private final ByteArrayOutputStream fragments = new ByteArrayOutputStream();
    private Object field16;

    LunarAuthListener(Session session) {
        this.session = session;
    }

    private void authenticationSucceeded(byte[] payload) {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(payload));
        for (int tag; (tag = reader.readTag()) != 0;) {
            if (tag >>> 3 == 1) {
                jwt.complete(reader.readString());
                return;
            }
            reader.skipField(tag);
        }
        jwt.completeExceptionally(new Exception("AuthSuccessMessage missing jwt field"));
    }

    private void missingEncryptionFields(WebSocket socket) {
        try {
            ProtoWriter failure = new ProtoWriter();
            failure.writeString(1, "EncryptionRequestMessage missing required fields");
            ProtoWriter envelope = new ProtoWriter();
            envelope.writeBytes(3, failure.toByteArray());
            jwt.completeExceptionally(new Exception("EncryptionRequestMessage missing required fields"));
            socket.sendBinary(ByteBuffer.wrap(envelope.toByteArray()), true).whenComplete(
                    (result, error) -> socket.abort());
        } catch (Exception error) {
            socket.abort();
        }
    }

    private void fail(WebSocket socket, Throwable error) {
        jwt.completeExceptionally(error);
        socket.abort();
    }

    private void dispatch(WebSocket socket, byte[] message) throws Exception {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(message));
        int tag = reader.readTag();
        if (tag == 0) {
            throw new IllegalArgumentException("Received an empty authentication message");
        }
        int field = tag >>> 3;
        byte[] payload = reader.readRawBytes(reader.readLength());
        switch (field) {
            case 1:
                encryptionRequested(socket, payload);
                break;
            case 2:
                authenticationSucceeded(payload);
                break;
            default:
                Logger.log(LogLevel.WARN, true, "Unknown authentication message field {}", field);
                break;
        }
    }

    private void send(WebSocket socket, byte[] message, String name) {
        socket.sendBinary(ByteBuffer.wrap(message), true).whenComplete(
                (result, error) -> sendCompleted(socket, name, result, error));
    }

    private void sendCompleted(WebSocket socket, String name, WebSocket result, Throwable error) {
        if (error != null) {
            jwt.completeExceptionally(new Exception("Failed to send " + name, error));
            socket.abort();
        }
    }

    private void encryptionRequested(WebSocket socket, byte[] payload) throws Exception {
        ProtoReader reader = new ProtoReader(ByteBuffer.wrap(payload));
        byte[] encodedPublicKey = null;
        byte[] verifyToken = null;
        for (int tag; (tag = reader.readTag()) != 0;) {
            switch (tag >>> 3) {
                case 1:
                    encodedPublicKey = reader.readBytes();
                    break;
                case 2:
                    verifyToken = reader.readBytes();
                    break;
                default:
                    reader.skipField(tag);
                    break;
            }
        }
        if (encodedPublicKey == null || verifyToken == null) {
            missingEncryptionFields(socket);
            return;
        }
        PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(encodedPublicKey));
        SecretKey secret = KeyGenerator.getInstance("AES").generateKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedSecret = cipher.doFinal(secret.getEncoded());
        byte[] encryptedToken = cipher.doFinal(verifyToken);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(secret.getEncoded());
        digest.update(encodedPublicKey);
        String serverHash = new BigInteger(digest.digest()).toString(16);
        Minecraft.getMinecraft().getSessionService().joinServer(session.getProfile(), session.getToken(), serverHash);
        ProtoWriter response = new ProtoWriter();
        response.writeBytes(1, encryptedSecret);
        response.writeBytes(2, encryptedToken);
        ProtoWriter envelope = new ProtoWriter();
        envelope.writeBytes(2, response.toByteArray());
        send(socket, envelope.toByteArray(), "EncryptionResponseMessage");
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer data, boolean last) {
        try {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            fragments.writeBytes(bytes);
            if (last) {
                byte[] message = fragments.toByteArray();
                fragments.reset();
                dispatch(socket, message);
            }
            socket.request(1);
            return CompletableFuture.completedFuture(null);
        } catch (Exception error) {
            fail(socket, error);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket socket, int statusCode, String reason) {
        jwt.completeExceptionally(new Exception("Server closed: " + statusCode + " " + reason));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onOpen(WebSocket socket) {
        socket.request(1);
        byte[] hello = LunarAuthenticator.encodeHello(session.getProfile().getId(), session.getUsername());
        ProtoWriter envelope = new ProtoWriter();
        envelope.writeBytes(1, hello);
        send(socket, envelope.toByteArray(), "HelloMessage");
    }

    @Override
    public CompletionStage<?> onText(WebSocket socket, CharSequence text, boolean last) {
        socket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {
        fail(socket, error);
    }
}
