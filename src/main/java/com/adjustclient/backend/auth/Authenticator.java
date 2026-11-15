package com.adjustclient.backend.auth;

import com.adjustclient.Adjust;
import com.adjustclient.backend.transport.HydraConnection;
import com.adjustclient.backend.transport.HydraPacketEncoder;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.config.ConfigurationService;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.SimulationPlayer;
import com.adjustclient.module.impl.render.esp.render.EntityEspRenderer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

public final class Authenticator {
    public static boolean authenticated;

    private static ChallengeStep mcChallengeStep;
    private static int mcChallengeWord288;
    private static int mcChallengeWord292;

    private static final long KEY_CONST_0 = 0x8BFC8AA7BBCCEDB6L;
    private static final long KEY_CONST_1 = 0xB9D3F86F44CC1891L;
    private static final long KEY_CONST_2 = 0x7ABB929A9312FF82L;
    private static final long KEY_CONST_3 = 0xA8F5AEBC7E12B43CL;

    private static final byte[] B2_MASK_32 = {
        (byte) 0x33, (byte) 0xE4, (byte) 0x7F, (byte) 0x20,
        (byte) 0xB3, (byte) 0x64, (byte) 0xEF, (byte) 0x90,
        (byte) 0xB3, (byte) 0x74, (byte) 0xFF, (byte) 0xB0,
        (byte) 0x33, (byte) 0xF4, (byte) 0x6F, (byte) 0x20,
        (byte) 0xB3, (byte) 0x84, (byte) 0xFF, (byte) 0xC0,
        (byte) 0x33, (byte) 0x04, (byte) 0x6F, (byte) 0x30,
        (byte) 0x33, (byte) 0x14, (byte) 0x7F, (byte) 0x50,
        (byte) 0xB3, (byte) 0x94, (byte) 0xEF, (byte) 0xC0
    };

    private static final String B2_PREFIX = "JoeBidenIsSlow";

    public byte[] modulus;
    public byte[] key32;
    public byte[] token24;
    public byte[] challengeBuf;
    public int challengeToken;
    public int responseCheckXor;
    public int stepLimit;
    public int randomXorKey;
    public int stepXorKey;
    public int tempState;
    public int attemptMode;
    public byte lastChallengeByte;
    public byte sessionXorByte;
    public boolean initialized;
    public Object field52;

    private Authenticator() {
    }

    public static Authenticator create() {
        int counter = 0;
        int step = -1;
        int prevToken = 0;
        while (step < 10) {
            int nextToken = RandomUtil.RANDOM.nextInt();
            int nextCounter = counter + 1;
            if (nextToken == prevToken) {
                int confirm = RandomUtil.RANDOM.nextInt();
                if (confirm == nextToken) {
                    System.exit(0);
                }
            }
            int mod3 = step % 3;
            try {
                if (mod3 == 0) {
                    if (step == AuthStatics.ctorTrapState) {
                        int trigger = 1 / 0;
                        counter = trigger;
                    }
                    step++;
                    prevToken = nextToken;
                    counter = nextCounter;
                } else if (mod3 == 1 && step == 1) {
                    int trigger = 1 / 0;
                    counter = trigger;
                } else if (mod3 == 2 && step == 2) {
                    int trigger = 1 / 0;
                    counter = trigger;
                } else {
                    step++;
                    prevToken = nextToken;
                    counter = nextCounter;
                }
            } catch (ArithmeticException ex) {
                AuthStatics.ctorTrapState = 14343597;
                Authenticator auth = new Authenticator();
                auth.challengeToken = nextToken;
                auth.attemptMode = nextCounter;
                auth.g();
                return auth;
            }
        }
        AuthStatics.ctorTrapState = 0;
        Authenticator auth = new Authenticator();
        auth.challengeToken = prevToken;
        auth.attemptMode = counter;
        return auth;
    }

    private static byte[] initConstState() {
        byte[] s = new byte[32];
        writeLittleLong(s, 0, KEY_CONST_0);
        writeLittleLong(s, 8, KEY_CONST_1);
        writeLittleLong(s, 16, KEY_CONST_2);
        writeLittleLong(s, 24, KEY_CONST_3);
        return s;
    }

    private static void writeLittleLong(byte[] dst, int offset, long val) {
        for (int i = 0; i < 8; i++) {
            dst[offset + i] = (byte) (val >>> (i * 8));
        }
    }

    private static void cryptBuffer(byte[] buf, byte[] key) {
        byte[] s = initConstState();
        int len = buf.length;
        for (int i = 0; i < len; i++) {
            int kIdx = i & 31;
            int multKey = i * key[kIdx];
            if ((multKey % 12) >= -3) {
                byte b = (byte) (buf[i] ^ (i + s[kIdx]));
                buf[i] = b;
                int rounds = (multKey % 12) + 4;
                for (int j = 1; j < rounds; j++) {
                    b = (byte) (b ^ ((i >> j) + s[kIdx] + multKey * j));
                    buf[i] = b;
                }
            }
            int idx31 = i % 31;
            int idx27 = i % 27;
            int sx = s[idx31];
            int sy = s[idx27];
            int kProd = i * sx;
            int acc = (i + multKey + kProd * (i + multKey)) ^ (i + 1);
            acc = (2 * (i + 2)) ^ (acc + 2 * kProd * acc);
            acc = (3 * (i + 3)) ^ (acc + 3 * kProd * acc);
            acc = (4 * (i + 4)) ^ (acc + 4 * kProd * acc);
            acc = (5 * (i + 5)) ^ (acc + 5 * kProd * acc);
            acc = (6 * (i + 6)) ^ (acc + 6 * kProd * acc);
            int seed = i ^ (((7 * (i + 7)) ^ (acc + 7 * kProd * acc)) - sy);
            buf[i] ^= (byte) i;
            cryptRounds(buf, s, i, len, seed, kIdx, idx31);
        }
    }

    private static void cryptRounds(byte[] buf, byte[] s, int i, int len, int seed, int kIdx, int idx31) {
        int lenPlusI = len + i;
        int revIdx = (len - 1) - i;
        int i3 = i * i * i;
        for (int r = 0; r < 8; r++) {
            if (r > 0) {
                buf[i] ^= (byte) ((r * (i + r)) ^ i);
            }
            int curSeed = seed - r;
            int lim = (byte) curSeed;
            int shiftProd = (i3 << r);
            for (int k = 0; k < lim; k++) {
                buf[i] ^= (byte) (k + r * curSeed * ((i + shiftProd + s[kIdx]) ^ curSeed ^ r));
                buf[revIdx] ^= (byte) (curSeed + lenPlusI);
                buf[i] ^= (byte) (curSeed + k * s[r] + s[kIdx] - (k * (((byte) (i << k)) << r) + curSeed * i));
            }
            if (r > 0) {
                buf[i] ^= (byte) (r * curSeed + r);
            }
            buf[revIdx] ^= (byte) ((curSeed + (curSeed >> i) - (curSeed << r)) ^ (len * i));
        }

        int rMod = (3 * (i + 1)) % 32;
        int rComp = 31 - kIdx;
        int mult5 = 5 * (i % 9 + 1);
        int state = seed + s[idx31] - 8 - mult5;
        for (int v = 0; v < 9; v++) {
            boolean run;
            if (v == 0) {
                run = true;
            } else if ((v % 3) == 0) {
                state += s[idx31] - mult5;
                run = true;
            } else if ((state & 1) != 0) {
                int candidate = state + s[idx31] - mult5;
                state = candidate;
                run = ((candidate & 3) != 0);
            } else {
                run = false;
            }
            if (run) {
                for (int u = 0; u < rMod + 8; u++) {
                    int absB = (byte) (u ^ (i + u + state));
                    if (absB < 0) {
                        absB = (byte) -absB;
                    }
                    int mixIdx = 31 - (absB % 32);
                    s[mixIdx] ^= (byte) ((2 * u - u * absB) ^ (state + (state >> u)) ^ i);
                    buf[i] ^= (byte) ((u + state * (i + 1)) ^ u ^ (state * i) ^ s[kIdx]);
                    byte t = (byte) (u ^ (u + u - absB) ^ (state * i - absB) ^ s[rComp]);
                    s[rComp] = t;
                    if (u > 0 && (state >> kIdx) == 0) {
                        state ^= u;
                    }
                    buf[i] ^= (byte) ((u * state * (state ^ i))
                            ^ (((byte) u << state) + (u >> i) - (state + i) * (state >> i) + s[kIdx])
                            ^ t
                            ^ u);
                }
            }
        }

        int sqProd = ((byte) i) * i;
        int n = state;
        for (int w = 0; w < (n < 0 ? 7 : 8); w++) {
            s[kIdx] ^= (byte) ((2 * w * n) + (w ^ i));
            int target = (n >= 0) ? (n % 32) : ((-n) % 32);
            s[target] ^= (byte) ((n * w * sqProd) + (i >> n));
            n ^= (w * n * i) - (w * n) - (n >> i);
        }
    }

    public void a(byte[] data, int firstResponse, int secondResponse) {
        int v7 = this.randomXorKey ^ secondResponse;
        int mask = (this.sessionXorByte ^ 0x69) & 0xFF;
        int token = ((v7 & 0xFF) ^ mask)
                | ((((v7 >>> 8) & 0xFF) ^ mask) << 8)
                | ((((v7 >>> 16) & 0xFF) ^ mask) << 16)
                | ((((v7 >>> 24) & 0xFF) ^ mask) << 24);
        if (this.initialized && token != this.challengeToken) {
            throw AuthFailure.fail();
        }
        int mode = this.stepXorKey ^ firstResponse;
        if (mode >= this.stepLimit) {
            throw AuthFailure.fail();
        }
        if (!this.initialized) {
            b(data, mode, token);
            return;
        }
        if (this.stepXorKey == firstResponse) {
            throw AuthFailure.fail();
        }
        if (mode < 3) {
            if (mode == 1) {
                AuthStatics.stageOneDone = true;
                a(data, token);
            } else if (mode == 2) {
                d(data);
            }
            return;
        }
        if (mode == 3) {
            authenticated = true;
            c(data);
            return;
        }
        if (mode == 4) {
            AuthStatics.authTagString = "crack4crack?";
            b(data);
            AuthStatics.emptyMarkerBytes = new byte[0];
            if (!AuthStatics.deferredFlag21f8 && AuthStatics.deferredFlag21f6) {
                AuthStatics.deferredFlag21f8 = true;
            }
            if (!ConfigurationService.configurationsAvailable) {
                Logger.log(LogLevel.INFO, true, "Authenticated as '{}'", Adjust.username);
            }
        }
    }

    public void a(byte[] data, int token) {
        int len = data.length;
        byte[] dec = new byte[len];
        for (int i = 0; i < len; i++) {
            dec[i] = (byte) (data[i] ^ (74 * i + 32 * i + 72));
        }

        byte[] nameBytes = Adjust.username.getBytes(StandardCharsets.UTF_8);
        int nameLen = nameBytes.length;

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) (this.key32[i] ^ (i + (i + 2) * (i ^ 0x59) + 22) ^ 0xFB);
        }

        cryptBuffer(dec, key);

        if (len > 0) {
            dec[0] ^= 0x19;
            for (int k = 1; k < len; k++) {
                dec[k] ^= (byte) (k * 10 * k + 3 * k + 25);
            }
        }

        long startMs = System.currentTimeMillis();
        try {
            Thread.sleep(2L);
            Thread.sleep(2L);
            Thread.sleep(2L);
        } catch (InterruptedException ignored) {
        }

        int magicHash = dec[2] * (dec[1] + dec[1] * 18894 * dec[0]);
        magicHash = 2 * dec[2] + magicHash;
        magicHash = 3 * dec[3] + dec[3] * magicHash;
        magicHash = 4 * dec[4] + dec[4] * magicHash;
        magicHash = 5 * dec[5] + dec[5] * magicHash;
        magicHash = 6 * dec[6] + dec[6] * magicHash;
        magicHash = 7 * dec[7] + dec[7] * magicHash;
        magicHash = 8 * dec[8] + dec[8] * magicHash;
        magicHash = 9 * dec[9] + dec[9] * magicHash;
        magicHash = 10 * dec[10] + dec[10] * magicHash;
        if (magicHash != 1934738925 || this.challengeToken != token) {
            throw AuthFailure.fail();
        }

        int pos = len - 30;
        boolean started = false;
        int check = 907676;
        while (pos < len) {
            if (!started) {
                AuthStatics.checksumStartSlot = check + 1;
                started = true;
            }
            check = 1934738925 * dec[pos] + check * dec[pos] - check;
            pos++;
        }
        AuthStatics.checksumXorSlot = check ^ 0xA766BBEA;
        if (check != 251518423) {
            throw AuthFailure.fail();
        }
        if ((System.currentTimeMillis() - startMs - 6L) >= 0x1EAL) {
            throw AuthFailure.fail();
        }

        int xorToken = (dec[12] & 0xFF)
                | ((dec[14] & 0xFF) << 8)
                | ((dec[13] & 0xFF) << 16)
                | ((dec[15] & 0xFF) << 24);
        if (xorToken != token) {
            throw AuthFailure.fail();
        }

        for (int i = 0; i < nameLen; i++) {
            if (dec[16 + i] != nameBytes[i]) {
                throw AuthFailure.fail();
            }
        }

        int tailStart = nameLen + 15;
        if (nameLen != 0) {
            AuthStatics.stepFlag215e = true;
            int val = ((dec[tailStart] & 0xFF) << 24)
                    | ((dec[tailStart + 1] & 0xFF) << 16)
                    | ((dec[tailStart + 2] & 0xFF) << 8)
                    | (dec[tailStart + 3] & 0xFF);
            token = val ^ xorToken;
            int adjLen = (nameBytes[0] == (byte) Adjust.username.charAt(0)) ? len - 25 : len;
            float prevScale = mcChallengeStep != null ? mcChallengeStep.scale : 0.0f;
            ChallengeStep nextStep = new ChallengeStep();
            nextStep.status = 2;
            nextStep.scale = prevScale;
            mcChallengeStep = nextStep;

            int dkHeader = dec[adjLen - 8];
            int dkLen = dkHeader ^ dec[adjLen - 9];
            int dkStart = (adjLen - 15) - (dkHeader ^ dec[adjLen - 15]);
            byte[] dk = new byte[dkLen];
            int state = 9165 - (dkHeader & 0xFF);
            for (int k = 0; k < dkLen; k++) {
                int nextState = state - ((state % 7) + 34);
                if ((nextState >> 5) == 0) {
                    nextState--;
                }
                if (((1 << (nextState % 12)) & nextState) == 0) {
                    nextState++;
                }
                dk[k] = (byte) (state ^ dkHeader ^ dec[dkStart + k] ^ 0xAC);
                state = nextState;
            }
            ConfigurationService.dataKey = dk;

            ArrayList<DeferredConfigurationReply> deferred = ConfigurationService.deferredReplies;
            if (deferred != null) {
                if (Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null
                        && Adjust.INSTANCE.screenStack.configurations != null) {
                    ConfigurationService cfg = Adjust.INSTANCE.screenStack.configurations;
                    for (DeferredConfigurationReply reply : deferred) {
                        cfg.receiveData(reply.requestId, reply.status, reply.hash, reply.data);
                    }
                } else {
                    Logger.log(LogLevel.ERROR, true, "Logic bug #4", new Object[0]);
                    System.exit(0);
                }
                ConfigurationService.deferredReplies = null;
            }

            this.responseCheckXor = val ^ token ^ 0xBFBFBFBF;
            int symHash = 2321;
            for (int i = 0; i < nameLen; i++) {
                symHash = 33 * (nameBytes[i] + 33 * symHash) + nameBytes[nameLen - 1 - i];
            }
            int expectedSym = ((dec[nameLen + 20] & 0xFF) << 24)
                    | ((dec[nameLen + 21] & 0xFF) << 16)
                    | ((dec[nameLen + 22] & 0xFF) << 8)
                    | (dec[nameLen + 23] & 0xFF);
            if (symHash != expectedSym) {
                return;
            }

            if (ServiceUtil.modeOptionsApplied) {
                int rv = RandomUtil.RANDOM.nextInt();
                e(new byte[] {(byte) (rv >>> 16)});
                return;
            }

            byte[] tokenBytes = new byte[24];
            byte[] pair = new byte[2];
            for (int i = 0; i < 24; i++) {
                int r = (pair[1] ^ RandomUtil.RANDOM.nextInt()) + (int) (RandomUtil.nextGaussian(0.0, 0.0) * 10.0);
                RandomUtil.RANDOM.nextBytes(pair);
                tokenBytes[i] = (byte) (pair[0] ^ r);
            }
            Arrays.fill(dec, (byte) 0);
            e(tokenBytes);
            this.token24 = new byte[24];
            for (int i = 0; i < 24; i++) {
                this.token24[i] = (byte) (tokenBytes[i] ^ 0xBC);
            }
        }
    }

    public void b(byte[] data) {
        int len = data.length;
        byte[] dec = Arrays.copyOf(data, len);
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) (this.key32[i] ^ (i + (i + 2) * (i ^ 0x59) + 22) ^ 0xFB);
        }
        cryptBuffer(dec, key);

        for (int i = 0; i < len; i++) {
            dec[i] ^= (byte) (31 * i);
        }
        int checkXor = this.responseCheckXor ^ 0xBFBFBFBF;
        if (mcChallengeStep != null) {
            mcChallengeStep.scale = 1.0f;
        }
        for (int pos = 1; pos + 7 < len; pos += 8) {
            if (dec[pos] < 5) {
                return;
            }
            EntityEspRenderer.primitiveSlot1334++;
            byte b5 = dec[pos + 5];
            short tag = (short) ((((b5 ^ dec[pos + 7]) ^ 0x48) << 8) | ((b5 ^ dec[pos + 6]) ^ 0x43));
            if (tag >= 1) {
                int val = ((b5 ^ dec[pos + 1]) & 0xFF)
                        | (((b5 ^ dec[pos + 2]) & 0xFF) << 8)
                        | (((b5 ^ dec[pos + 3]) & 0xFF) << 16)
                        | (((b5 ^ dec[pos + 4]) & 0xFF) << 24);
                if (tag == 1828) {
                    AuthStatics.tokenSlot1350 = val;
                } else if (tag == 1982) {
                    AuthStatics.tokenSlot1354 = val;
                } else if (tag == 3144) {
                    AuthStatics.tokenSlot1358 = val - 105;
                }
            }
        }
        AuthStatics.stepFlag215e = (checkXor != 0);
    }

    public void b(byte[] data, int mode, int token) {
        long startMs = System.currentTimeMillis();
        if (mode != 0) {
            throw AuthFailure.fail();
        }
        int prevToken = this.challengeToken;
        this.tempState = (int) startMs;
        this.challengeToken = token;
        if (this.modulus == null || this.modulus.length < 5) {
            throw AuthFailure.fail();
        }
        this.tempState = this.modulus[4];
        byte[] plain = ChallengeMath.a(data, this.modulus);
        this.challengeToken = ((int) startMs) + 2;
        if (plain.length < 4 || plain[0] != 86 || plain[1] != 1 || plain[2] != 86 || plain[3] != 49 || plain.length != 52) {
            throw AuthFailure.fail();
        }
        this.challengeToken = plain[9];
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed < 0L) {
            return;
        }
        this.challengeToken = token;
        this.tempState = (int) elapsed;
        this.challengeToken = (int) elapsed;

        int hash = plain[4] + 68310;
        for (int idx = 5; idx <= 19; idx++) {
            hash = 33 * hash + plain[idx];
        }
        this.tempState = hash;
        if (hash != token) {
            throw AuthFailure.fail();
        }

        int prev = -554899859;
        int rngNext = 0;
        for (int i = 0; i < 9; i++) {
            int r1 = RandomUtil.RANDOM.nextInt();
            int r2 = RandomUtil.RANDOM.nextInt();
            if (r2 == r1) {
                rngNext = r2;
                break;
            }
            if (i == 8) {
                rngNext = RandomUtil.RANDOM.nextInt();
                break;
            }
        }
        this.challengeToken = token;
        this.initialized = true;
        this.tempState = 262947366;

        byte[] derived = new byte[32];
        for (int j = 20; j < 52; j++) {
            byte b = plain[j];
            this.tempState = b ^ (byte) (rngNext ^ prev ^ token);
            derived[j - 20] = (byte) (b ^ (j + (j - 9) * (j + 12) - 20));
        }

        byte[] newKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            int v = derived[i] & 0xFF;
            if (i == 10 || i == 18 || i == 21) {
                newKey[i] = (i == 21) ? (byte) ((~v) ^ 0xFB) : (byte) (~derived[i]);
            } else {
                newKey[i] = (byte) (derived[i] ^ B2_MASK_32[i]);
            }
        }
        this.key32 = newKey;

        int seedInt = rngNext ^ prev ^ token;
        byte[] prefix = new byte[18];
        byte[] baseBytes = B2_PREFIX.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(baseBytes, 0, prefix, 0, baseBytes.length);
        prefix[14] = (byte) (seedInt >>> 24);
        prefix[15] = (byte) seedInt;
        prefix[16] = (byte) (seedInt >>> 8);
        prefix[17] = (byte) (seedInt >>> 16);

        byte[] s = initConstState();
        for (int i = 0; i < 18; i++) {
            int mult = i * plain[i];
            byte b = prefix[i];
            int kLim = (mult % 12) + 4;
            for (int k = 0; k < kLim; k++) {
                b = (byte) (b ^ ((i >> k) + mult * k + s[i]));
            }
            prefix[i] = (byte) (b ^ i);
            byte sx = s[i];
            int kProd = i * sx;
            int acc = ((i + 1) ^ (i + mult + kProd * (i + mult)));
            acc = (2 * (i + 2)) ^ (acc + 2 * kProd * acc);
            acc = (3 * (i + 3)) ^ (acc + 3 * kProd * acc);
            acc = (4 * (i + 4)) ^ (acc + 4 * kProd * acc);
            acc = (5 * (i + 5)) ^ (acc + 5 * kProd * acc);
            acc = (6 * (i + 6)) ^ (acc + 6 * kProd * acc);
            int innerSeed = (((7 * (i + 7)) ^ acc) - sx) ^ i;
            cryptRounds(prefix, s, i, 18, innerSeed, i & 31, i);
        }

        for (int i = 0; i < 5; i++) {
            this.tempState = token;
            this.challengeToken = prevToken;
            try {
                Thread.sleep(1L);
            } catch (InterruptedException ignored) {
            }
        }
        if ((System.currentTimeMillis() - startMs) < 4L) {
            return;
        }
        e(prefix);
    }

    public void c(byte[] data) {
        int len = data.length;
        byte[] dec = Arrays.copyOf(data, len);
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) (this.key32[i] ^ (i + (i + 2) * (i ^ 0x59) + 22) ^ 0xFB);
        }
        cryptBuffer(dec, key);

        for (int i = 0; i < len; i++) {
            dec[i] ^= (byte) (48 * i + 22 * i + 124 - 69);
        }
        int magic = 911 * dec[19] * (911 * dec[18] * (911 * dec[17] * (911 * dec[16] * (911 * dec[15]
                * (911 * dec[14] * (911 * dec[13] * (911 * dec[12] * (911 * dec[11] * (911 * dec[10]
                * (911 * dec[9] * (911 * dec[8] * (911 * dec[7] * (911 * dec[6] * (911 * dec[5]
                * (911 * dec[4] * (911 * dec[3] * (911 * dec[2] * (911 * dec[1] * (83639821 * dec[0] + 511)
                + 510) + 509) + 508) + 507) + 506) + 505) + 504) + 503) + 502) + 501) + 500) + 499)
                + 498) + 497) + 496) + 495) + 494) + 493);
        if (magic != 2046858006) {
            throw AuthFailure.fail();
        }
        AuthStatics.stepByte20ce = 93;

        int markerIdx = -1;
        int scanEnd = len - 6;
        for (int pos = 23; pos < scanEnd; pos++) {
            if (dec[pos] == 0 && dec[pos + 1] == 0 && dec[pos + 2] == 0
                    && dec[pos + 3] == 0 && dec[pos + 4] == 0 && dec[pos + 5] == 0) {
                markerIdx = pos + 1;
                break;
            }
        }

        int markerBase = 0;
        boolean hasExtraKey = false;
        byte markerFirstByte = 0;
        if (markerIdx != -1) {
            markerBase = markerIdx;
            markerFirstByte = dec[markerBase + 5];
            byte b0 = (byte) (dec[markerBase + 5] ^ 0x23);
            key[0] = b0;
            this.key32[0] = (byte) (b0 ^ 0x81);
            boolean nonDefault = (b0 != 3);
            for (int off = markerBase + 6; off < markerBase + 37; off++) {
                int rel = off - (markerBase + 5);
                byte kb = (byte) (dec[off] ^ (43 * rel + 35 - 22 * rel));
                if (kb == 3) {
                    if (rel == 8) {
                        nonDefault = true;
                    }
                } else {
                    nonDefault = true;
                }
                this.key32[rel] = kb;
                key[rel] = (byte) ((29 * rel) ^ (43 * rel) ^ kb ^ 0x81);
            }
            hasExtraKey = nonDefault;
        }

        int hash = 187;
        for (int i = 0; i < len; i++) {
            hash = (391 * dec[i] * hash) ^ (dec[i] + 2 * i);
        }
        AuthStatics.stageValueSlot = dec[markerBase - 6] ^ 0xFFFFFFBC;
        AuthStatics.stepByte20d0 = (byte) (dec[markerBase - 5] ^ 0xFA);
        AuthStatics.stepTagShort = (short) ((dec[markerBase - 4] & 0xFF) + markerFirstByte);

        char u0 = Adjust.username.charAt(0);
        byte[] stage = new byte[37];
        stage[0] = (byte) (((byte) hash) ^ 0x17);
        stage[1] = (byte) (((byte) (hash >>> 8)) ^ 0xD7);
        stage[2] = (byte) (((byte) (hash >>> 16)) ^ 0x97);
        stage[3] = (byte) (((byte) (hash >>> 24)) ^ 0x57);
        stage[4] = (byte) (((byte) hash ^ (byte) u0) ^ 0x17);
        for (int i = 5; i < 37; i++) {
            switch ((i - 5) & 3) {
                case 0:
                    stage[i] = (byte) 0xD7;
                    break;
                case 1:
                    stage[i] = (byte) 0x97;
                    break;
                case 2:
                    stage[i] = (byte) 0x57;
                    break;
                default:
                    stage[i] = (byte) 0x17;
                    break;
            }
        }
        cryptBuffer(stage, key);

        mcChallengeWord288 = dec[markerBase - 8] ^ dec[markerBase - 7];
        int outLen = ((hash >>> 8) & 0xFF) + 48;
        byte[] reply = new byte[outLen];
        reply[0] = stage[0];
        short hHi = (short) (hash >>> 16);
        if (hasExtraKey) {
            byte xorKey = stage[36];
            for (int i = 1; i < outLen; i++) {
                if (i >= 37) {
                    reply[i] = (byte) ((28 * i) ^ hHi ^ (-107 * i));
                } else if (i == 5) {
                    ChallengeState cs = new ChallengeState();
                    cs.scale = (float) ((xorKey ^ dec[markerBase - 9]) * (xorKey ^ dec[markerBase - 10]));
                    cs.state = dec[markerBase - 11];
                    ChallengeState.current = cs;
                    reply[i] = stage[i];
                } else {
                    reply[i] = stage[i];
                }
            }
        } else {
            for (int i = 1; i < outLen; i++) {
                reply[i] = (i >= 37)
                        ? (byte) ((28 * i) ^ (-107 * i) ^ hHi)
                        : stage[i];
            }
        }
        e(reply);
    }

    public void d() {
        int randCount = RandomUtil.RANDOM.nextInt(47);
        int len = randCount + 59;
        int padCount = randCount + 39;
        byte[] buf = new byte[len];
        for (int k = 0; k < padCount; k++) {
            int r1 = RandomUtil.RANDOM.nextInt() >>> 16;
            int r2 = RandomUtil.RANDOM.nextInt() >>> 16;
            int rByte = (byte) (r2 ^ r1);
            AuthStatics.stepFlag2171 = true;
            if (k < 32) {
                int a = (int) (Math.random() * 420.0);
                int b = (int) (RandomUtil.nextGaussian(0.0, 0.0) * 10.0);
                int cByte = (b ^ a ^ rByte);
                buf[20 + k] = (byte) cByte;
                if (this.key32 != null && k < this.key32.length) {
                    this.key32[k] = (byte) ((33 * k) ^ (123 * k) ^ cByte ^ 0x8B);
                }
            } else {
                buf[20 + k] = (byte) rByte;
            }
        }

        byte[] header = "where is jenny from?".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(header, 0, buf, 0, header.length);
        AuthStatics.stepByte20ca = 32;
        SimulationPlayer.primitiveSlot20d1 = 2;
        for (int j = 0; j < len; j++) {
            buf[j] ^= (byte) ((-47 * j) ^ 0x8F);
        }
        this.challengeBuf = buf;

        byte[] key = new byte[32];
        for (int k = 0; k < 32; k++) {
            key[k] = (byte) (this.key32[k] ^ (k + (k + 2) * (k ^ 0x59) + 22) ^ 0xFB);
        }
        cryptBuffer(buf, key);

        for (int i = 0; i < len; i++) {
            buf[i] ^= (byte) (i + 33 - (i + 5) * (i ^ 0x16));
        }
        this.lastChallengeByte = buf[len - 1];

        byte[] packet = HydraPacketEncoder.encodeAuthResponse(len, this.stepXorKey ^ this.challengeToken,
                this.randomXorKey ^ this.stepLimit, buf);
        HydraConnection conn = Adjust.hydraConnection;
        if (conn != null && conn.authenticated) {
            conn.send(packet);
        }
        this.stepLimit++;
    }

    public void d(byte[] data) {
        if (data.length == 1) {
            d();
            return;
        }
        int len = data.length;
        byte[] dec = new byte[len];
        for (int j = 0; j < len; j++) {
            dec[j] = (byte) (data[j] ^ (11 * j + 22 * j - 69));
        }
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) (this.key32[i] ^ (i + (i + 2) * (i ^ 0x59) + 22) ^ 0xFB);
        }
        cryptBuffer(dec, key);

        AuthStatics.stepByte20cd = dec[len - 1];
        byte scaleKey = dec[len - 5];
        AuthStatics.challengeScaleAccumulator += (scaleKey ^ dec[len - 3]);

        int pos = 0;
        for (int idx = 0; idx < len; idx++) {
            if (dec[idx] == 36) {
                pos = idx + 1;
                break;
            }
        }

        String[] options = new String[5];
        byte[] tok = this.token24;
        int tokCursor = 0;
        for (int block = 0; block < 6; block++) {
            byte tag = dec[pos];
            if ((tag ^ block) != dec[pos + 1]) {
                throw AuthFailure.fail();
            }
            int chunkLen = (tag ^ dec[pos + 2]) & 0xFF;
            byte[] chunk = new byte[chunkLen];
            for (int k = 0; k < chunkLen; k++) {
                chunk[k] = (byte) ((tag - 117 * block - tag * block + 8) ^ dec[pos + 3 + k]);
            }
            pos += 3 + chunkLen;
            if (block != 5) {
                if (block != 0 && chunkLen > 0) {
                    chunk[0] ^= (byte) (tok[tokCursor % tok.length] ^ 0x43);
                    tokCursor++;
                    for (int k = 1; k < chunkLen; k++) {
                        chunk[k] ^= (byte) ((11 * k) ^ tok[tokCursor % tok.length] ^ 0x43);
                        tokCursor++;
                    }
                }
                if (block < 5) {
                    options[block] = new String(chunk, StandardCharsets.UTF_8);
                }
            } else {
                String uname = Adjust.username;
                for (int k = 0; k < uname.length(); k++) {
                    mcChallengeWord292++;
                    if ((((byte) uname.charAt(k)) ^ 0xBB) != (chunk[k] & 0xFF)) {
                        throw AuthFailure.fail();
                    }
                    AuthStatics.stepByte20cb = 80;
                }
            }
        }

        for (int i = 0; i < tok.length; i++) {
            tok[i] = (byte) (i ^ 0xB);
        }
        this.token24 = null;
        ServiceUtil.applyModeOptions(options, (byte) (dec[len - 2] ^ scaleKey));
        d();
    }

    public void e(byte[] payload) {
        int len = payload.length;
        int first = this.stepXorKey ^ this.challengeToken;
        int limit = this.stepLimit;
        int second = this.randomXorKey ^ limit;
        byte[] packet = new byte[len + 16];
        packet[0] = 19;
        packet[4] = (byte) (len >>> 24);
        packet[5] = (byte) (len >>> 16);
        packet[6] = (byte) (len >>> 8);
        packet[7] = (byte) len;
        packet[8] = (byte) (first >>> 24);
        packet[9] = (byte) (first >>> 16);
        packet[10] = (byte) (first >>> 8);
        packet[11] = (byte) first;
        packet[12] = (byte) (second >>> 24);
        packet[13] = (byte) (second >>> 16);
        packet[14] = (byte) (second >>> 8);
        packet[15] = (byte) second;
        System.arraycopy(payload, 0, packet, 16, len);
        HydraConnection conn = Adjust.hydraConnection;
        if (conn != null && conn.authenticated) {
            conn.send(packet);
        }
        this.stepLimit = limit + 1;
    }

    public void g() {
        int r1 = RandomUtil.RANDOM.nextInt();
        int r2 = RandomUtil.RANDOM.nextInt();
        int r3 = RandomUtil.RANDOM.nextInt();
        this.randomXorKey = r3;
        int r4 = RandomUtil.RANDOM.nextInt();
        int newStepXor = r1 ^ r2;
        byte sessionXor = (byte) ((r4 & 0xFF) ^ 0x69);

        HydraConnection conn = Adjust.hydraConnection;
        if (conn != null && conn.authenticated) {
            byte[] pkt = new byte[13];
            pkt[0] = 32;
            int rx = this.randomXorKey;
            pkt[4] = (byte) (rx >>> 24);
            pkt[5] = (byte) (rx >>> 16);
            pkt[6] = (byte) (rx >>> 8);
            pkt[7] = (byte) rx;
            pkt[8] = (byte) (newStepXor >>> 24);
            pkt[9] = (byte) (newStepXor >>> 16);
            pkt[10] = (byte) (newStepXor >>> 8);
            pkt[11] = (byte) newStepXor;
            pkt[12] = sessionXor;
            conn.send(pkt);
        }
        this.stepXorKey = newStepXor;
        this.sessionXorByte = sessionXor;
        if (this.attemptMode >= 2) {
            AuthStatics.authPhaseActive = true;
        }

        byte[][] kp = AuthStatics.cachedKeyPair;
        if (kp == null) {
            SecureRandom sr = new SecureRandom();
            BigInteger p = BigInteger.probablePrime(256, sr);
            BigInteger q = BigInteger.probablePrime(256, sr);
            byte[] nBytes = p.multiply(q).toByteArray();
            BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            BigInteger e = BigInteger.probablePrime(128, sr);
            while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
                e = e.add(BigInteger.ONE);
            }
            BigInteger d = e.modInverse(phi);
            byte[] privMod = ChallengeMath.c(d.toByteArray(), nBytes);
            byte[] pubMod = ChallengeMath.c(e.toByteArray(), nBytes);
            kp = new byte[][] {privMod, pubMod};
            AuthStatics.cachedKeyPair = kp;
        }

        this.modulus = kp[0];
        byte[] pub = kp[1];
        int pLen = pub.length;
        int first = this.stepXorKey ^ this.challengeToken;
        int limit = this.stepLimit;
        int second = this.randomXorKey ^ limit;
        byte[] hello = new byte[pLen + 16];
        hello[0] = 19;
        hello[4] = (byte) (pLen >>> 24);
        hello[5] = (byte) (pLen >>> 16);
        hello[6] = (byte) (pLen >>> 8);
        hello[7] = (byte) pLen;
        hello[8] = (byte) (first >>> 24);
        hello[9] = (byte) (first >>> 16);
        hello[10] = (byte) (first >>> 8);
        hello[11] = (byte) first;
        hello[12] = (byte) (second >>> 24);
        hello[13] = (byte) (second >>> 16);
        hello[14] = (byte) (second >>> 8);
        hello[15] = (byte) second;
        System.arraycopy(pub, 0, hello, 16, pLen);

        if (conn != null && conn.authenticated) {
            conn.send(hello);
        }
        this.stepLimit = limit + 1;
    }
}
