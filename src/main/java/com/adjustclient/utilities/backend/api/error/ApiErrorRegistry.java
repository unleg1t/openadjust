package com.adjustclient.utilities.backend.api.error;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ApiErrorRegistry {
    public static final Map<Integer, Enum<?>[]> errors;
    static {
        errors = new HashMap<>();
        errors.put(0, RepositoryInviteError.values());
        errors.put(1, ConfigurationError.values());
        errors.put(2, MessageError.values());
        errors.put(3, AccountLoginError.values());
        errors.put(4, FriendsError.values());
    }

    public static Enum<?> resolveError(int family, int ordinal) {
        Enum<?>[] values = Objects.requireNonNull(errors.get(family), "No shared enum: " + family);
        return values[ordinal];
    }
}
