package com.adjustclient.script.engine.compiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public final class ScriptSourceScanner {
    public Object field4;
    public static final Predicate<Path> REGULAR_FILE = path -> Files.isRegularFile(path);
    public static final Predicate<Path> JAVA_SOURCE = path -> path.getFileName().toString().toLowerCase().endsWith(".java");

    public static java.util.List<Path> b(Path dir) throws java.io.IOException {
        try (java.util.stream.Stream<Path> stream = Files.list(dir)) {
            return stream.filter(REGULAR_FILE).filter(JAVA_SOURCE).sorted().toList();
        }
    }
}
