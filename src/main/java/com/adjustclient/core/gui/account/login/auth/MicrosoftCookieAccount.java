package com.adjustclient.core.gui.account.login.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.util.Session;

import com.adjustclient.utilities.auth.AuthApiException;
import com.adjustclient.utilities.auth.Cookie;
import com.adjustclient.utilities.auth.CookieFileParser;
import com.adjustclient.utilities.auth.EntitlementsRequest;
import com.adjustclient.utilities.auth.LoginResult;
import com.adjustclient.utilities.auth.LoginStatus;
import com.adjustclient.utilities.auth.LoginWithXboxRequest;
import com.adjustclient.utilities.auth.LoginWithXboxResponse;
import com.adjustclient.utilities.auth.MicrosoftCookieLogin;
import com.adjustclient.utilities.auth.ProfileResponse;
import com.adjustclient.utilities.auth.ProfileRequest;
import com.adjustclient.utilities.auth.SisuLoginUrlRequest;
import com.adjustclient.utilities.auth.XboxIdentity;
import com.adjustclient.utilities.ByteUtil;
import com.adjustclient.core.gui.account.AccountEntry;
import com.adjustclient.core.gui.account.AccountType;

public final class MicrosoftCookieAccount extends AccountEntry {

    /** Prefix of the current persisted format. */
    private static byte[] MAGIC = new byte[] { (byte) 0xBB, 0x3F, (byte) 0xAC, (byte) 0x99, (byte) 0xEF, 0x33 };

    /** Cookie file text (Netscape format). */
    private String cookies;
    private Object field76;

    public MicrosoftCookieAccount(String cookies, boolean flag) { this.cookies = cookies; }

    // Local persistence allocation; populated by deserialize.
    public MicrosoftCookieAccount() { }

    /** Reads the persisted blob (current GZIP format or the legacy length-prefixed format). */
    @Override
    public void deserialize(byte[] data) throws IOException {
        this.cookies = decode(data);
    }

    @Override
    public AccountType getType() {
        return AccountType.COOKIE;
    }

    /** Logs in with the stored cookies. */
    @Override
    public LoginResult login() {
        try {
            String loginUrl = new SisuLoginUrlRequest().a();
            List<Cookie> cookieList = CookieFileParser.a(this.cookies);
            StringBuilder cookieHeader = new StringBuilder();
            for (int i = 0; i < cookieList.size(); i++) {
                Cookie cookie = cookieList.get(i);
                cookieHeader.append(cookie.toString().trim());     // "name=value" (record toString)
                if (i != cookieList.size() - 1) {
                    cookieHeader.append("; ");
                }
            }
            String cookieString = cookieHeader.toString().trim();

            try {
                URI uri = URI.create(loginUrl);
                XboxIdentity identity = new MicrosoftCookieLogin(uri, cookieString).a();
                LoginWithXboxResponse response = new LoginWithXboxRequest("{\"identityToken\":\"" + identity.identityToken() + "\"}").a();
                if (response.accessToken == null) {
                    return new LoginResult(LoginStatus.VALID_RATE_LIMITED);
                }
                String accessToken = response.accessToken;
                try {
                    ProfileResponse profile = new ProfileRequest(accessToken, null).a();
                    return new LoginResult(new Session(profile.name, profile.id, accessToken, "mojang"), LoginStatus.SUCCESS);
                } catch (AuthApiException e) {
                    if (new EntitlementsRequest(accessToken).a().valid()) {
                        return new LoginResult(new Session(null, null, accessToken, "mojang"), LoginStatus.NO_PROFILE);
                    }
                    return new LoginResult(new Session(null, null, accessToken, "mojang"), LoginStatus.NO_MINECRAFT);
                }
            } catch (IllegalArgumentException e) {
                return new LoginResult(LoginStatus.FAILED);
            }
        } catch (IllegalArgumentException e) {
            return new LoginResult(LoginStatus.RATE_LIMITED);
        }
    }

    /**
     * Decodes a persisted blob into the cookie text: data starting with {@code MAGIC} is GZIP(UTF-16BE text) after the 6 byte
     * prefix, everything else is the legacy format.
     */
    public static String decode(byte[] data) throws IOException {
        for (int i = 0; i < MAGIC.length; i++) {
            if (data[i] != MAGIC[i]) {
                return decodeLegacy(data);   // note: data shorter than MAGIC throws ArrayIndexOutOfBoundsException here (no length guard)
            }
        }
        byte[] raw;
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data, MAGIC.length, data.length - MAGIC.length))) {
            raw = in.readAllBytes();
        }
        return new String(raw, StandardCharsets.UTF_16BE);
    }

    /** Persisted form: {@code MAGIC} followed by the GZIP compressed cookie text (UTF-16BE). */
    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(MAGIC, 0, MAGIC.length);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(this.cookies.getBytes(StandardCharsets.UTF_16BE));
        }
        return bytes.toByteArray();
    }

    /**
     * Legacy format: {@code [int textLength][int extraLength][text (UTF-16BE)][extra bytes]}, big-endian ints; only the text is used.
     */
    private static String decodeLegacy(byte[] data) throws IOException {
        int textLength = ByteUtil.readInt(data, 0);
        int extraLength = ByteUtil.readInt(data, 4);
        if (data.length != textLength + extraLength + 8) {
            throw new IOException(textLength + " " + extraLength + " != " + data.length);
        }
        return new String(data, 8, textLength, StandardCharsets.UTF_16BE);
    }
}
