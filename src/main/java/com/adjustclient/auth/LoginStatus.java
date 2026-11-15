package com.adjustclient.auth;

public enum LoginStatus {
    SUCCESS("Successful", "Successful login."),
    NO_PROFILE("Valid, No Minecraft Profile", "his account doesn't have a Minecraft profile."),
    NO_MINECRAFT("No Minecraft", "This account doesn't own Minecraft."),
    ABUSE_LOCKED("Abuse Locked", "This account is permanently locked."),
    RECOVER_LOCKED("Recovery Locked", "This account is locked for recovery."),
    FAILED("Invalid Account", "Failed to login."),
    RATE_LIMITED("Rate Limited", "You are sending too many requests, try again later."),
    VALID_RATE_LIMITED("Valid, Rate Limited", "This account is valid but you are sending too many requests, try again later.");

    public final String message;
    public final String description;
    LoginStatus(String message, String description) { this.message = message; this.description = description; }
}
