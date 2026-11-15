package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum AccountLoginError {
    SIZE_LIMIT("The account you tried to add is too big in size."),
    COUNT_LIMIT("You have reached the account limit for this repository.");
    public final String field16;
    AccountLoginError(String message) { this.field16 = message; }
}
