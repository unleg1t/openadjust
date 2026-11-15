package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.GLFW;

/**
 * LWJGL 2 {@code GLContext} bridge delegating to LWJGL 3 {@link GL}.
 */
public final class GLContext {
    private static final ThreadLocal<GLCapabilities> LAST_GL_CAPS = new ThreadLocal<>();
    private static final ThreadLocal<ContextCapabilities> LAST_COMPAT_CAPS = new ThreadLocal<>();

    public static ContextCapabilities getCapabilities() {
        GLCapabilities caps = null;
        try {
            caps = GL.getCapabilities();
        } catch (IllegalStateException ignored) {
            if (GLFW.glfwGetCurrentContext() != 0L) {
                caps = GL.createCapabilities();
            }
        }
        if (caps == null) {
            return new ContextCapabilities();
        }
        if (LAST_GL_CAPS.get() == caps) {
            ContextCapabilities cached = LAST_COMPAT_CAPS.get();
            if (cached != null) {
                return cached;
            }
        }
        ContextCapabilities wrapped = new ContextCapabilities(caps);
        LAST_GL_CAPS.set(caps);
        LAST_COMPAT_CAPS.set(wrapped);
        return wrapped;
    }

    public static synchronized void useContext(Object context) throws LWJGLException {
        useContext(context, false);
    }

    public static synchronized void useContext(Object context, boolean forwardCompatible) throws LWJGLException {
        if (context == null) {
            GL.setCapabilities(null);
            LAST_GL_CAPS.remove();
            LAST_COMPAT_CAPS.remove();
            return;
        }
        try {
            GL.createCapabilities(forwardCompatible);
            getCapabilities();
        } catch (Throwable t) {
            throw new LWJGLException(t);
        }
    }

    public static synchronized void loadOpenGLLibrary() throws LWJGLException {
        try {
            GL.create();
        } catch (IllegalStateException ignored) {
            // Already initialized.
        } catch (Throwable t) {
            throw new LWJGLException(t);
        }
    }

    public static synchronized void unloadOpenGLLibrary() {
        GL.destroy();
    }
}
