package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.UserInputVerificationEvent;

public final class UserInputVerificationEventType extends ScriptEventType<UserInputVerificationEvent> {
    public static final UserInputVerificationEventType INSTANCE;

    static {
        INSTANCE = new UserInputVerificationEventType();
    }

    private UserInputVerificationEventType() {
        super("UserInputVerificationEvent", null, UserInputVerificationEvent.class);
    }
}
