package com.adjustclient.core.gui.account;

import java.io.File;

public class DroppedCookieFile {
    public String name;
    public byte[] contents;
    public File directory;
    public Object field16;

    public DroppedCookieFile(String name, byte[] contents, File directory) {
        this.name = name;
        this.contents = contents;
        this.directory = directory;
    }
}
