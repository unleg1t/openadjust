package com.adjustclient.service.prefs;

import com.adjustclient.Adjust;
import com.adjustclient.service.BackendMessageHandler;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import com.adjustclient.service.config.ConfigurationService;

public class SyncedPreferenceService implements BackendMessageHandler {
    public List<SyncedPreference<?>> preferences = new ArrayList<>();
    public SyncedPreference<IntPreferenceCodec> field8;
    public SkinTexturePreference skinTexture;
    public SkinModelPreference skinModel;
    public SyncedPreference<BooleanPreferenceCodec> booleanPreference;
    public CapePreference cape;
    public SyncedPreference<StringListPreferenceCodec> scripts;
    public Timer syncTimer = new Timer();
    public Object field36;

    public SyncedPreferenceService() {
        field8 = new SyncedPreference<>();
        field8.codec = new IntPreferenceCodec();
        field8.id = 0;

        skinTexture = new SkinTexturePreference();
        skinTexture.codec = new StringPreferenceCodec();
        skinTexture.condition = PARTY_AVAILABLE;
        skinTexture.id = 1;

        skinModel = new SkinModelPreference();
        EnumPreferenceCodec<SkinModel> skinModelCodec = new EnumPreferenceCodec<>();
        skinModelCodec.choices = SkinModel.values();
        skinModelCodec.value = com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT != null
                && com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.invites != null
                        ? com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.invites.getValue()
                        : SkinModel.EVERYONE;
        skinModel.codec = skinModelCodec;
        skinModel.id = 2;

        booleanPreference = new SyncedPreference<>();
        booleanPreference.codec = new BooleanPreferenceCodec();
        booleanPreference.id = 3;

        cape = new CapePreference();
        EnumPreferenceCodec<com.adjustclient.user.Cape> capeCodec = new EnumPreferenceCodec<>();
        capeCodec.choices = com.adjustclient.user.Cape.values();
        capeCodec.value = com.adjustclient.service.config.user.UserConfiguration.INSTANCE != null
                && com.adjustclient.service.config.user.UserConfiguration.INSTANCE.cape != null
                        ? com.adjustclient.service.config.user.UserConfiguration.INSTANCE.cape
                        : com.adjustclient.user.Cape.NOSTALGIA;
        cape.codec = capeCodec;
        cape.id = 4;

        scripts = new SyncedPreference<>();
        StringListPreferenceCodec scriptsCodec = new StringListPreferenceCodec();
        scriptsCodec.value = new String[0];
        scripts.codec = scriptsCodec;
        scripts.id = 5;

        preferences.add(field8);
        preferences.add(skinTexture);
        preferences.add(skinModel);
        preferences.add(booleanPreference);
        preferences.add(cape);
        preferences.add(scripts);
    }
    public static final BooleanSupplier PARTY_AVAILABLE = () -> Adjust.INSTANCE.screenStack.partyService.party != null
            || Adjust.INSTANCE.screenStack.partyService.syncingPreferences;

    public void onDisconnect() {
        for (SyncedPreference<?> preference : preferences) preference.codec.lastSentValue = null;
    }

    public void onConnect() { booleanPreference.codec.lastSentValue = null; }

    public void tick() {
        if (!syncTimer.hasElapsed(800L)) return;
        sendChanges();
        syncTimer.reset();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void sendChanges() {
        List<SyncedPreference<?>> changed = getChangedPreferences();
        if (changed == null) return;
        for (SyncedPreference<?> preference : changed) {
            PreferenceCodec codec = preference.codec;
            codec.lastSentValue = codec.value;
        }
    }

    @SuppressWarnings({"unused", "rawtypes", "unchecked"})
    private void vendorSendChanges() {
        if (Adjust.hydraConnection == null || !Adjust.hydraConnection.authenticated
                || !Adjust.hydraConnection.ready || !ConfigurationService.configurationReceived) return;
        List<SyncedPreference<?>> changed = getChangedPreferences();
        if (changed == null) return;
        SyncedPreferenceValue[] values = new SyncedPreferenceValue[changed.size()];
        for (int i = 0; i < values.length; i++) {
            SyncedPreference<?> preference = changed.get(i);
            PreferenceCodec codec = preference.codec;
            values[i] = new SyncedPreferenceValue(codec.encode(), preference.id);
            codec.lastSentValue = codec.value;
        }
        ServiceUtil.sendToBackend(com.adjustclient.backend.transport.HydraPacketEncoder.encodePreferences(values));
    }

    public List<SyncedPreference<?>> getChangedPreferences() {
        List<SyncedPreference<?>> changed = null;
        for (SyncedPreference<?> preference : preferences) {
            if (preference.condition != null && !preference.condition.getAsBoolean()) continue;
            if (preference.codec.isChanged()) {
                if (changed == null) changed = new ArrayList<>(1);
                changed.add(preference);
            }
        }
        return changed;
    }
}
