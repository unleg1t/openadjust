package com.adjustclient.managers.telemetry;

import com.adjustclient.managers.telemetry.event.*;
import java.util.HashMap;
import java.util.Map;

public final class TelemetryConstants {
    public static final Map<Class<? extends TelemetryRecord>, Integer> RECORD_IDS = new HashMap<>();

    static {
        RECORD_IDS.put(SessionStartRecord.class, 0);
        RECORD_IDS.put(HypixelJoinRecord.class, 1);
        RECORD_IDS.put(EmptyRecord.class, 2);
        RECORD_IDS.put(ServerAddressRecord.class, 3);
        RECORD_IDS.put(BanDetectRecord.class, 4);
        RECORD_IDS.put(PlayerNameRecord.class, 5);
        RECORD_IDS.put(GameModeRecord.class, 6);
        RECORD_IDS.put(GameStartRecord.class, 7);
        RECORD_IDS.put(GameStateRecord.class, 8);
        RECORD_IDS.put(AntiVoidRecord.class, 9);
        RECORD_IDS.put(EntityNameRecord.class, 10);
        RECORD_IDS.put(FloatPairRecord.class, 11);
        RECORD_IDS.put(EmptyRecord2.class, 12);
        RECORD_IDS.put(BytesRecord.class, 13);
        RECORD_IDS.put(EmptyRecord4.class, 14);
        RECORD_IDS.put(GameEventRecordB.class, 15);
        RECORD_IDS.put(GameEventRecordC.class, 16);
        RECORD_IDS.put(GameEventRecordA.class, 17);
        RECORD_IDS.put(EmptyRecord3.class, 18);
        RECORD_IDS.put(BlockBreakRecord.class, 19);
        RECORD_IDS.put(FeatureUsedRecord.class, 20);
    }
}
