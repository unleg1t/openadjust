package com.adjustclient.utilities.backend.transport;

import java.util.Objects;

public record RepositoryInfo(
        String repositoryName,
        long repositoryId,
        long lastAccessTime,
        int accountCount,
        String[] users,
        Object[] permissions
) {
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof RepositoryInfo info)) return false;
        return permissions == info.permissions && users == info.users
                && accountCount == info.accountCount && lastAccessTime == info.lastAccessTime
                && repositoryId == info.repositoryId && Objects.equals(repositoryName, info.repositoryName);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hashCode(repositoryName);
        hash = 31 * hash + Long.hashCode(repositoryId);
        hash = 31 * hash + Long.hashCode(lastAccessTime);
        hash = 31 * hash + accountCount;
        hash = 31 * hash + Objects.hashCode(users);
        return 31 * hash + Objects.hashCode(permissions);
    }

    @Override
    public String toString() {
        return "a[repositoryName=" + repositoryName + ", repositoryId=" + repositoryId
                + ", lastAccessTime=" + lastAccessTime + ", accountCount=" + accountCount
                + ", users=" + users + ", permissions=" + permissions + "]";
    }
}
