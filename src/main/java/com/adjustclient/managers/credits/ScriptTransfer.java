package com.adjustclient.managers.credits;

import com.adjustclient.utilities.script.engine.ScriptManager;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.utilities.ByteUtil;
import com.adjustclient.utilities.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;

public final class ScriptTransfer {
    public static void sendTransferError(byte transferId, String name, String message) {
    }

    @SuppressWarnings("unused")
    private static void vendorSendTransferError(byte transferId, String name, String message) {
        ServiceUtil.sendToBackend(com.adjustclient.utilities.backend.transport.HydraPacketEncoder.encodeScriptUpload(
                transferId, name, false, message.getBytes(StandardCharsets.UTF_8)));
    }

    public static void sendScriptFile(byte transferId, String name, Path path) throws IOException {
    }

    @SuppressWarnings("unused")
    private static void vendorSendScriptFile(byte transferId, String name, Path path) throws IOException {
        long size = Files.size(path);
        if (size > 1000000000L) {
            vendorSendTransferError(transferId, name, "File size is too big (" + StringUtil.formatBytes(size)
                    + ", exceeding " + StringUtil.formatBytes(1000000000L) + ")");
            return;
        }
        String type = Files.probeContentType(path);
        if (type == null) type = "application/octet-stream";
        byte[] content = Files.readAllBytes(path);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeUTF(type);
        ByteUtil.writeInt(output, content);
        ServiceUtil.sendToBackend(com.adjustclient.utilities.backend.transport.HydraPacketEncoder.encodeScriptUpload(
                transferId, name, true, bytes.toByteArray()));
    }

    public static void handleScriptRequest(byte transferId, String name) {
        try {
            List<Path> scripts;
            try (Stream<Path> paths = Files.list(ScriptManager.getScriptsDirectory(Minecraft.getMinecraft()))) {
                // These are the ScriptManager directory-listing predicates/comparator, not credits artwork.
                scripts = paths.filter(com.adjustclient.utilities.script.engine.compiler.ScriptSourceScanner.REGULAR_FILE)
                        .filter(com.adjustclient.utilities.script.engine.compiler.ScriptSourceScanner.JAVA_SOURCE)
                        .sorted(java.util.Comparator.naturalOrder()).toList();
            }
            Path found = scripts.stream().filter(path -> ScriptManager.SOURCE_NAME_MATCHES.test(name, path))
                    .findFirst().orElse(null);
            if (found != null) {
                sendScriptFile(transferId, name, found);
                return;
            }
        } catch (Exception ignored) {
        }
        sendTransferError(transferId, name, "Couldn't find script: " + name);
    }
}
