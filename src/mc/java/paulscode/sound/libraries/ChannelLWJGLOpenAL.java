package paulscode.sound.libraries;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import paulscode.sound.Channel;

public class ChannelLWJGLOpenAL extends Channel {
    public IntBuffer ALSource;
    public int ALformat;
    public int sampleRate;
    public float millisPreviouslyPlayed = 0.0f;

    public ChannelLWJGLOpenAL(int type, IntBuffer src) {
        super(type);
        libraryType = LibraryLWJGLOpenAL.class;
        ALSource = src;
    }

    @Override
    public void cleanup() {
        if (ALSource != null) {
            try {
                AL10.alSourceStopv(ALSource);
                AL10.alGetError();
            } catch (Exception e) {
            }
            if (channelType == 1) {
                // Stopping marks every queued buffer as processed; release them before the source goes away.
                try {
                    deleteProcessedBuffers(ALSource.get(0));
                } catch (Exception e) {
                }
            }
            try {
                AL10.alDeleteSources(ALSource);
                AL10.alGetError();
            } catch (Exception e) {
            }
            ALSource.clear();
        }
        ALSource = null;
        super.cleanup();
    }

    private static void deleteBuffersSafe(IntBuffer buffers) {
        if (buffers == null || buffers.remaining() == 0) {
            return;
        }
        try {
            AL10.alDeleteBuffers(buffers);
            AL10.alGetError();
        } catch (Exception ignored) {
        }
    }

    /**
     * Unqueues and deletes every processed buffer of the given streaming source.
     *
     * @return number of buffers released, or -1 if OpenAL reported an error
     */
    private int deleteProcessedBuffers(int src) {
        int processed = AL10.alGetSourcei(src, AL10.AL_BUFFERS_PROCESSED);
        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            return -1;
        }
        if (processed <= 0) {
            return 0;
        }
        IntBuffer finished = BufferUtils.createIntBuffer(processed);
        AL10.alSourceUnqueueBuffers(src, finished);
        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            return -1;
        }
        AL10.alDeleteBuffers(finished);
        AL10.alGetError();
        return processed;
    }

    public boolean attachBuffer(IntBuffer buf) {
        if (errorCheck(channelType != 0, "Sound buffers may only be attached to normal sources.")) {
            return false;
        }
        AL10.alSourcei(ALSource.get(0), AL10.AL_BUFFER, buf.get(0));
        if (attachedSource != null && attachedSource.soundBuffer != null && attachedSource.soundBuffer.audioFormat != null) {
            setAudioFormat(attachedSource.soundBuffer.audioFormat);
        }
        return checkALError();
    }

    @Override
    public void setAudioFormat(AudioFormat audioFormat) {
        int soundFormat = LibraryLWJGLOpenAL.resolveALFormat(audioFormat);
        if (soundFormat == LibraryLWJGLOpenAL.FORMAT_ILLEGAL_SAMPLE_SIZE) {
            errorMessage("Illegal sample size in method 'setAudioFormat'");
            return;
        }
        if (soundFormat == LibraryLWJGLOpenAL.FORMAT_UNSUPPORTED_CHANNELS) {
            errorMessage("Audio data neither mono nor stereo in method 'setAudioFormat'");
            return;
        }
        ALformat = soundFormat;
        sampleRate = (int) audioFormat.getSampleRate();
    }

    public void setFormat(int format, int rate) {
        ALformat = format;
        sampleRate = rate;
    }

    @Override
    public boolean preLoadBuffers(LinkedList<byte[]> bufferList) {
        if (errorCheck(channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        if (errorCheck(bufferList == null, "Buffer List null in method 'preLoadBuffers'")) {
            return false;
        }

        IntBuffer streamBuffers;
        boolean playing = playing();
        if (playing) {
            AL10.alSourceStop(ALSource.get(0));
            checkALError();
        }

        int processed = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);
        if (processed > 0) {
            streamBuffers = BufferUtils.createIntBuffer(processed);
            AL10.alSourceUnqueueBuffers(ALSource.get(0), streamBuffers);
            if (errorCheck(checkALError(), "Error unqueuing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
            AL10.alDeleteBuffers(streamBuffers);
            if (errorCheck(checkALError(), "Error clearing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
        }

        if (playing) {
            AL10.alSourcePlay(ALSource.get(0));
            checkALError();
        }

        streamBuffers = BufferUtils.createIntBuffer(bufferList.size());
        AL10.alGenBuffers(streamBuffers);
        if (errorCheck(checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
            return false;
        }

        ByteBuffer byteBuffer = null;
        for (int i = 0; i < bufferList.size(); i++) {
            byteBuffer = BufferUtils.createByteBuffer(bufferList.get(i).length);
            byteBuffer.put(bufferList.get(i));
            byteBuffer.flip();
            try {
                AL10.alBufferData(streamBuffers.get(i), ALformat, byteBuffer, sampleRate);
            } catch (Exception e) {
                errorMessage("Error creating buffers in method 'preLoadBuffers'");
                printStackTrace(e);
                deleteBuffersSafe(streamBuffers);
                return false;
            }
            if (errorCheck(checkALError(), "Error creating buffers in method 'preLoadBuffers'")) {
                deleteBuffersSafe(streamBuffers);
                return false;
            }
        }

        try {
            AL10.alSourceQueueBuffers(ALSource.get(0), streamBuffers);
        } catch (Exception e) {
            errorMessage("Error queuing buffers in method 'preLoadBuffers'");
            printStackTrace(e);
            deleteBuffersSafe(streamBuffers);
            return false;
        }
        if (errorCheck(checkALError(), "Error queuing buffers in method 'preLoadBuffers'")) {
            deleteBuffersSafe(streamBuffers);
            return false;
        }

        AL10.alSourcePlay(ALSource.get(0));
        if (errorCheck(checkALError(), "Error playing source in method 'preLoadBuffers'")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean queueBuffer(byte[] buffer) {
        if (errorCheck(channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(buffer.length);
        byteBuffer.put(buffer);
        byteBuffer.flip();
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);

        AL10.alSourceUnqueueBuffers(ALSource.get(0), intBuffer);
        if (checkALError()) {
            return false;
        }

        if (AL10.alIsBuffer(intBuffer.get(0))) {
            millisPreviouslyPlayed += millisInBuffer(intBuffer.get(0));
        }
        checkALError();

        AL10.alBufferData(intBuffer.get(0), ALformat, byteBuffer, sampleRate);
        if (checkALError()) {
            return false;
        }

        AL10.alSourceQueueBuffers(ALSource.get(0), intBuffer);
        if (checkALError()) {
            return false;
        }
        return true;
    }

    @Override
    public int feedRawAudioData(byte[] buffer) {
        if (errorCheck(channelType != 1, "Raw audio data can only be fed to streaming sources.")) {
            return -1;
        }

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(buffer.length);
        byteBuffer.put(buffer);
        byteBuffer.flip();
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        int processed = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);
        if (processed > 0) {
            IntBuffer finished = BufferUtils.createIntBuffer(processed);
            AL10.alSourceUnqueueBuffers(ALSource.get(0), finished);
            if (errorCheck(checkALError(), "Error unqueuing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
            for (int i = 0; i < processed; i++) {
                int finishedBuffer = finished.get(i);
                if (AL10.alIsBuffer(finishedBuffer)) {
                    millisPreviouslyPlayed += millisInBuffer(finishedBuffer);
                }
            }
            checkALError();
            // Recycle the first finished buffer for the new data and release the rest, so that
            // exactly one buffer (holding the fresh data) is queued per call.
            intBuffer.put(0, finished.get(0));
            if (processed > 1) {
                finished.position(1);
                AL10.alDeleteBuffers(finished);
                finished.position(0);
                if (errorCheck(checkALError(), "Error clearing stream buffers in method 'feedRawAudioData'")) {
                    return -1;
                }
            }
        } else {
            AL10.alGenBuffers(intBuffer);
            if (errorCheck(checkALError(), "Error generating stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
        }

        AL10.alBufferData(intBuffer.get(0), ALformat, byteBuffer, sampleRate);
        if (checkALError()) {
            deleteBuffersSafe(intBuffer);
            return -1;
        }

        AL10.alSourceQueueBuffers(ALSource.get(0), intBuffer);
        if (checkALError()) {
            deleteBuffersSafe(intBuffer);
            return -1;
        }

        if (attachedSource != null && attachedSource.channel == this && attachedSource.active() && !playing()) {
            AL10.alSourcePlay(ALSource.get(0));
            checkALError();
        }
        return processed;
    }

    public float millisInBuffer(int alBufferi) {
        return (float) AL10.alGetBufferi(alBufferi, AL10.AL_SIZE)
                / (float) AL10.alGetBufferi(alBufferi, AL10.AL_CHANNELS)
                / ((float) AL10.alGetBufferi(alBufferi, AL10.AL_BITS) / 8.0f)
                / (float) sampleRate
                * 1000.0f;
    }

    @Override
    public float millisecondsPlayed() {
        float offset = (float) AL10.alGetSourcei(ALSource.get(0), AL11.AL_BYTE_OFFSET);
        float bytesPerFrame = 1.0f;
        switch (ALformat) {
            case AL10.AL_FORMAT_MONO8:
                bytesPerFrame = 1.0f;
                break;
            case AL10.AL_FORMAT_MONO16:
                bytesPerFrame = 2.0f;
                break;
            case AL10.AL_FORMAT_STEREO8:
                bytesPerFrame = 2.0f;
                break;
            case AL10.AL_FORMAT_STEREO16:
                bytesPerFrame = 4.0f;
                break;
            default:
                break;
        }
        offset = offset / bytesPerFrame / (float) sampleRate * 1000.0f;
        if (channelType == 1) {
            offset += millisPreviouslyPlayed;
        }
        return offset;
    }

    @Override
    public int buffersProcessed() {
        if (channelType != 1) {
            return 0;
        }
        int processed = AL10.alGetSourcei(ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);
        if (checkALError()) {
            return 0;
        }
        return processed;
    }

    @Override
    public void flush() {
        if (channelType != 1) {
            return;
        }
        int src = ALSource.get(0);
        int queued = AL10.alGetSourcei(src, AL10.AL_BUFFERS_QUEUED);
        if (checkALError()) {
            return;
        }
        if (queued > 0) {
            if (deleteProcessedBuffers(src) < 0) {
                return;
            }
            int state = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE);
            if (AL10.alGetSourcei(src, AL10.AL_BUFFERS_QUEUED) > 0
                    && state != AL10.AL_PLAYING
                    && state != AL10.AL_PAUSED) {
                // Buffers queued on a source that never started are not "processed"; stopping the
                // source marks them processed so they can be unqueued and deleted instead of orphaned.
                if (state == AL10.AL_INITIAL) {
                    AL10.alSourceStop(src);
                    AL10.alGetError();
                }
                if (deleteProcessedBuffers(src) < 0) {
                    return;
                }
                if (AL10.alGetSourcei(src, AL10.AL_BUFFERS_QUEUED) > 0) {
                    AL10.alSourcei(src, AL10.AL_BUFFER, AL10.AL_NONE);
                    if (checkALError()) {
                        return;
                    }
                }
            }
        }
        millisPreviouslyPlayed = 0.0f;
    }

    @Override
    public void close() {
        try {
            AL10.alSourceStop(ALSource.get(0));
            AL10.alGetError();
        } catch (Exception e) {
        }
        if (channelType == 1) {
            flush();
        }
    }

    @Override
    public void play() {
        AL10.alSourcePlay(ALSource.get(0));
        checkALError();
    }

    @Override
    public void pause() {
        AL10.alSourcePause(ALSource.get(0));
        checkALError();
    }

    @Override
    public void stop() {
        AL10.alSourceStop(ALSource.get(0));
        if (!checkALError()) {
            millisPreviouslyPlayed = 0.0f;
        }
    }

    @Override
    public void rewind() {
        if (channelType == 1) {
            return;
        }
        AL10.alSourceRewind(ALSource.get(0));
        if (!checkALError()) {
            millisPreviouslyPlayed = 0.0f;
        }
    }

    @Override
    public boolean playing() {
        int state = AL10.alGetSourcei(ALSource.get(0), AL10.AL_SOURCE_STATE);
        if (checkALError()) {
            return false;
        }
        return state == AL10.AL_PLAYING;
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
