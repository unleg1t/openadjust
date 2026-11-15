package net.minecraft.client.renderer;

import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class JomlProjection
{
    public static final Matrix4f MATRIX = new Matrix4f();
    public static final Vector3f RESULT = new Vector3f();

    private JomlProjection()
    {
    }

    public static boolean project(IntBuffer viewport, float x, float y, float z)
    {
        return project(viewport, x, y, z, RESULT);
    }

    public static boolean project(IntBuffer viewport, float x, float y, float z, Vector3f dest)
    {
        if (viewport == null || dest == null)
        {
            return false;
        }

        Vector4f clip = MATRIX.transform(new Vector4f(x, y, z, 1.0F));

        if (clip.w <= 0.0F)
        {
            return false;
        }

        float invW = 1.0F / clip.w;
        float ndcX = clip.x * invW;
        float ndcY = clip.y * invW;
        float ndcZ = clip.z * invW;
        int base = viewport.position();
        float vx = (float)viewport.get(base);
        float vy = (float)viewport.get(base + 1);
        float vw = (float)viewport.get(base + 2);
        float vh = (float)viewport.get(base + 3);
        float winX = vx + vw * (ndcX * 0.5F + 0.5F);
        float winY = vy + vh * (ndcY * 0.5F + 0.5F);
        float winZ = ndcZ * 0.5F + 0.5F;
        dest.set(winX, winY, winZ);
        RESULT.set(dest);
        return true;
    }
}
