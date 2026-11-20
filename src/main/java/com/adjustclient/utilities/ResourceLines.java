package com.adjustclient.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public final class ResourceLines {
    public static String[] read(ResourceLocation location) throws IOException {
        InputStream input = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        String[] lines = IOUtils.toString(input, StandardCharsets.UTF_8).split("\n");
        IOUtils.closeQuietly(input);
        for (int i = 0; i < lines.length; i++) lines[i] = lines[i].replace("\r", "");
        return lines;
    }
}
