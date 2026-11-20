package com.adjustclient.utilities.backend.protocol;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.network.PacketBuffer;

public class ImageIoHelper {
    public static void writeImage(BufferedImage image, PacketBuffer buffer) {
        buffer.writeBoolean(image == null);
        if (image != null) {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageIO.write(image, "png", output);
                byte[] bytes = output.toByteArray();
                buffer.writeInt(bytes.length);
                buffer.writeBytes(bytes);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
