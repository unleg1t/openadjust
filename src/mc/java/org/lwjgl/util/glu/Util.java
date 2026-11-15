package org.lwjgl.util.glu;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Util {
    private static IntBuffer scratch = BufferUtils.createIntBuffer(16);

    protected static int ceil(int a, int b) {
        return (a % b == 0) ? (a / b) : (a / b + 1);
    }

    protected static float[] normalize(float[] v) {
        float r = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (r == 0.0) {
            return v;
        }
        r = 1.0f / r;
        v[0] *= r;
        v[1] *= r;
        v[2] *= r;
        return v;
    }

    protected static void cross(float[] v1, float[] v2, float[] result) {
        result[0] = v1[1] * v2[2] - v1[2] * v2[1];
        result[1] = v1[2] * v2[0] - v1[0] * v2[2];
        result[2] = v1[0] * v2[1] - v1[1] * v2[0];
    }

    protected static int compPerPix(int format) {
        switch (format) {
            case GL11.GL_COLOR_INDEX:
            case GL11.GL_STENCIL_INDEX:
            case GL11.GL_DEPTH_COMPONENT:
            case GL11.GL_RED:
            case GL11.GL_GREEN:
            case GL11.GL_BLUE:
            case GL11.GL_ALPHA:
            case GL11.GL_LUMINANCE:
                return 1;
            case GL11.GL_LUMINANCE_ALPHA:
                return 2;
            case GL11.GL_RGB:
            case 32992: // GL_BGR
                return 3;
            case GL11.GL_RGBA:
            case 32993: // GL_BGRA
                return 4;
            default:
                return -1;
        }
    }

    protected static int nearestPower(int value) {
        int i = 1;
        if (value == 0) {
            return -1;
        }
        while (true) {
            if (value == 1) {
                return i;
            } else if (value == 3) {
                return i << 2;
            }
            value >>>= 1;
            i <<= 1;
        }
    }

    protected static int bytesPerPixel(int format, int type) {
        int n;
        int m;
        switch (format) {
            case GL11.GL_COLOR_INDEX:
            case GL11.GL_STENCIL_INDEX:
            case GL11.GL_DEPTH_COMPONENT:
            case GL11.GL_RED:
            case GL11.GL_GREEN:
            case GL11.GL_BLUE:
            case GL11.GL_ALPHA:
            case GL11.GL_LUMINANCE:
                n = 1;
                break;
            case GL11.GL_LUMINANCE_ALPHA:
                n = 2;
                break;
            case GL11.GL_RGB:
            case 32992: // GL_BGR
                n = 3;
                break;
            case GL11.GL_RGBA:
            case 32993: // GL_BGRA
                n = 4;
                break;
            default:
                n = 0;
        }
        switch (type) {
            case GL11.GL_UNSIGNED_BYTE:
            case GL11.GL_BYTE:
            case GL11.GL_BITMAP:
                m = 1;
                break;
            case GL11.GL_UNSIGNED_SHORT:
            case GL11.GL_SHORT:
                m = 2;
                break;
            case GL11.GL_UNSIGNED_INT:
            case GL11.GL_INT:
            case GL11.GL_FLOAT:
                m = 4;
                break;
            default:
                m = 0;
        }
        return n * m;
    }

    protected static int glGetIntegerv(int what) {
        scratch.rewind();
        GL11.glGetIntegerv(what, scratch);
        return scratch.get();
    }
}
