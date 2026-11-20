package com.adjustclient.utilities;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public final class ImageUtil {
    public static BufferedImage read(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            return read(input);
        }
    }

    public static BufferedImage read(InputStream input) throws IOException {
        byte[] bytes = input.readAllBytes();
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    @SuppressWarnings("unused")
    public static BufferedImage vendorReadFallback(byte[] bytes, Exception error) throws IOException {
        if (error instanceof IOException || error instanceof IllegalArgumentException) {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        }
        return null;
    }
}
