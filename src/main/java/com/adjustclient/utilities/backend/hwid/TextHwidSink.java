package com.adjustclient.utilities.backend.hwid;

public class TextHwidSink implements HwidSink {
    public final StringBuilder builder;

    public TextHwidSink(StringBuilder builder) {
        this.builder = builder;
    }

    public TextHwidSink() {
        this(new StringBuilder());
    }

    @Override
    public void a(int value) {
        this.builder.append(value).append('\n');
    }

    @Override
    public void a(String value) {
        this.builder.append(value != null ? value : "(null)").append('\n');
    }

    @Override
    public void b(int code) {
        this.builder.append("(CRASH) ").append(code);
    }
}
