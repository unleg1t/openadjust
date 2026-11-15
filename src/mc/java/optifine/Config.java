package optifine;

import shadersmod.client.Shaders;

public class Config {
    public static boolean isShaders() {
        return Shaders.shaderPackLoaded;
    }

    public static boolean isShowCapes() {
        return true;
    }
}
