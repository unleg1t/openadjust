package com.adjustclient.core.audio.device.openal;

import com.adjustclient.core.audio.device.*;
import com.adjustclient.core.audio.openal.*;
import org.lwjgl.openal.ALC11;

public class OpenAlCaptureDevice extends AudioDevice {
    public DeviceInfo info;
    public AlCaptureDevice device;
    public float[] captureBuffer;
    public Object field16;
    public OpenAlCaptureDevice(DeviceInfo info) { this.info = info; }
    public void read(short[] samples, int count) {
        if (!isOpen()) throw new IllegalStateException("Device is not open");
        int available = device.availableSamples();
        if (count > available) throw new IllegalStateException("Not enough samples available: Available: " + available + ", requested: " + count);
        if (captureBuffer == null || captureBuffer.length < count) captureBuffer = new float[count];
        ALC11.alcCaptureSamples(device.handle, captureBuffer, count);
        OpenAlErrors.checkAlcError(device.handle);
        for (int i = 0; i < count; i++) samples[i] = (short) (int) (32767.0F * captureBuffer[i]);
    }
    public void start() {
        if (!isOpen()) throw new IllegalStateException("Device is not open");
        ALC11.alcCaptureStart(device.handle);
        OpenAlErrors.checkAlcError(device.handle);
        device.started = true;
    }
    public int availableSamples() { return !isOpen() ? 0 : device.availableSamples(); }
    public boolean isOpen() { return device != null && device.handle != 0L; }
    public void open(AudioFormat format, int bufferSize) throws AudioDeviceException {
        if (isOpen()) throw new AudioDeviceException("Device is already open");
        device = new AlCaptureDevice();
        if (device.open(info.identifier.name, format, bufferSize)) return;
        com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true, "Failed to find target device {}, using default", info.identifier);
        if (!device.open(AlCaptureDevice.defaultName(), format, bufferSize))
            throw new AudioDeviceException("Couldn't find any device with the specified format: " + format);
    }
}

