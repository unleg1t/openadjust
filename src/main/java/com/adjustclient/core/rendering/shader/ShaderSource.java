package com.adjustclient.core.rendering.shader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20;

public final class ShaderSource implements AutoCloseable {
    public final int programId;
    private Object field8;

    public ShaderSource(String vertexSource, String fragmentSource) {
        int vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertex, vertexSource);
        GL20.glCompileShader(vertex);
        int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragment, fragmentSource);
        GL20.glCompileShader(fragment);
        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertex);
        GL20.glAttachShader(programId, fragment);
        GL20.glLinkProgram(programId);
    }

    public static ShaderSource loadFromResources(ResourceLocation vertex, ResourceLocation fragment) {
        try {
            DefaultResourcePack pack = Minecraft.getMinecraft().mcDefaultResourcePack;
            String vertexText;
            InputStream vertexInput = DefaultResourcePack.class.getResourceAsStream("/assets/" + vertex.getResourceDomain() + "/" + vertex.getResourcePath());
            if (vertexInput == null) vertexInput = pack.getInputStreamAssets(vertex);
            if (vertexInput == null) throw new FileNotFoundException(vertex.getResourcePath());
            try (InputStream input = vertexInput) {
                vertexText = IOUtils.toString(input, StandardCharsets.UTF_8);
            }
            String fragmentText;
            InputStream fragmentInput = DefaultResourcePack.class.getResourceAsStream("/assets/" + fragment.getResourceDomain() + "/" + fragment.getResourcePath());
            if (fragmentInput == null) fragmentInput = pack.getInputStreamAssets(fragment);
            if (fragmentInput == null) throw new FileNotFoundException(fragment.getResourcePath());
            try (InputStream input = fragmentInput) {
                fragmentText = IOUtils.toString(input, StandardCharsets.UTF_8);
            }
            return new ShaderSource(vertexText, fragmentText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public void close() {
        GL20.glDeleteProgram(programId);
    }
}
