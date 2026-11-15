package com.adjustclient.script.engine.event;

import com.adjustclient.event.UserInputVerificationEvent;

public final class UserInputVerificationEventType extends ScriptEventType<UserInputVerificationEvent> {
    public static final UserInputVerificationEventType INSTANCE;

    static {
        INSTANCE = new UserInputVerificationEventType();
    }

    private UserInputVerificationEventType() {
        super("UserInputVerificationEvent", null, UserInputVerificationEvent.class);
    }
}
