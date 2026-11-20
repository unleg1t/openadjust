package com.adjustclient.utilities.script.engine.compiler;

import java.util.regex.Pattern;

public final class CompilerDiagnosticPatterns {
    public static final Pattern DIAGNOSTIC;
    public static final Pattern SYMBOL;
    public static final Pattern LOCATION;
    public static final Pattern SUMMARY;

    static {
        DIAGNOSTIC = Pattern.compile("^.+: line (\\d+): (error|warning): (.*)$");
        SYMBOL = Pattern.compile("(?m)^\\s*symbol:\\s+(variable|method|class)\\s+(.+)$");
        LOCATION = Pattern.compile("(?m)^\\s*location:\\s+(?:variable .+ of type |class |interface |enum )(.+)$");
        SUMMARY = Pattern.compile("\\d+ (?:error|warning)s?");
    }
}
