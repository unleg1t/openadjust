package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;

public final class ApolloLunarClientVersion implements ProtoMessage {
    private final String gitBranch;
    private final String gitCommit;
    private final String semver;
    private final Object field16 = null;

    public ApolloLunarClientVersion(String gitBranch, String gitCommit, String semver) {
        this.gitBranch = gitBranch;
        this.gitCommit = gitCommit;
        this.semver = semver;
    }

    public String getGitBranch() { return gitBranch; }
    public String getGitCommit() { return gitCommit; }
    public String getSemver() { return semver; }

    public static ApolloLunarClientVersion parse(ProtoReader in) {
        String gitBranch = "";
        String gitCommit = "";
        String semver = "";
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10: gitBranch = in.readString(); break;
                case 18: gitCommit = in.readString(); break;
                case 26: semver = in.readString(); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloLunarClientVersion(gitBranch, gitCommit, semver);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (!gitBranch.isEmpty()) out.writeString(1, gitBranch);
        if (!gitCommit.isEmpty()) out.writeString(2, gitCommit);
        if (!semver.isEmpty()) out.writeString(3, semver);
    }
}
