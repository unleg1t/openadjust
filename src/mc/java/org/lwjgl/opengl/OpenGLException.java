package org.lwjgl.opengl;

public class OpenGLException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OpenGLException(int glErrorCode) {
        this(createErrorMessage(glErrorCode));
    }

    public OpenGLException() {
    }

    public OpenGLException(String message) {
        super(message);
    }

    public OpenGLException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenGLException(Throwable cause) {
        super(cause);
    }

    private static String createErrorMessage(int glErrorCode) {
        return Util.translateGLErrorString(glErrorCode) + " (" + glErrorCode + ")";
    }
}
