package com.adjustclient.ui.account.login.auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;

import com.adjustclient.auth.LoginResult;
import com.adjustclient.auth.LoginStatus;
import com.adjustclient.auth.MicrosoftAuth;
import com.adjustclient.auth.MinecraftProfile;
import com.adjustclient.logging.Logger;
import com.adjustclient.util.ByteUtil;
import com.adjustclient.util.StringUtil;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountType;

public final class MicrosoftCredentialAccount extends AccountEntry {

    /** E-mail address. */
    private String email;
    /** Password. */
    private String password;
    private Object field80;

    public MicrosoftCredentialAccount(String email, String password, boolean flag) {
        this.email = email;
        this.password = password;
    }

    // Local persistence allocation; populated by deserialize.
    public MicrosoftCredentialAccount() { }

    /** Reads the persisted blob into e-mail and password. */
    @Override
    public void deserialize(byte[] data) throws IOException {
        String[] parts = decode(data);
        this.email = parts[0];
        this.password = parts[1];
    }

    @Override
    public AccountType getType() {
        return AccountType.CREDENTIALS;
    }

    @Override
    public LoginResult login() {
        return login(this.email, this.password);
    }

    /** Persisted form: {@code [int emailLength][int passwordLength]} (big endian) followed by both strings as UTF-16BE. */
    @Override
    public byte[] serialize() {
        byte[] emailBytes = this.email.getBytes(StandardCharsets.UTF_16BE);
        byte[] passwordBytes = this.password.getBytes(StandardCharsets.UTF_16BE);
        byte[] out = new byte[emailBytes.length + passwordBytes.length + 8];
        out[0] = (byte) (emailBytes.length >>> 24);
        out[1] = (byte) (emailBytes.length >>> 16);
        out[2] = (byte) (emailBytes.length >>> 8);
        out[3] = (byte) emailBytes.length;
        out[4] = (byte) (passwordBytes.length >>> 24);
        out[5] = (byte) (passwordBytes.length >>> 16);
        out[6] = (byte) (passwordBytes.length >>> 8);
        out[7] = (byte) passwordBytes.length;
        System.arraycopy(emailBytes, 0, out, 8, emailBytes.length);
        System.arraycopy(passwordBytes, 0, out, 8 + emailBytes.length, passwordBytes.length);
        return out;
    }

    /** Parses the persisted form into {email, password}. */
    public static String[] decode(byte[] data) throws IOException {
        int emailLength = ByteUtil.readInt(data, 0);
        int passwordLength = ByteUtil.readInt(data, 4);
        if (data.length != emailLength + passwordLength + 8) {
            throw new IOException(emailLength + " " + passwordLength + " != " + data.length);
        }
        String email = new String(data, 8, emailLength, StandardCharsets.UTF_16BE);
        String password = new String(data, 8 + emailLength, passwordLength, StandardCharsets.UTF_16BE);
        return new String[] { email, password };
    }

    /** Returns the first match of {@code regex} in {@code text} (whole match, group 0) or null when there is none. */
    private static String extract(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group();
    }

    /**
     * The e-mail/password login chain (see class comment). All HTTP requests use connect/read timeouts of 5000 ms.
     * Returns {@link LoginStatus#FAILED} for every page-scraping failure.
     */
    public static LoginResult login(String email, String password) {
        CookieHandler previousCookieHandler = CookieHandler.getDefault();
        CookieHandler.setDefault(new CookieManager());
        try {
            // step 1: authorize page -> PPFT value and POST target
            String userAgent = "Mozilla/5.0 (XboxReplay; XboxLiveAuth/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
            HttpURLConnection authorize = (HttpURLConnection) URI.create("https://login.live.com/oauth20_authorize.srf?client_id=00000000402B5328&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=token&locale=en").toURL().openConnection();
            authorize.setRequestProperty("User-Agent", userAgent);
            authorize.setConnectTimeout(5000);
            authorize.setReadTimeout(5000);
            authorize.connect();
            String page = IOUtils.toString(authorize.getInputStream(), StandardCharsets.UTF_8);
            String ppft = StringUtil.firstGroup("value=\\\\\\\"(.+?)\\\\\\\"", page);
            if (ppft == null) {
                ppft = StringUtil.firstGroup("value=\"(.+?)\"", page);
            }
            String urlPost = StringUtil.firstGroup("\"urlPost\":\"(.+?)\"", page);
            if (urlPost == null) {
                urlPost = StringUtil.firstGroup("urlPost:'(.+?)'", page);
            }
            if (ppft == null || urlPost == null) {
                CookieHandler.setDefault(previousCookieHandler);
                return new LoginResult(LoginStatus.FAILED);
            }

            // step 2: POST the credentials
            String loginBody = "loginfmt=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&passwd=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
                    + "&PPFT=" + URLEncoder.encode(ppft, StandardCharsets.UTF_8) + "&login=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            HttpURLConnection post = (HttpURLConnection) URI.create(urlPost).toURL().openConnection();
            post.setRequestMethod("POST");
            post.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            post.setDoOutput(true);
            post.getOutputStream().write(loginBody.getBytes(StandardCharsets.UTF_8));
            post.setInstanceFollowRedirects(false);
            post.setConnectTimeout(5000);
            post.setReadTimeout(5000);
            post.connect();
            String location = post.getHeaderField("Location");

            String accessTokenFragment;
            if (location == null) {
                // no redirect: look at the answer page
                String answer = IOUtils.toString(post.getInputStream(), StandardCharsets.UTF_8);
                if (answer.contains("Abuse?mkt=")) {
                    return new LoginResult(LoginStatus.ABUSE_LOCKED);
                }
                if (answer.contains("recover?mkt=")) {
                    return new LoginResult(LoginStatus.RECOVER_LOCKED);
                }
                if (!answer.contains("cancel?mkt=")) {
                    return new LoginResult(LoginStatus.FAILED);
                }
                String ipt = extract(answer, "(?<=\"ipt\" value=\").+?(?=\">)");
                String pprid = extract(answer, "(?<=\"pprid\" value=\").+?(?=\">)");
                String uaid = extract(answer, "(?<=\"uaid\" value=\").+?(?=\">)");
                String action = extract(answer, "(?<=id=\"fmHF\" action=\").+?(?=\" )");
                if (ipt == null || pprid == null || uaid == null || action == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
                String cancelBody = "ipt=" + ipt + "&pprid=" + pprid + "&uaid=" + uaid;
                // confirm the "cancel" page
                HttpURLConnection cancel = (HttpURLConnection) URI.create(action).toURL().openConnection();
                cancel.setRequestMethod("POST");
                cancel.setDoOutput(true);
                cancel.getOutputStream().write(cancelBody.getBytes(StandardCharsets.UTF_8));
                cancel.setInstanceFollowRedirects(false);
                cancel.setConnectTimeout(5000);
                cancel.setReadTimeout(5000);
                cancel.connect();
                String cancelAnswer = IOUtils.toString(cancel.getInputStream(), StandardCharsets.UTF_8);
                String returnUrl = extract(cancelAnswer, "(?<=\"recoveryCancel\":\\{\"returnUrl\":\").+?(?=\",)");
                if (returnUrl == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
                HttpURLConnection finish = (HttpURLConnection) URI.create(returnUrl).toURL().openConnection();
                finish.setRequestMethod("GET");
                finish.setDoOutput(true);
                finish.getOutputStream().write(cancelBody.getBytes(StandardCharsets.UTF_8));
                finish.setInstanceFollowRedirects(false);
                finish.setConnectTimeout(5000);
                finish.setReadTimeout(5000);
                finish.connect();
                String finishLocation = finish.getHeaderField("Location");
                if (finishLocation == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
                accessTokenFragment = StringUtil.firstGroup("access_token=([^&]*)", finishLocation);
                if (accessTokenFragment == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
            } else {
                accessTokenFragment = StringUtil.firstGroup("access_token=([^&]*)", location);
                if (accessTokenFragment == null) {
                    return new LoginResult(LoginStatus.FAILED);
                }
            }
            String msaToken = URLDecoder.decode(accessTokenFragment, StandardCharsets.UTF_8);
            CookieHandler.setDefault(previousCookieHandler);

            // step 3: Xbox Live user token
            String xblBody = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"" + msaToken
                    + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
            HttpURLConnection xbl = (HttpURLConnection) URI.create("https://user.auth.xboxlive.com/user/authenticate").toURL().openConnection();
            xbl.setRequestMethod("POST");
            xbl.addRequestProperty("Content-Type", "application/json");
            xbl.setDoOutput(true);
            xbl.getOutputStream().write(xblBody.getBytes(StandardCharsets.UTF_8));
            xbl.setConnectTimeout(5000);
            xbl.setReadTimeout(5000);
            xbl.connect();
            Gson gson = new Gson();
            JsonObject xblJson = gson.fromJson(IOUtils.toString(xbl.getInputStream(), StandardCharsets.UTF_8), JsonObject.class);
            String xblToken = xblJson.get("Token").getAsString();

            // step 4: XSTS token and user hash
            String xstsBody = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblToken
                    + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
            HttpURLConnection xsts = (HttpURLConnection) URI.create("https://xsts.auth.xboxlive.com/xsts/authorize").toURL().openConnection();
            xsts.setRequestMethod("POST");
            xsts.addRequestProperty("Content-Type", "application/json");
            xsts.setDoOutput(true);
            xsts.getOutputStream().write(xstsBody.getBytes(StandardCharsets.UTF_8));
            xsts.setConnectTimeout(5000);
            xsts.setReadTimeout(5000);
            xsts.connect();
            String xstsAnswer = IOUtils.toString(xsts.getInputStream(), StandardCharsets.UTF_8);
            String xstsToken = xstsAnswer.split("\"Token\":\"")[1].split("\"")[0];
            String userHash = xstsAnswer.split("\"uhs\":\"")[1].split("\"")[0];

            // step 5: Minecraft services
            String mcAnswer = MicrosoftAuth.post("https://api.minecraftservices.com/authentication/login_with_xbox",
                    "{\"identityToken\":\"" + "XBL3.0 x=" + userHash + ";" + xstsToken + "\"}", true);
            if (mcAnswer == null) {
                return new LoginResult(LoginStatus.FAILED);
            }
            JsonObject mcJson = JsonParser.parseString(mcAnswer).getAsJsonObject();
            if (!mcJson.has("access_token")) {
                return new LoginResult(LoginStatus.VALID_RATE_LIMITED);
            }
            String mcAccessToken = mcJson.get("access_token").getAsString();

            HttpURLConnection profileConnection = (HttpURLConnection) URI.create("https://api.minecraftservices.com/minecraft/profile").toURL().openConnection();
            try {
                profileConnection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
                profileConnection.setConnectTimeout(5000);
                profileConnection.setReadTimeout(5000);
                profileConnection.connect();
                MinecraftProfile profile = gson.fromJson(IOUtils.toString(profileConnection.getInputStream(), StandardCharsets.UTF_8), MinecraftProfile.class);
                return new LoginResult(new Session(profile.name, profile.id, mcAccessToken, "mojang"), LoginStatus.SUCCESS);
            } catch (FileNotFoundException e) {
                Logger.debugStackTrace(e, null);
                if (MicrosoftAuth.ownsMinecraft(mcAccessToken)) {
                    return new LoginResult(new Session(null, null, mcAccessToken, "mojang"), LoginStatus.NO_PROFILE);
                }
                return new LoginResult(new Session(null, null, mcAccessToken, "mojang"), LoginStatus.NO_MINECRAFT);
            } finally {
                profileConnection.disconnect();
            }
        } catch (IOException e) {
            Logger.debugStackTrace(e, null);
            CookieHandler.setDefault(previousCookieHandler);
            return new LoginResult(LoginStatus.FAILED);
        }
    }
}
