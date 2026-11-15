package com.adjustclient.ui.account.login.auth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

import com.adjustclient.auth.LoginResult;
import com.adjustclient.auth.LoginStatus;
import com.adjustclient.auth.MicrosoftAuth;
import com.adjustclient.auth.MinecraftProfile;
import com.adjustclient.util.HttpClients;
import com.adjustclient.ui.account.login.ViewAccountButton;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountType;

public final class SessionTokenAccount extends AccountEntry {

    private Object field72;

    public SessionTokenAccount(String username, String playerId, String token, boolean flag) {
        this.username = username;
        this.token = token;
        setUuid(playerId);
    }

    // Local persistence allocation.
    public SessionTokenAccount() { }

    /**
     * Validates the stored token against the Minecraft services profile endpoint and builds the login result.
     * HTTP status &gt;= 400 means no profile: the result still carries a session with only the token, with status NO_PROFILE when the
     * token owns the game ({@code MicrosoftAuth.ownsMinecraft}) and NO_MINECRAFT otherwise. Connection problems give FAILED.
     */
    public LoginResult requestSession() {
        try (HttpClient client = HttpClients.create()) {
            String token = this.token.replace("\n", "").trim();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(5L))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                if (MicrosoftAuth.ownsMinecraft(token)) {
                    return new LoginResult(new Session(null, null, token, "mojang"), LoginStatus.NO_PROFILE);
                }
                return new LoginResult(new Session(null, null, token, "mojang"), LoginStatus.NO_MINECRAFT);
            }
            MinecraftProfile profile = new Gson().fromJson(response.body(), MinecraftProfile.class);
            return new LoginResult(new Session(profile.name, profile.id, token, "mojang"), LoginStatus.SUCCESS);
        } catch (IOException | InterruptedException e) {
            return new LoginResult(LoginStatus.FAILED);
        }
    }

    /** Session accounts persist nothing: any non-empty blob is an error. */
    @Override
    public void deserialize(byte[] data) throws IOException {
        if (data.length != 0) {
            throw new IOException(data.length + " != 0 xs");
        }
    }

    @Override
    public AccountType getType() {
        return AccountType.SESSION;
    }

    /**
     * Uses the stored data when a username or a UUID is known; with neither it asks the profile endpoint
     * ({@link #requestSession()}).
     */
    @Override
    public LoginResult login() {
        if (this.username.isEmpty() && this.uuid.isEmpty()) {
            return requestSession();
        }
        return new LoginResult(new Session(this.username, this.uuid, this.token, "mojang"), LoginStatus.SUCCESS);
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    protected void initButtons() {
        if (this.token != null && !this.token.isEmpty()) {
            super.initButtons();
            return;
        }
        this.buttons = List.of(this.deleteButton,
                new ViewAccountButton(this, new ResourceLocation("adjust", "textures/account/view.png")));
    }
}
