package com.adjustclient.modules.impl.player.voicechat.playback;

import com.adjustclient.utilities.Timer;

public class AudioLevelMeter {
    public float[] levels = new float[0];
    public Timer timer = new Timer(0L);
    public boolean secondHalf;
}
