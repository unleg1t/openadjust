package com.adjustclient.module.impl.player.voicechat.audio;

import com.adjustclient.audio.device.*;
import com.adjustclient.audio.device.openal.*;
import com.adjustclient.audio.openal.*;
import com.adjustclient.util.MinecraftInstance;
import java.util.*;
import java.util.concurrent.Future;

public final class VoiceAudioManager implements MinecraftInstance {
    public final List<AudioDeviceManager> backends = new ArrayList<>();
    public final AudioFormat captureFormat = new AudioFormat(ChannelMode.MONO, 48000);
    public final AudioFormat outputFormat = new AudioFormat(ChannelMode.STEREO, 48000);
    public final MicrophoneWorker microphone;
    public final SpeakerWorker speaker;
    public OpenAlCaptureDevice captureDevice;
    public OpenAlOutputDevice outputDevice;

    public VoiceAudioManager() {
        microphone = new MicrophoneWorker(this, 960);
        speaker = new SpeakerWorker(this, 960);
        backends.clear();
        backends.add(new AudioDeviceManager());
        selectInitialDevices();
    }

    public void refresh(DeviceType type) {
        switch (type) { case INPUT -> refreshInput(); case OUTPUT -> refreshOutput(); }
    }

    public boolean isMicrophoneReady() {
        return captureDevice != null && captureDevice.isOpen() && microphone.loopFuture != null
                && !microphone.loopFuture.isDone() && microphone.isEncoderOpen();
    }

    public void setCaptureDevice(OpenAlCaptureDevice device) throws AudioDeviceException {
        if (captureDevice != null && captureDevice.info.equals(device.info)) return;
        if (captureDevice != null && captureDevice.isOpen()) {
            OpenAlCaptureDevice old = captureDevice;
            microphone.tasks.add(() -> {
                try {
                    if (old.isOpen()) {
                        org.lwjgl.openal.ALC11.alcCaptureStop(old.device.handle);
                        OpenAlErrors.checkAlcError(old.device.handle);
                        old.device.started = false;
                        old.device.close();
                    }
                } catch (Exception ignored) { }
            });
        }
        captureDevice = device;
        reopenCapture(device);
    }

    public void setOutputDevice(OpenAlOutputDevice device) throws AudioDeviceException {
        if (outputDevice != null && outputDevice.info.equals(device.info)) return;
        if (outputDevice != null && outputDevice.device != null && outputDevice.device.handle != 0L
                && outputDevice.context != null && outputDevice.context.handle != 0L) {
            OpenAlOutputDevice old = outputDevice;
            speaker.tasks.add(() -> {
                try { old.close(); } catch (Exception ignored) { }
            });
        }
        outputDevice = device;
        reopenOutput(device);
    }

    public void setDevice(AudioDevice device) throws AudioDeviceException {
        if (device instanceof OpenAlCaptureDevice capture) setCaptureDevice(capture);
        else setOutputDevice((OpenAlOutputDevice) device);
    }

    public AudioDeviceManager getBackend(String id) {
        for (AudioDeviceManager backend : backends) if ("openal".equals(id)) return backend;
        return null;
    }

    public DeviceInfo findDevice(DeviceType type, String identifier) {
        String[] parts = identifier.split(":", 2);
        if (parts.length != 2) {
            for (AudioDeviceManager backend : backends)
                for (DeviceInfo device : backend.getDevices(type))
                    if (device.identifier.toString().equals(identifier)) return device;
            return null;
        }
        AudioDeviceManager backend = getBackend(parts[0]);
        if (backend == null) return null;
        for (DeviceInfo device : backend.getDevices(type)) if (device.identifier.name.equals(parts[1])) return device;
        return null;
    }

    public void reopenCapture(OpenAlCaptureDevice device) throws AudioDeviceException {
        if (!device.isOpen()) device.open(captureFormat, 500 * captureFormat.channels.channels * (captureFormat.sampleRate / 1000));
        mc.addScheduledTask(() -> {
            microphone.initialize();
        });
    }

    public void reopenOutput(OpenAlOutputDevice device) throws AudioDeviceException {
        if (device.device == null || device.device.handle == 0L || device.context == null || device.context.handle == 0L)
            device.open(outputFormat);
        mc.addScheduledTask(() -> {
            synchronized (speaker) {
                if (speaker.loopFuture != null && !speaker.loopFuture.isDone()) return;
                if (speaker.loopFuture != null) speaker.loopFuture.cancel(true);
                speaker.start();
            }
        });
    }

    public List<DeviceInfo> getDevices(DeviceType type) {
        List<DeviceInfo> devices = new ArrayList<>();
        for (AudioDeviceManager backend : backends) devices.addAll(backend.getDevices(type));
        return devices;
    }

    public void selectDevice(DeviceType type, String identifier) throws AudioDeviceException {
        DeviceInfo info = findDevice(type, identifier);
        if (info == null) throw new IllegalArgumentException("Device with identifier " + identifier + " not found");
        if (info.type != type) throw new IllegalArgumentException("Device with identifier " + identifier + " is not an " + type + " device");
        AudioDevice current = type == DeviceType.INPUT ? captureDevice : outputDevice;
        if (current != null) {
            DeviceInfo currentInfo = current instanceof OpenAlCaptureDevice capture ? capture.info : ((OpenAlOutputDevice) current).info;
            if (currentInfo.equals(info)) return;
        }
        setDevice(info.manager.createDevice(info));
    }

    public void refreshInput() {
        for (AudioDeviceManager backend : backends) {
            backend.inputDevices.clear();
            backend.refreshInputDevices(captureFormat);
        }
        if (captureDevice != null && !isMicrophoneReady()) {
            try { reopenCapture(captureDevice); } catch (AudioDeviceException exception) { throw new RuntimeException(exception); }
        }
    }

    public void applySelection(DeviceType type, String identifier) throws AudioDeviceException { selectDevice(type, identifier); }

    public void refreshOutput() {
        for (AudioDeviceManager backend : backends) {
            backend.outputDevices.clear();
            backend.refreshOutputDevices(outputFormat);
        }
        if (outputDevice != null && (outputDevice.device == null || outputDevice.device.handle == 0L
                || outputDevice.context == null || outputDevice.context.handle == 0L
                || speaker.loopFuture == null || speaker.loopFuture.isDone())) {
            try { reopenOutput(outputDevice); } catch (AudioDeviceException exception) { throw new RuntimeException(exception); }
        }
    }

    public DeviceInfo getDefaultDevice(DeviceType type) {
        if (backends.isEmpty()) return null;
        String name = type == DeviceType.INPUT ? AlCaptureDevice.defaultName() : AlPlaybackDevice.defaultName();
        return new DeviceInfo(backends.get(0), new DeviceIdentifier("openal", name), name.replaceAll("OpenAL Soft on ", ""),
                type, type == DeviceType.INPUT ? captureFormat : outputFormat);
    }

    public void refreshAll() { refreshInput(); refreshOutput(); }

    public void selectInitialDevices() {
        refreshAll();
        try {
            applySelection(DeviceType.OUTPUT, getDevices(DeviceType.OUTPUT).getFirst().identifier.toString());
            applySelection(DeviceType.INPUT, getDevices(DeviceType.INPUT).getFirst().identifier.toString());
        } catch (Exception ignored) { }
        refreshAll();
    }
}
