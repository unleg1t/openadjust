package com.adjustclient.ui.account.login.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;

import com.adjustclient.auth.LoginResult;
import com.adjustclient.auth.LoginStatus;
import com.adjustclient.auth.MicrosoftAuth;
import com.adjustclient.auth.MinecraftProfile;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountType;
import com.adjustclient.ui.hud.notification.NotificationBuilder;

public final class MicrosoftRefreshTokenAccount extends AccountEntry {

    // ---- static state of the local browser-login server (shared with BrowserLoginRequestHandler) ----

    /** Netty boss group of the running local HTTP server (null when stopped). */
    private static NioEventLoopGroup bossGroup;
    /** Netty worker group of the running local HTTP server (null when stopped). */
    private static NioEventLoopGroup workerGroup;
    static Consumer<String> refreshTokenCallback;
    /** Port the local HTTP server is bound to (0 = not running). */
    static int port;

    private static final String CLIENT_SECRET = "G.F8Q~x.uPo1CEz5I2CRpkx-KkVjNn1rNVM1ScYg";

    // ---- instance fields (own) ----

    /** true: imported refresh token of the official launcher application (serialised with the "NOT_BROWSER@@" prefix). */
    private boolean notBrowser;
    /** OAuth refresh token. */
    private String refreshToken;
    private Object field76;

    public MicrosoftRefreshTokenAccount(String refreshToken, boolean notBrowser, boolean flag) {
        this.refreshToken = refreshToken;
        this.notBrowser = notBrowser;
    }

    // Local persistence allocation; credentials are populated by deserialize.
    public MicrosoftRefreshTokenAccount() { }

    /** Stops the local browser-login HTTP server if it is running and clears the static state. */
    public static void stopServer() {
        if (bossGroup == null) {
            return;
        }
        Logger.log(LogLevel.INFO, true, "Ended HTTP browser server running on port {}", new Object[] { port });
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup = null;
        refreshTokenCallback = null;
    }

    /** Reads the persisted blob: one modified-UTF string; the prefix "NOT_BROWSER@@" marks an imported (non browser) token. */
    @Override
    public void deserialize(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        this.refreshToken = in.readUTF();
        if (this.refreshToken.startsWith("NOT_BROWSER@@")) {
            this.notBrowser = true;
            this.refreshToken = this.refreshToken.substring(13);
        }
    }

    @Override
    public AccountType getType() {
        return AccountType.BROWSER;
    }

    /**
     * Performs the refresh-token login chain and returns the resulting session/status.
     * Every HTTP call goes through the shared helper {@code MicrosoftAuth.post(url, body, json)} which returns null on failure.
     */
    @Override
    public LoginResult login() {
        try {
            String msaResponse;
            String xblResponse;
            String msaAccessToken;
            if (this.notBrowser) {
                msaResponse = MicrosoftAuth.post("https://login.live.com/oauth20_token.srf",
                        "client_id=00000000402b5328&refresh_token=" + this.refreshToken
                                + "&grant_type=refresh_token&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL",
                        false);
                if (msaResponse == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
                JsonObject msa = JsonParser.parseString(msaResponse).getAsJsonObject();
                msaAccessToken = msa.get("access_token").getAsString();
                this.refreshToken = msa.get("refresh_token").getAsString();
                xblResponse = MicrosoftAuth.post("https://user.auth.xboxlive.com/user/authenticate",
                        "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"t=" + msaAccessToken
                                + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}",
                        true);
                if (xblResponse == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
            } else {
                msaResponse = MicrosoftAuth.post("https://login.live.com/oauth20_token.srf",
                        "client_id=c4f0db78-5015-4a94-8b34-1a4da87b9ce4&client_secret=" + CLIENT_SECRET + "&refresh_token=" + this.refreshToken
                                + "&grant_type=refresh_token&redirect_uri=http://localhost:" + port,
                        false);
                if (msaResponse == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
                JsonObject msa = JsonParser.parseString(msaResponse).getAsJsonObject();
                msaAccessToken = msa.get("access_token").getAsString();
                this.refreshToken = msa.get("refresh_token").getAsString();
                xblResponse = MicrosoftAuth.post("https://user.auth.xboxlive.com/user/authenticate",
                        "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + msaAccessToken
                                + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}",
                        true);
                if (xblResponse == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
            }
            String xblToken = JsonParser.parseString(xblResponse).getAsJsonObject().get("Token").getAsString();
            // user hash: text between "uhs":" and the next quote of the raw Xbox Live response
            String userHash = xblResponse.split("\"uhs\":\"")[1].split("\"")[0];

            String xstsResponse = MicrosoftAuth.post("https://xsts.auth.xboxlive.com/xsts/authorize",
                    "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblToken
                            + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}",
                    true);
            if (xstsResponse == null) {
                return new LoginResult(LoginStatus.FAILED);
            }
            String xstsToken = JsonParser.parseString(xstsResponse).getAsJsonObject().get("Token").getAsString();

            String mcResponse = MicrosoftAuth.post("https://api.minecraftservices.com/authentication/login_with_xbox",
                    "{\"identityToken\":\"XBL3.0 x=" + userHash + ";" + xstsToken + "\"}", true);
            if (mcResponse == null) {
                return new LoginResult(LoginStatus.FAILED);
            }
            JsonObject mc = JsonParser.parseString(mcResponse).getAsJsonObject();
            if (!mc.has("access_token")) {
                return new LoginResult(null, LoginStatus.VALID_RATE_LIMITED);
            }
            String mcAccessToken = mc.get("access_token").getAsString();

            HttpURLConnection connection = (HttpURLConnection) URI.create("https://api.minecraftservices.com/minecraft/profile").toURL().openConnection();
            try {
                connection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                MinecraftProfile profile = new Gson().fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), MinecraftProfile.class);
                return new LoginResult(new Session(profile.name, profile.id, mcAccessToken, "mojang"), LoginStatus.SUCCESS);
            } catch (FileNotFoundException e) {
                // profile endpoint answered 404: no profile yet
                Logger.debugStackTrace(e, "FileNotFound");
                if (MicrosoftAuth.ownsMinecraft(mcAccessToken)) {
                    return new LoginResult(new Session(null, null, mcAccessToken, "mojang"), LoginStatus.NO_PROFILE);
                }
                return new LoginResult(new Session(null, null, mcAccessToken, "mojang"), LoginStatus.NO_MINECRAFT);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            Logger.debugStackTrace(e, null);
            return new LoginResult(LoginStatus.FAILED);
        }
    }

    /** Persisted form: one modified-UTF string, "NOT_BROWSER@@" + token for imported tokens, the bare token otherwise. */
    @Override
    public byte[] serialize() {
        String value = this.notBrowser ? "NOT_BROWSER@@" + this.refreshToken : this.refreshToken;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); // initial 32 bytes
        try {
            new DataOutputStream(bytes).writeUTF(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes.toByteArray();
    }

    public static String startServer(Consumer<String> callback) {
        boolean running = bossGroup != null && workerGroup != null;
        if (!running) {
            try {
                refreshTokenCallback = callback;
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(new BrowserLoginChannelInitializer());
                Channel channel = bootstrap.bind(new InetSocketAddress(0)).sync().channel();
                port = ((InetSocketAddress) channel.localAddress()).getPort();
            } catch (Exception e) {
                stopServer();
                Logger.log(LogLevel.ERROR, true, "Unable to start HTTP browser server: {}", new Object[] { e });
                Logger.debugStackTrace(e, null);
                new NotificationBuilder("Browser Account").format("Unable to start HTTP server: {}", e).show(); // format(...).show()
            }
        }
        if (port < 1) {
            return null;
        }
        Logger.log(LogLevel.INFO, true, (running ? "Running" : "Starting") + " HTTP browser server on port {}", new Object[] { port });
        return "https://login.live.com/oauth20_authorize.srf?client_id=c4f0db78-5015-4a94-8b34-1a4da87b9ce4&client_secret=" + CLIENT_SECRET
                + "&response_type=code&redirect_uri=http://localhost:" + port + "&scope=XboxLive.signin%20offline_access";
    }
}
