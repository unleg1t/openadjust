package com.adjustclient.audio.device;

import java.util.ArrayList;
import java.util.List;
import com.adjustclient.audio.device.openal.OpenAlCaptureDevice;
import com.adjustclient.audio.device.openal.OpenAlOutputDevice;
import com.adjustclient.audio.openal.AlCaptureDevice;
import com.adjustclient.audio.openal.AlPlaybackDevice;

public final class AudioDeviceManager {
    public List<DeviceInfo> inputDevices = new ArrayList<>();
    public List<DeviceInfo> outputDevices = new ArrayList<>();
    public Object field12;
    public AudioDevice createDevice(DeviceInfo info) {
        return switch (info.type) {
            case INPUT -> new OpenAlCaptureDevice(info);
            case OUTPUT -> new OpenAlOutputDevice(info);
        };
    }
    public List<DeviceInfo> getDevices(DeviceType type) {
        return switch (type) {
            case INPUT -> inputDevices;
            case OUTPUT -> outputDevices;
        };
    }
    public List<DeviceInfo> enumerate(DeviceType type, AudioFormat format) {
        List<DeviceInfo> result = new ArrayList<>();
        List<String> names;
        if (type == DeviceType.INPUT) {
            names = AlCaptureDevice.deviceNames();
        } else if (type == DeviceType.OUTPUT) {
            names = AlPlaybackDevice.deviceNames();
        } else {
            return result;
        }
        for (String name : names) {
            result.add(new DeviceInfo(this, new DeviceIdentifier("openal", name), name.replaceAll("OpenAL Soft on ", ""), type, format));
        }
        return result;
    }
    public void refreshInputDevices(AudioFormat format) {
        List<DeviceInfo> discovered = new ArrayList<>();
        for (String name : AlCaptureDevice.deviceNames()) {
            discovered.add(new DeviceInfo(this, new DeviceIdentifier("openal", name), name.replaceAll("OpenAL Soft on ", ""), DeviceType.INPUT, format));
        }
        for (DeviceInfo device : discovered) if (!inputDevices.contains(device)) inputDevices.add(device);
    }
    public void refreshOutputDevices(AudioFormat format) {
        for (DeviceInfo device : enumerate(DeviceType.OUTPUT, format)) if (!outputDevices.contains(device)) outputDevices.add(device);
    }
}
