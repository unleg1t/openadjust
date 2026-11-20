package com.adjustclient.utilities.script.engine.type;

public class ClassTypeMirror<T> extends AbstractScriptType<T> {
    public final String className;
    public final String descriptor;
    public final ClassTypeMirror<?> superType;
    public final Class<?> hostType;

    public ClassTypeMirror(String className, ClassTypeMirror<?> superType, Class<?> hostType) {
        this.className = className;
        this.descriptor = "L" + className.replace('.', '/') + ";";
        this.superType = superType;
        this.hostType = hostType;
    }
}
