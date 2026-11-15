package paulscode.sound.libraries;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;

public class SourceLWJGLOpenAL extends Source {
    private ChannelLWJGLOpenAL channelOpenAL;
    private IntBuffer myBuffer;
    private FloatBuffer listenerPosition;
    private FloatBuffer sourcePosition;
    private FloatBuffer sourceVelocity;

    public SourceLWJGLOpenAL(
            FloatBuffer listenerPosition,
            IntBuffer myBuffer,
            boolean priority,
            boolean toStream,
            boolean toLoop,
            String sourcename,
            FilenameURL filenameURL,
            SoundBuffer soundBuffer,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll,
            boolean temporary) {
        super(priority, toStream, toLoop, sourcename, filenameURL, soundBuffer, x, y, z, attModel, distOrRoll, temporary);
        channelOpenAL = (ChannelLWJGLOpenAL) channel;
        if (codec != null) {
            codec.reverseByteOrder(true);
        }
        this.listenerPosition = listenerPosition;
        this.myBuffer = myBuffer;
        libraryType = LibraryLWJGLOpenAL.class;
        pitch = 1.0f;
        initSourceBuffers();
    }

    public SourceLWJGLOpenAL(
            FloatBuffer listenerPosition,
            IntBuffer myBuffer,
            Source old,
            SoundBuffer soundBuffer) {
        super(old, soundBuffer);
        channelOpenAL = (ChannelLWJGLOpenAL) channel;
        if (codec != null) {
            codec.reverseByteOrder(true);
        }
        this.listenerPosition = listenerPosition;
        this.myBuffer = myBuffer;
        libraryType = LibraryLWJGLOpenAL.class;
        pitch = 1.0f;
        initSourceBuffers();
    }

    public SourceLWJGLOpenAL(
            FloatBuffer listenerPosition,
            AudioFormat audioFormat,
            boolean priority,
            String sourcename,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll) {
        super(audioFormat, priority, sourcename, x, y, z, attModel, distOrRoll);
        channelOpenAL = (ChannelLWJGLOpenAL) channel;
        this.listenerPosition = listenerPosition;
        libraryType = LibraryLWJGLOpenAL.class;
        pitch = 1.0f;
        initSourceBuffers();
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public void changeSource(
            FloatBuffer listenerPosition,
            IntBuffer myBuffer,
            boolean priority,
            boolean toStream,
            boolean toLoop,
            String sourcename,
            FilenameURL filenameURL,
            SoundBuffer soundBuffer,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll,
            boolean temporary) {
        super.changeSource(priority, toStream, toLoop, sourcename, filenameURL, soundBuffer, x, y, z, attModel, distOrRoll, temporary);
        this.listenerPosition = listenerPosition;
        this.myBuffer = myBuffer;
        pitch = 1.0f;
        resetALInformation();
    }

    @Override
    public boolean incrementSoundSequence() {
        if (!toStream) {
            errorMessage("Method 'incrementSoundSequence' may only be used for streaming sources.");
            return false;
        }
        synchronized (soundSequenceLock) {
            if (soundSequenceQueue != null && soundSequenceQueue.size() > 0) {
                filenameURL = soundSequenceQueue.remove(0);
                if (codec != null) {
                    codec.cleanup();
                }
                codec = SoundSystemConfig.getCodec(filenameURL.getFilename());
                if (codec != null) {
                    codec.reverseByteOrder(true);
                    if (codec.getAudioFormat() == null) {
                        codec.initialize(filenameURL.getURL());
                    }
                    AudioFormat audioFormat = codec.getAudioFormat();
                    if (!configureStreamFormat(audioFormat, "incrementSoundSequence")) {
                        return false;
                    }
                    preLoad = true;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void listenerMoved() {
        positionChanged();
    }

    @Override
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
        if (sourcePosition == null) {
            resetALInformation();
        } else {
            positionChanged();
        }

        sourcePosition.put(0, x);
        sourcePosition.put(1, y);
        sourcePosition.put(2, z);

        if (hasAttachedALSource()) {
            AL10.alSourcefv(channelOpenAL.ALSource.get(0), AL10.AL_POSITION, sourcePosition);
            checkALError();
        }
    }

    @Override
    public void positionChanged() {
        applyPositionChange();
    }

    private void applyPositionChange() {
        calculateDistance();
        calculateGain();
        if (hasAttachedALSource()) {
            AL10.alSourcef(
                    channelOpenAL.ALSource.get(0),
                    AL10.AL_GAIN,
                    gain * sourceVolume * Math.abs(fadeOutGain) * fadeInGain);
            checkALError();
        }
        checkPitch();
    }

    private boolean hasAttachedALSource() {
        return channel != null
                && channel.attachedSource == this
                && channelOpenAL != null
                && channelOpenAL.ALSource != null;
    }

    private void checkPitch() {
        if (hasAttachedALSource() && LibraryLWJGLOpenAL.alPitchSupported()) {
            AL10.alSourcef(channelOpenAL.ALSource.get(0), AL10.AL_PITCH, pitch);
            checkALError();
        }
    }

    @Override
    public void setLooping(boolean lp) {
        super.setLooping(lp);
        if (hasAttachedALSource()) {
            AL10.alSourcei(
                    channelOpenAL.ALSource.get(0),
                    AL10.AL_LOOPING,
                    lp ? AL10.AL_TRUE : AL10.AL_FALSE);
            checkALError();
        }
    }

    @Override
    public void setAttenuation(int model) {
        super.setAttenuation(model);
        if (hasAttachedALSource()) {
            AL10.alSourcef(
                    channelOpenAL.ALSource.get(0),
                    AL10.AL_ROLLOFF_FACTOR,
                    model == SoundSystemConfig.ATTENUATION_ROLLOFF ? distOrRoll : 0.0f);
            checkALError();
        }
    }

    @Override
    public void setDistOrRoll(float dr) {
        super.setDistOrRoll(dr);
        if (hasAttachedALSource()) {
            AL10.alSourcef(
                    channelOpenAL.ALSource.get(0),
                    AL10.AL_ROLLOFF_FACTOR,
                    attModel == SoundSystemConfig.ATTENUATION_ROLLOFF ? dr : 0.0f);
            checkALError();
        }
    }

    @Override
    public void setVelocity(float x, float y, float z) {
        super.setVelocity(x, y, z);
        sourceVelocity = BufferUtils.createFloatBuffer(3).put(new float[] {x, y, z});
        sourceVelocity.flip();
        if (hasAttachedALSource()) {
            AL10.alSourcefv(channelOpenAL.ALSource.get(0), AL10.AL_VELOCITY, sourceVelocity);
            checkALError();
        }
    }

    @Override
    public void setPitch(float value) {
        super.setPitch(value);
        checkPitch();
    }

    @Override
    public void play(Channel c) {
        if (!active()) {
            if (toLoop) {
                toPlay = true;
            }
            return;
        }
        if (c == null) {
            errorMessage("Unable to play source, because channel was null");
            return;
        }

        boolean newChannel = channel != c;
        if (channel != null && channel.attachedSource != this) {
            newChannel = true;
        }

        boolean wasPaused = paused();
        super.play(c);

        channelOpenAL = (ChannelLWJGLOpenAL) channel;

        if (newChannel) {
            setPosition(position.x, position.y, position.z);
            checkPitch();

            if (channelOpenAL != null && channelOpenAL.ALSource != null) {
                if (LibraryLWJGLOpenAL.alPitchSupported()) {
                    AL10.alSourcef(channelOpenAL.ALSource.get(0), AL10.AL_PITCH, pitch);
                    checkALError();
                }

                AL10.alSourcefv(channelOpenAL.ALSource.get(0), AL10.AL_POSITION, sourcePosition);
                checkALError();

                AL10.alSourcefv(channelOpenAL.ALSource.get(0), AL10.AL_VELOCITY, sourceVelocity);
                checkALError();

                if (attModel == SoundSystemConfig.ATTENUATION_ROLLOFF) {
                    AL10.alSourcef(channelOpenAL.ALSource.get(0), AL10.AL_ROLLOFF_FACTOR, distOrRoll);
                } else {
                    AL10.alSourcef(channelOpenAL.ALSource.get(0), AL10.AL_ROLLOFF_FACTOR, 0.0f);
                }
                checkALError();

                if (toLoop && !toStream) {
                    AL10.alSourcei(channelOpenAL.ALSource.get(0), AL10.AL_LOOPING, AL10.AL_TRUE);
                } else {
                    AL10.alSourcei(channelOpenAL.ALSource.get(0), AL10.AL_LOOPING, AL10.AL_FALSE);
                }
                checkALError();
            }

            if (!toStream) {
                if (myBuffer == null) {
                    errorMessage("No sound buffer to play");
                    return;
                }
                channelOpenAL.attachBuffer(myBuffer);
            }
        }

        if (!playing()) {
            if (toStream && !wasPaused) {
                if (codec == null) {
                    errorMessage("Decoder null in method 'play'");
                    return;
                }
                if (codec.getAudioFormat() == null) {
                    codec.initialize(filenameURL.getURL());
                }
                AudioFormat audioFormat = codec.getAudioFormat();
                if (!configureStreamFormat(audioFormat, "play")) {
                    return;
                }
                preLoad = true;
            }
            channel.play();
            if (pitch != 1.0f) {
                checkPitch();
            }
        }
    }

    private boolean configureStreamFormat(AudioFormat audioFormat, String methodName) {
        if (audioFormat == null) {
            errorMessage("Audio Format null in method '" + methodName + "'");
            return false;
        }
        int soundFormat = LibraryLWJGLOpenAL.resolveALFormat(audioFormat);
        if (soundFormat == LibraryLWJGLOpenAL.FORMAT_ILLEGAL_SAMPLE_SIZE) {
            errorMessage("Illegal sample size in method '" + methodName + "'");
            return false;
        }
        if (soundFormat == LibraryLWJGLOpenAL.FORMAT_UNSUPPORTED_CHANNELS) {
            errorMessage("Audio data neither mono nor stereo in method '" + methodName + "'");
            return false;
        }
        channelOpenAL.setFormat(soundFormat, (int) audioFormat.getSampleRate());
        return true;
    }

    @Override
    public boolean preLoad() {
        if (codec == null) {
            return false;
        }
        codec.initialize(filenameURL.getURL());
        LinkedList<byte[]> preLoadBuffers = new LinkedList<>();
        for (int i = 0; i < SoundSystemConfig.getNumberStreamingBuffers(); i++) {
            soundBuffer = codec.read();
            if (soundBuffer == null || soundBuffer.audioData == null) {
                break;
            }
            preLoadBuffers.add(soundBuffer.audioData);
        }
        positionChanged();
        channel.preLoadBuffers(preLoadBuffers);
        preLoad = false;
        return true;
    }

    private void initSourceBuffers() {
        sourcePosition = BufferUtils.createFloatBuffer(3).put(new float[] {position.x, position.y, position.z});
        sourceVelocity = BufferUtils.createFloatBuffer(3).put(new float[] {velocity.x, velocity.y, velocity.z});
        sourcePosition.flip();
        sourceVelocity.flip();
        calculateDistance();
        calculateGain();
    }

    private void resetALInformation() {
        initSourceBuffers();
        applyPositionChange();
    }

    private void calculateDistance() {
        if (listenerPosition != null) {
            double dX = position.x - listenerPosition.get(0);
            double dY = position.y - listenerPosition.get(1);
            double dZ = position.z - listenerPosition.get(2);
            distanceFromListener = (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        }
    }

    private void calculateGain() {
        if (attModel == SoundSystemConfig.ATTENUATION_LINEAR) {
            if (distanceFromListener <= 0.0f) {
                gain = 1.0f;
            } else if (distanceFromListener >= distOrRoll) {
                gain = 0.0f;
            } else {
                gain = 1.0f - distanceFromListener / distOrRoll;
            }
            if (gain > 1.0f) {
                gain = 1.0f;
            }
            if (gain < 0.0f) {
                gain = 0.0f;
            }
        } else {
            gain = 1.0f;
        }
    }

    private boolean checkALError() {
        String message = LibraryLWJGLOpenAL.describeALError(AL10.alGetError());
        if (message == null) {
            return false;
        }
        errorMessage(message);
        return true;
    }
}
