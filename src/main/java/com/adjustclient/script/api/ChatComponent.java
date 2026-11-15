package com.adjustclient.script.api;

public final class ChatComponent {
    public static native ChatComponent text(String var0);

    public static native ChatComponent fromJson(String var0);

    public native String toJson();

    public native String getUnformattedText();

    public native String getFormattedText();

    public native ChatComponent appendText(String var1);

    public native ChatComponent appendSibling(ChatComponent var1);

    public native ChatComponent createCopy();
}

