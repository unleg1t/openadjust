package paulscode.sound.libraries;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.ICodec;
import paulscode.sound.Library;
import paulscode.sound.ListenerData;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.Source;

/**
 * LWJGL 3 OpenAL backend for Paulscode SoundSystem, replacing the legacy LWJGL 2
 * {@code librarylwjglopenal} classes.
 */
public class LibraryLWJGLOpenAL extends Library {
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;

    static final int FORMAT_ILLEGAL_SAMPLE_SIZE = -1;
    static final int FORMAT_UNSUPPORTED_CHANNELS = -2;

    private static final Object OPENAL_LOCK = new Object();
    private static long alDevice = 0L;
    private static long alContext = 0L;
    private static ALCCapabilities alcCapabilities = null;
    private static ALCapabilities alCapabilities = null;

    private FloatBuffer listenerPositionAL = null;
    private FloatBuffer listenerOrientation = null;
    private FloatBuffer listenerVelocity = null;
    private HashMap<String, IntBuffer> ALBufferMap = null;
    private static boolean alPitchSupported = true;

    public LibraryLWJGLOpenAL() throws SoundSystemException {
        super();
        ALBufferMap = new HashMap<>();
        reverseByteOrder = true;
    }

    @Override
    public void init() throws SoundSystemException {
        boolean errors = false;
        try {
            createOpenAL();
            errors = checkALError();
        } catch (LWJGLException e) {
            errorMessage("Unable to initialize OpenAL.  Probable cause: OpenAL not supported.");
            printStackTrace(e);
            throw new LibraryLWJGLOpenAL.Exception(e.getMessage(), LibraryLWJGLOpenAL.Exception.CREATE);
        }

        if (errors) {
            importantMessage("OpenAL did not initialize properly!");
        } else {
            message("OpenAL initialized.");
        }

        listenerPositionAL = BufferUtils.createFloatBuffer(3).put(
                new float[] {listener.position.x, listener.position.y, listener.position.z});
        listenerOrientation = BufferUtils.createFloatBuffer(6).put(
                new float[] {
                    listener.lookAt.x,
                    listener.lookAt.y,
                    listener.lookAt.z,
                    listener.up.x,
                    listener.up.y,
                    listener.up.z
                });
        listenerVelocity = BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f});

        listenerPositionAL.flip();
        listenerOrientation.flip();
        listenerVelocity.flip();

        AL10.alListenerfv(AL10.AL_POSITION, listenerPositionAL);
        errors = checkALError() || errors;

        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        errors = checkALError() || errors;

        AL10.alListenerfv(AL10.AL_VELOCITY, listenerVelocity);
        errors = checkALError() || errors;

        AL10.alDopplerFactor(SoundSystemConfig.getDopplerFactor());
        errors = checkALError() || errors;

        AL10.alDopplerVelocity(SoundSystemConfig.getDopplerVelocity());
        errors = checkALError() || errors;

        if (errors) {
            importantMessage("OpenAL did not initialize properly!");
            throw new LibraryLWJGLOpenAL.Exception(
                    "Problem encountered while loading OpenAL or creating the listener.  Probable cause:  OpenAL not supported",
                    LibraryLWJGLOpenAL.Exception.CREATE);
        }

        super.init();

        int testChannelIndex = normalChannels.size() > 1 ? 1 : 0;
        if (normalChannels.isEmpty()) {
            alPitchSupported(SET, false);
            throw new LibraryLWJGLOpenAL.Exception(
                    "OpenAL: AL_PITCH not supported.", LibraryLWJGLOpenAL.Exception.NO_AL_PITCH);
        }
        ChannelLWJGLOpenAL channel = (ChannelLWJGLOpenAL) normalChannels.get(testChannelIndex);
        try {
            AL10.alSourcef(channel.ALSource.get(0), AL10.AL_PITCH, 1.0f);
            if (checkALError()) {
                alPitchSupported(SET, false);
                throw new LibraryLWJGLOpenAL.Exception(
                        "OpenAL: AL_PITCH not supported.", LibraryLWJGLOpenAL.Exception.NO_AL_PITCH);
            }
            alPitchSupported(SET, true);
        } catch (java.lang.Exception e) {
            alPitchSupported(SET, false);
            throw new LibraryLWJGLOpenAL.Exception(
                    "OpenAL: AL_PITCH not supported.", LibraryLWJGLOpenAL.Exception.NO_AL_PITCH);
        }
    }

    public static boolean libraryCompatible() {
        synchronized (OPENAL_LOCK) {
            if (isOpenALCreated()) {
                return true;
            }
            try {
                createOpenAL();
            } catch (Throwable t) {
                return false;
            }
            destroyOpenAL();
            return true;
        }
    }

    private static boolean isOpenALCreated() {
        return alDevice != 0L && alContext != 0L && alCapabilities != null;
    }

    private static void createOpenAL() throws LWJGLException {
        synchronized (OPENAL_LOCK) {
            if (isOpenALCreated()) {
                return;
            }
            long device = 0L;
            long context = 0L;
            try {
                ensureALCLoaded();
                device = ALC10.alcOpenDevice((ByteBuffer) null);
                if (device == 0L) {
                    throw new LWJGLException("Could not locate OpenAL library.");
                }
                ALCCapabilities deviceCaps = ALC.createCapabilities(device);
                context = ALC10.alcCreateContext(device, (IntBuffer) null);
                if (context == 0L) {
                    throw new LWJGLException("Could not create OpenAL context.");
                }
                if (!ALC10.alcMakeContextCurrent(context)) {
                    throw new LWJGLException("Could not make OpenAL context current.");
                }
                ALCapabilities caps = AL.createCapabilities(deviceCaps);
                AL.setCurrentProcess(caps);
                alcCapabilities = deviceCaps;
                alCapabilities = caps;
                alContext = context;
                alDevice = device;
            } catch (Throwable t) {
                if (context != 0L) {
                    try {
                        ALC10.alcMakeContextCurrent(0L);
                    } catch (Throwable ignored) {
                    }
                    try {
                        AL.setCurrentProcess(null);
                    } catch (Throwable ignored) {
                    }
                    try {
                        ALC10.alcDestroyContext(context);
                    } catch (Throwable ignored) {
                    }
                }
                if (device != 0L) {
                    try {
                        ALC10.alcCloseDevice(device);
                    } catch (Throwable ignored) {
                    }
                }
                alcCapabilities = null;
                alCapabilities = null;
                alContext = 0L;
                alDevice = 0L;
                if (t instanceof LWJGLException) {
                    throw (LWJGLException) t;
                }
                throw new LWJGLException(t.getMessage() != null ? t.getMessage() : t.toString(), t);
            }
        }
    }

    private static void ensureALCLoaded() {
        try {
            ALC.getFunctionProvider();
        } catch (IllegalStateException e) {
            try {
                ALC.create();
            } catch (IllegalStateException concurrentlyLoaded) {
                // Another thread (e.g. voice chat) initialized the OpenAL ICD concurrently.
                ALC.getFunctionProvider();
            }
        }
    }

    private static void destroyOpenAL() {
        synchronized (OPENAL_LOCK) {
            long context = alContext;
            long device = alDevice;
            alContext = 0L;
            alDevice = 0L;
            alcCapabilities = null;
            alCapabilities = null;
            if (context != 0L) {
                try {
                    ALC10.alcMakeContextCurrent(0L);
                } catch (Throwable ignored) {
                }
                try {
                    AL.setCurrentProcess(null);
                } catch (Throwable ignored) {
                }
                try {
                    ALC10.alcDestroyContext(context);
                } catch (Throwable ignored) {
                }
            }
            if (device != 0L) {
                try {
                    ALC10.alcCloseDevice(device);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    protected Channel createChannel(int type) {
        IntBuffer alSource = BufferUtils.createIntBuffer(1);
        try {
            AL10.alGenSources(alSource);
        } catch (java.lang.Exception e) {
            AL10.alGetError();
            return null;
        }
        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            return null;
        }
        return new ChannelLWJGLOpenAL(type, alSource);
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (bufferMap != null && ALBufferMap != null) {
            Set<String> keys = bufferMap.keySet();
            Iterator<String> iter = keys.iterator();
            boolean hasContext = isOpenALCreated();
            while (iter.hasNext()) {
                String filename = iter.next();
                IntBuffer buffer = ALBufferMap.get(filename);
                if (buffer != null) {
                    if (hasContext) {
                        AL10.alDeleteBuffers(buffer);
                        checkALError();
                    }
                    buffer.clear();
                }
            }
            bufferMap.clear();
            ALBufferMap.clear();
        }

        destroyOpenAL();

        bufferMap = null;
        listenerPositionAL = null;
        listenerOrientation = null;
        listenerVelocity = null;
    }

    @Override
    public boolean loadSound(FilenameURL filenameURL) {
        ensureBufferMaps("loadSound");
        if (errorCheck(filenameURL == null, "Filename/URL not specified in method 'loadSound'")) {
            return false;
        }
        if (bufferMap.get(filenameURL.getFilename()) != null) {
            return true;
        }

        ICodec codec = SoundSystemConfig.getCodec(filenameURL.getFilename());
        if (errorCheck(codec == null, "No codec found for file '" + filenameURL.getFilename() + "' in method 'loadSound'")) {
            return false;
        }
        codec.reverseByteOrder(true);

        URL url = filenameURL.getURL();
        if (errorCheck(url == null, "Unable to open file '" + filenameURL.getFilename() + "' in method 'loadSound'")) {
            return false;
        }

        codec.initialize(url);
        SoundBuffer buffer = codec.readAll();
        codec.cleanup();

        if (errorCheck(buffer == null, "Sound buffer null in method 'loadSound'")) {
            return false;
        }

        bufferMap.put(filenameURL.getFilename(), buffer);
        return uploadSoundBuffer(buffer, filenameURL.getFilename(), "loading");
    }

    @Override
    public boolean loadSound(SoundBuffer buffer, String identifier) {
        ensureBufferMaps("loadSound");
        if (errorCheck(identifier == null, "Identifier not specified in method 'loadSound'")) {
            return false;
        }
        if (bufferMap.get(identifier) != null) {
            return true;
        }
        if (errorCheck(buffer == null, "Sound buffer null in method 'loadSound'")) {
            return false;
        }

        bufferMap.put(identifier, buffer);
        return uploadSoundBuffer(buffer, identifier, "saving");
    }

    private void ensureBufferMaps(String methodName) {
        if (bufferMap == null) {
            bufferMap = new HashMap<>();
            importantMessage("Buffer Map was null in method '" + methodName + "'");
        }
        if (ALBufferMap == null) {
            ALBufferMap = new HashMap<>();
            importantMessage("Open AL Buffer Map was null in method'" + methodName + "'");
        }
    }

    private boolean uploadSoundBuffer(SoundBuffer buffer, String key, String errorVerb) {
        AudioFormat audioFormat = buffer.audioFormat;
        int soundFormat = resolveALFormat(audioFormat);
        if (soundFormat == FORMAT_ILLEGAL_SAMPLE_SIZE) {
            errorMessage("Illegal sample size in method 'loadSound'");
            return false;
        }
        if (soundFormat == FORMAT_UNSUPPORTED_CHANNELS) {
            errorMessage("File neither mono nor stereo in method 'loadSound'");
            return false;
        }

        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(intBuffer);
        if (errorCheck(AL10.alGetError() != AL10.AL_NO_ERROR, "alGenBuffers error when " + errorVerb + key)) {
            return false;
        }

        ByteBuffer pcm = BufferUtils.createByteBuffer(buffer.audioData.length);
        pcm.put(buffer.audioData);
        pcm.flip();
        AL10.alBufferData(intBuffer.get(0), soundFormat, pcm, (int) audioFormat.getSampleRate());
        if (errorCheck(AL10.alGetError() != AL10.AL_NO_ERROR, "alBufferData error when " + errorVerb + key)) {
            AL10.alDeleteBuffers(intBuffer);
            AL10.alGetError();
            return false;
        }

        IntBuffer previous = ALBufferMap.put(key, intBuffer);
        if (previous != null && previous != intBuffer) {
            AL10.alDeleteBuffers(previous);
            AL10.alGetError();
        }
        return true;
    }

    @Override
    public void unloadSound(String filename) {
        IntBuffer buffer = ALBufferMap != null ? ALBufferMap.remove(filename) : null;
        // Let the base library detach/stop sources that still reference the sound before the AL buffer is released.
        super.unloadSound(filename);
        if (buffer != null) {
            synchronized (OPENAL_LOCK) {
                if (isOpenALCreated()) {
                    try {
                        AL10.alDeleteBuffers(buffer);
                        AL10.alGetError();
                    } catch (java.lang.Exception e) {
                    }
                }
            }
            buffer.clear();
        }
    }

    @Override
    public void setMasterVolume(float value) {
        super.setMasterVolume(value);
        AL10.alListenerf(AL10.AL_GAIN, value);
        checkALError();
    }

    @Override
    public void newSource(
            boolean priority,
            boolean toStream,
            boolean toLoop,
            String sourcename,
            FilenameURL filenameURL,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll) {
        IntBuffer myBuffer = null;
        if (!toStream) {
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            if (myBuffer == null && !loadSound(filenameURL)) {
                errorMessage(
                        "Source '" + sourcename + "' was not created"
                                + "because an error occurred while loading"
                                + filenameURL.getFilename());
                return;
            }
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            if (myBuffer == null) {
                errorMessage(
                        "Source '" + sourcename + "' was not created"
                                + "because a sound buffer was not found for"
                                + filenameURL.getFilename());
                return;
            }
        }

        SoundBuffer buffer = null;
        if (!toStream) {
            buffer = bufferMap.get(filenameURL.getFilename());
            if (buffer == null && !loadSound(filenameURL)) {
                errorMessage(
                        "Source '" + sourcename + "' was not created"
                                + "because an error occurred while loading"
                                + filenameURL.getFilename());
                return;
            }
            buffer = bufferMap.get(filenameURL.getFilename());
            if (buffer == null) {
                errorMessage(
                        "Source '" + sourcename + "' was not created"
                                + "because audio data was not found for"
                                + filenameURL.getFilename());
                return;
            }
        }

        sourceMap.put(
                sourcename,
                new SourceLWJGLOpenAL(
                        listenerPositionAL,
                        myBuffer,
                        priority,
                        toStream,
                        toLoop,
                        sourcename,
                        filenameURL,
                        buffer,
                        x,
                        y,
                        z,
                        attModel,
                        distOrRoll,
                        false));
    }

    @Override
    public void rawDataStream(
            AudioFormat audioFormat,
            boolean priority,
            String sourcename,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll) {
        sourceMap.put(
                sourcename,
                new SourceLWJGLOpenAL(
                        listenerPositionAL,
                        audioFormat,
                        priority,
                        sourcename,
                        x,
                        y,
                        z,
                        attModel,
                        distOrRoll));
    }

    @Override
    public void quickPlay(
            boolean priority,
            boolean toStream,
            boolean toLoop,
            String sourcename,
            FilenameURL filenameURL,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll,
            boolean temporary) {
        IntBuffer myBuffer = null;
        if (!toStream) {
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            if (myBuffer == null) {
                loadSound(filenameURL);
            }
            myBuffer = ALBufferMap.get(filenameURL.getFilename());
            if (myBuffer == null) {
                errorMessage("Sound buffer was not created for" + filenameURL.getFilename());
                return;
            }
        }

        SoundBuffer buffer = null;
        if (!toStream) {
            buffer = bufferMap.get(filenameURL.getFilename());
            if (buffer == null && !loadSound(filenameURL)) {
                errorMessage(
                        "Source '" + sourcename + "' was not created"
                                + "because an error occurred while loading"
                                + filenameURL.getFilename());
                return;
            }
            buffer = bufferMap.get(filenameURL.getFilename());
            if (buffer == null) {
                errorMessage(
                        "Source '" + sourcename + "' was not created"
                                + "because audio data was not found for"
                                + filenameURL.getFilename());
                return;
            }
        }

        SourceLWJGLOpenAL s = new SourceLWJGLOpenAL(
                listenerPositionAL,
                myBuffer,
                priority,
                toStream,
                toLoop,
                sourcename,
                filenameURL,
                buffer,
                x,
                y,
                z,
                attModel,
                distOrRoll,
                false);
        sourceMap.put(sourcename, s);
        play(s);
        if (temporary) {
            s.setTemporary(true);
        }
    }

    @Override
    public void copySources(HashMap<String, Source> srcMap) {
        if (srcMap == null) {
            return;
        }
        Set<String> keys = srcMap.keySet();
        Iterator<String> iter = keys.iterator();

        ensureBufferMaps("copySources");

        sourceMap.clear();

        while (iter.hasNext()) {
            String sourcename = iter.next();
            Source source = srcMap.get(sourcename);
            if (source != null) {
                SoundBuffer buffer = null;
                if (!source.toStream) {
                    loadSound(source.filenameURL);
                    buffer = bufferMap.get(source.filenameURL.getFilename());
                }
                if (source.toStream || buffer != null) {
                    sourceMap.put(
                            sourcename,
                            new SourceLWJGLOpenAL(
                                    listenerPositionAL,
                                    ALBufferMap.get(source.filenameURL.getFilename()),
                                    source,
                                    buffer));
                }
            }
        }
    }

    @Override
    public void setListenerPosition(float x, float y, float z) {
        super.setListenerPosition(x, y, z);
        listenerPositionAL.put(0, x);
        listenerPositionAL.put(1, y);
        listenerPositionAL.put(2, z);
        AL10.alListenerfv(AL10.AL_POSITION, listenerPositionAL);
        checkALError();
    }

    @Override
    public void setListenerAngle(float angle) {
        super.setListenerAngle(angle);
        listenerOrientation.put(0, listener.lookAt.x);
        listenerOrientation.put(2, listener.lookAt.z);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        checkALError();
    }

    @Override
    public void setListenerOrientation(
            float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
        super.setListenerOrientation(lookX, lookY, lookZ, upX, upY, upZ);
        listenerOrientation.put(0, lookX);
        listenerOrientation.put(1, lookY);
        listenerOrientation.put(2, lookZ);
        listenerOrientation.put(3, upX);
        listenerOrientation.put(4, upY);
        listenerOrientation.put(5, upZ);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        checkALError();
    }

    @Override
    public void setListenerData(ListenerData l) {
        super.setListenerData(l);
        listenerPositionAL.put(0, l.position.x);
        listenerPositionAL.put(1, l.position.y);
        listenerPositionAL.put(2, l.position.z);
        AL10.alListenerfv(AL10.AL_POSITION, listenerPositionAL);
        checkALError();

        listenerOrientation.put(0, l.lookAt.x);
        listenerOrientation.put(1, l.lookAt.y);
        listenerOrientation.put(2, l.lookAt.z);
        listenerOrientation.put(3, l.up.x);
        listenerOrientation.put(4, l.up.y);
        listenerOrientation.put(5, l.up.z);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOrientation);
        checkALError();

        listenerVelocity.put(0, l.velocity.x);
        listenerVelocity.put(1, l.velocity.y);
        listenerVelocity.put(2, l.velocity.z);
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerVelocity);
        checkALError();
    }

    @Override
    public void setListenerVelocity(float x, float y, float z) {
        super.setListenerVelocity(x, y, z);
        listenerVelocity.put(0, listener.velocity.x);
        listenerVelocity.put(1, listener.velocity.y);
        listenerVelocity.put(2, listener.velocity.z);
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerVelocity);
    }

    @Override
    public void dopplerChanged() {
        super.dopplerChanged();
        AL10.alDopplerFactor(SoundSystemConfig.getDopplerFactor());
        checkALError();
        AL10.alDopplerVelocity(SoundSystemConfig.getDopplerVelocity());
        checkALError();
    }

    private boolean checkALError() {
        String message = describeALError(AL10.alGetError());
        if (message == null) {
            return false;
        }
        errorMessage(message);
        return true;
    }

    static String describeALError(int error) {
        switch (error) {
            case AL10.AL_NO_ERROR:
                return null;
            case AL10.AL_INVALID_NAME:
                return "Invalid name parameter.";
            case AL10.AL_INVALID_ENUM:
                return "Invalid parameter.";
            case AL10.AL_INVALID_VALUE:
                return "Invalid enumerated parameter value.";
            case AL10.AL_INVALID_OPERATION:
                return "Illegal call.";
            case AL10.AL_OUT_OF_MEMORY:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    static int resolveALFormat(AudioFormat audioFormat) {
        int channels = audioFormat.getChannels();
        int sampleSize = audioFormat.getSampleSizeInBits();
        if (channels == 1) {
            if (sampleSize == 8) {
                return AL10.AL_FORMAT_MONO8;
            }
            if (sampleSize == 16) {
                return AL10.AL_FORMAT_MONO16;
            }
            return FORMAT_ILLEGAL_SAMPLE_SIZE;
        }
        if (channels == 2) {
            if (sampleSize == 8) {
                return AL10.AL_FORMAT_STEREO8;
            }
            if (sampleSize == 16) {
                return AL10.AL_FORMAT_STEREO16;
            }
            return FORMAT_ILLEGAL_SAMPLE_SIZE;
        }
        return FORMAT_UNSUPPORTED_CHANNELS;
    }

    public static boolean alPitchSupported() {
        return alPitchSupported(GET, XXX);
    }

    private static synchronized boolean alPitchSupported(boolean action, boolean value) {
        if (action == SET) {
            alPitchSupported = value;
        }
        return alPitchSupported;
    }

    public static String getTitle() {
        return "LWJGL OpenAL";
    }

    public static String getDescription() {
        return "The LWJGL binding of OpenAL.  For more information, see http://www.lwjgl.org";
    }

    @Override
    public String getClassName() {
        return "LibraryLWJGLOpenAL";
    }

    public static class Exception extends SoundSystemException {
        public static final int CREATE = 101;
        public static final int INVALID_NAME = 102;
        public static final int INVALID_ENUM = 103;
        public static final int INVALID_VALUE = 104;
        public static final int INVALID_OPERATION = 105;
        public static final int OUT_OF_MEMORY = 106;
        public static final int LISTENER = 107;
        public static final int NO_AL_PITCH = 108;

        public Exception(String message) {
            super(message);
        }

        public Exception(String message, int type) {
            super(message, type);
        }
    }
}
