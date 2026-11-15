package org.lwjgl.opengl;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

public final class ContextAttribs {
    public static final int CONTEXT_MAJOR_VERSION_ARB = 0x2091;
    public static final int CONTEXT_MINOR_VERSION_ARB = 0x2092;
    public static final int CONTEXT_PROFILE_MASK_ARB = 0x9126;
    public static final int CONTEXT_CORE_PROFILE_BIT_ARB = 0x0001;
    public static final int CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB = 0x0002;
    public static final int CONTEXT_ES2_PROFILE_BIT_EXT = 0x0004;
    public static final int CONTEXT_FLAGS_ARB = 0x2094;
    public static final int CONTEXT_DEBUG_BIT_ARB = 0x0001;
    public static final int CONTEXT_FORWARD_COMPATIBLE_BIT_ARB = 0x0002;
    public static final int CONTEXT_ROBUST_ACCESS_BIT_ARB = 0x0004;
    public static final int CONTEXT_RESET_ISOLATION_BIT_ARB = 0x0008;
    public static final int CONTEXT_RESET_NOTIFICATION_STRATEGY_ARB = 0x8256;
    public static final int NO_RESET_NOTIFICATION_ARB = 0x8261;
    public static final int LOSE_CONTEXT_ON_RESET_ARB = 0x8252;
    public static final int CONTEXT_RELEASE_BEHABIOR_ARB = 0x2097;
    public static final int CONTEXT_RELEASE_BEHAVIOR_NONE_ARB = 0x0000;
    public static final int CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB = 0x2098;
    public static final int CONTEXT_LAYER_PLANE_ARB = 0x2093;

    private final int majorVersion;
    private final int minorVersion;
    private final int profileMask;
    private final int contextFlags;
    private final int contextResetNotificationStrategy;
    private final int contextReleaseBehavior;
    private final int layerPlane;

    public ContextAttribs() {
        this(1, 0);
    }

    public ContextAttribs(int majorVersion, int minorVersion) {
        this(majorVersion, minorVersion, 0, 0);
    }

    public ContextAttribs(int majorVersion, int minorVersion, int profileMask) {
        this(majorVersion, minorVersion, 0, profileMask);
    }

    public ContextAttribs(int majorVersion, int minorVersion, int profileMask, int contextFlags) {
        this(majorVersion, minorVersion, profileMask, contextFlags, NO_RESET_NOTIFICATION_ARB, CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB, 0);
    }

    private ContextAttribs(
            int majorVersion,
            int minorVersion,
            int profileMask,
            int contextFlags,
            int contextResetNotificationStrategy,
            int contextReleaseBehavior,
            int layerPlane) {
        if (majorVersion < 0 || 4 < majorVersion || minorVersion < 0
                || (majorVersion == 4 && 5 < minorVersion)
                || (majorVersion == 3 && 3 < minorVersion)
                || (majorVersion == 2 && 1 < minorVersion)
                || (majorVersion == 1 && 5 < minorVersion)) {
            throw new IllegalArgumentException("Invalid OpenGL version specified: " + majorVersion + '.' + minorVersion);
        }
        if (Integer.bitCount(profileMask) > 1 || profileMask < 0 || profileMask > 4) {
            throw new IllegalArgumentException("Invalid profile mask specified: " + Integer.toBinaryString(profileMask));
        }
        if (contextFlags < 0 || contextFlags > 15) {
            throw new IllegalArgumentException("Invalid context flags specified: " + Integer.toBinaryString(contextFlags));
        }
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.profileMask = profileMask;
        this.contextFlags = contextFlags;
        this.contextResetNotificationStrategy = contextResetNotificationStrategy;
        this.contextReleaseBehavior = contextReleaseBehavior;
        this.layerPlane = layerPlane;
    }

    private ContextAttribs(ContextAttribs attribs) {
        this.majorVersion = attribs.majorVersion;
        this.minorVersion = attribs.minorVersion;
        this.profileMask = attribs.profileMask;
        this.contextFlags = attribs.contextFlags;
        this.contextResetNotificationStrategy = attribs.contextResetNotificationStrategy;
        this.contextReleaseBehavior = attribs.contextReleaseBehavior;
        this.layerPlane = attribs.layerPlane;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getProfileMask() {
        return profileMask;
    }

    private boolean hasMask(int mask) {
        return profileMask == mask;
    }

    public boolean isProfileCore() {
        return hasMask(CONTEXT_CORE_PROFILE_BIT_ARB);
    }

    public boolean isProfileCompatibility() {
        return hasMask(CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB);
    }

    public boolean isProfileES() {
        return hasMask(CONTEXT_ES2_PROFILE_BIT_EXT);
    }

    public int getContextFlags() {
        return contextFlags;
    }

    private boolean hasFlag(int flag) {
        return (contextFlags & flag) != 0;
    }

    public boolean isDebug() {
        return hasFlag(CONTEXT_DEBUG_BIT_ARB);
    }

    public boolean isForwardCompatible() {
        return hasFlag(CONTEXT_FORWARD_COMPATIBLE_BIT_ARB);
    }

    public boolean isRobustAccess() {
        return hasFlag(CONTEXT_ROBUST_ACCESS_BIT_ARB);
    }

    public boolean isContextResetIsolation() {
        return hasFlag(CONTEXT_RESET_ISOLATION_BIT_ARB);
    }

    public int getContextResetNotificationStrategy() {
        return contextResetNotificationStrategy;
    }

    public boolean isLoseContextOnReset() {
        return contextResetNotificationStrategy == LOSE_CONTEXT_ON_RESET_ARB;
    }

    public int getContextReleaseBehavior() {
        return contextReleaseBehavior;
    }

    public int getLayerPlane() {
        return layerPlane;
    }

    private ContextAttribs withProfile(int mask, boolean enabled) {
        int next = enabled ? mask : 0;
        if (next == profileMask) {
            return this;
        }
        return new ContextAttribs(majorVersion, minorVersion, next, contextFlags, contextResetNotificationStrategy, contextReleaseBehavior, layerPlane);
    }

    private ContextAttribs withFlag(int flag, boolean enabled) {
        int next = enabled ? (contextFlags | flag) : (contextFlags & ~flag);
        if (next == contextFlags) {
            return this;
        }
        return new ContextAttribs(majorVersion, minorVersion, profileMask, next, contextResetNotificationStrategy, contextReleaseBehavior, layerPlane);
    }

    public ContextAttribs withProfileCore(boolean profileCore) {
        if (majorVersion < 3 || (majorVersion == 3 && minorVersion < 2)) {
            throw new IllegalArgumentException("Profiles are only supported on OpenGL version 3.2 or higher.");
        }
        return withProfile(CONTEXT_CORE_PROFILE_BIT_ARB, profileCore);
    }

    public ContextAttribs withProfileCompatibility(boolean profileCompatibility) {
        if (majorVersion < 3 || (majorVersion == 3 && minorVersion < 2)) {
            throw new IllegalArgumentException("Profiles are only supported on OpenGL version 3.2 or higher.");
        }
        return withProfile(CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB, profileCompatibility);
    }

    public ContextAttribs withProfileES(boolean profileES) {
        if (!(majorVersion == 2 && minorVersion == 0)) {
            throw new IllegalArgumentException("The OpenGL ES profile is only supported for OpenGL version 2.0.");
        }
        return withProfile(CONTEXT_ES2_PROFILE_BIT_EXT, profileES);
    }

    public ContextAttribs withDebug(boolean debug) {
        return withFlag(CONTEXT_DEBUG_BIT_ARB, debug);
    }

    public ContextAttribs withForwardCompatible(boolean forwardCompatible) {
        return withFlag(CONTEXT_FORWARD_COMPATIBLE_BIT_ARB, forwardCompatible);
    }

    public ContextAttribs withRobustAccess(boolean robustAccess) {
        return withFlag(CONTEXT_ROBUST_ACCESS_BIT_ARB, robustAccess);
    }

    public ContextAttribs withContextResetIsolation(boolean contextResetIsolation) {
        return withFlag(CONTEXT_RESET_ISOLATION_BIT_ARB, contextResetIsolation);
    }

    public ContextAttribs withResetNotificationStrategy(int strategy) {
        if (strategy == contextResetNotificationStrategy) {
            return this;
        }
        if (strategy != NO_RESET_NOTIFICATION_ARB && strategy != LOSE_CONTEXT_ON_RESET_ARB) {
            throw new IllegalArgumentException("Invalid reset notification strategy specified: 0x" + Integer.toHexString(strategy));
        }
        return new ContextAttribs(majorVersion, minorVersion, profileMask, contextFlags, strategy, contextReleaseBehavior, layerPlane);
    }

    public ContextAttribs withLoseContextOnReset(boolean loseContextOnReset) {
        return withResetNotificationStrategy(loseContextOnReset ? LOSE_CONTEXT_ON_RESET_ARB : NO_RESET_NOTIFICATION_ARB);
    }

    public ContextAttribs withContextReleaseBehavior(int behavior) {
        if (behavior == contextReleaseBehavior) {
            return this;
        }
        if (behavior != CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB && behavior != CONTEXT_RELEASE_BEHAVIOR_NONE_ARB) {
            throw new IllegalArgumentException("Invalid context release behavior specified: 0x" + Integer.toHexString(behavior));
        }
        return new ContextAttribs(majorVersion, minorVersion, profileMask, contextFlags, contextResetNotificationStrategy, behavior, layerPlane);
    }

    public ContextAttribs withLayer(int layerPlane) {
        if (layerPlane < 0) {
            throw new IllegalArgumentException("Invalid layer plane specified: " + layerPlane);
        }
        if (this.layerPlane == layerPlane) {
            return this;
        }
        return new ContextAttribs(majorVersion, minorVersion, profileMask, contextFlags, contextResetNotificationStrategy, contextReleaseBehavior, layerPlane);
    }

    IntBuffer getAttribList() {
        int count = 0;
        if (!(majorVersion == 1 && minorVersion == 0)) {
            count += 4;
        }
        if (contextFlags != 0) {
            count += 2;
        }
        if (profileMask != 0) {
            count += 2;
        }
        if (contextResetNotificationStrategy != NO_RESET_NOTIFICATION_ARB) {
            count += 2;
        }
        if (contextReleaseBehavior != CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB) {
            count += 2;
        }
        if (layerPlane != 0) {
            count += 2;
        }
        if (count == 0) {
            return null;
        }
        IntBuffer attribs = BufferUtils.createIntBuffer(count + 1);
        if (!(majorVersion == 1 && minorVersion == 0)) {
            attribs.put(CONTEXT_MAJOR_VERSION_ARB).put(majorVersion);
            attribs.put(CONTEXT_MINOR_VERSION_ARB).put(minorVersion);
        }
        if (contextFlags != 0) {
            attribs.put(CONTEXT_FLAGS_ARB).put(contextFlags);
        }
        if (profileMask != 0) {
            attribs.put(CONTEXT_PROFILE_MASK_ARB).put(profileMask);
        }
        if (contextResetNotificationStrategy != NO_RESET_NOTIFICATION_ARB) {
            attribs.put(CONTEXT_RESET_NOTIFICATION_STRATEGY_ARB).put(contextResetNotificationStrategy);
        }
        if (contextReleaseBehavior != CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB) {
            attribs.put(CONTEXT_RELEASE_BEHABIOR_ARB).put(contextReleaseBehavior);
        }
        if (layerPlane != 0) {
            attribs.put(CONTEXT_LAYER_PLANE_ARB).put(layerPlane);
        }
        attribs.put(0);
        attribs.rewind();
        return attribs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("ContextAttribs:");
        sb.append(" Version=").append(majorVersion).append('.').append(minorVersion);
        if (profileMask != 0) {
            sb.append(", Profile=");
            if (hasMask(CONTEXT_CORE_PROFILE_BIT_ARB)) {
                sb.append("CORE");
            } else if (hasMask(CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB)) {
                sb.append("COMPATIBLITY");
            } else if (hasMask(CONTEXT_ES2_PROFILE_BIT_EXT)) {
                sb.append("ES2");
            } else {
                sb.append("*unknown*");
            }
        }
        if (contextFlags != 0) {
            if (hasFlag(CONTEXT_DEBUG_BIT_ARB)) {
                sb.append(", DEBUG");
            }
            if (hasFlag(CONTEXT_FORWARD_COMPATIBLE_BIT_ARB)) {
                sb.append(", FORWARD_COMPATIBLE");
            }
            if (hasFlag(CONTEXT_ROBUST_ACCESS_BIT_ARB)) {
                sb.append(", ROBUST_ACCESS");
            }
            if (hasFlag(CONTEXT_RESET_ISOLATION_BIT_ARB)) {
                sb.append(", RESET_ISOLATION");
            }
        }
        if (contextResetNotificationStrategy != NO_RESET_NOTIFICATION_ARB) {
            sb.append(", LOSE_CONTEXT_ON_RESET");
        }
        if (contextReleaseBehavior != CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB) {
            sb.append(", RELEASE_BEHAVIOR_NONE");
        }
        if (layerPlane != 0) {
            sb.append(", Layer=").append(layerPlane);
        }
        return sb.toString();
    }
}
