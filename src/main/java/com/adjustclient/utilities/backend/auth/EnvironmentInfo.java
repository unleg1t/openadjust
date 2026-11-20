package com.adjustclient.utilities.backend.auth;

import java.io.File;

public class EnvironmentInfo {
    public static int userNameHash;
    public static int javaVersionHash;
    public static int assetsPathHash;

    public static void a() {
        userNameHash = c();
        javaVersionHash = d();
        assetsPathHash = b();
    }

    public static int b() {
        String path = new File("assets").getAbsolutePath();
        int len = path.length();
        if (len == 0) {
            return 368;
        }
        int hash = 368 * 31 + path.charAt(0);
        for (int i = 1; i < len; i++) {
            hash = 31 * hash + path.charAt(i);
        }
        return hash;
    }

    public static int c() {
        String name = System.getProperty("user.name");
        int len = name.length();
        if (len == 0) {
            return 812;
        }
        int hash = 812 * 31 + name.charAt(0);
        for (int i = 1; i < len; i++) {
            hash = 31 * hash + name.charAt(i);
        }
        return hash;
    }

    public static int d() {
        String version = System.getProperty("java.version");
        int len = version.length();
        if (len == 0) {
            return 192;
        }
        int hash = 192 * 31 + version.charAt(0);
        for (int i = 1; i < len; i++) {
            hash = 31 * hash + version.charAt(i);
        }
        return hash;
    }
}
