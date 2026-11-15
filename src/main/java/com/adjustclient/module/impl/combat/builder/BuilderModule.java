package com.adjustclient.module.impl.combat.builder;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.ui.hud.toggled.ToggledList;
import com.adjustclient.module.impl.combat.builder.feature.BuilderFeature;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseFeature;
import com.adjustclient.module.impl.combat.builder.feature.blockin.BlockInFeature;
import com.adjustclient.module.impl.combat.builder.feature.headhitter.HeadHitterFeature;
import java.util.ArrayList;
import java.util.List;

public final class BuilderModule extends Module {
    public static BuilderModule INSTANCE;
    public final ArrayList<BuilderFeature> features;
    public final BuilderPlacer placer;
    private String suffix;
    public final BlockInFeature blockIn;
    public final BedDefenseFeature bedDefense;
    public final HeadHitterFeature headHitter;

    public BuilderModule() {
        super("Builder", Category.COMBAT);
        INSTANCE = this;
        features = new ArrayList<>();
        placer = new BuilderPlacer(this);
        bedDefense = new BedDefenseFeature(this, "module.builder.beddefense", "Builder Bed Defense");
        blockIn = new BlockInFeature(this, "module.builder.blockin", "Builder Block In");
        headHitter = new HeadHitterFeature(this, "module.builder.headhitter", "Builder Head Hitter");
        features.add(bedDefense);
        features.add(blockIn);
        features.add(headHitter);
    }

    @Override public void onEnable() { updateSuffix(); }

    @Override public void onDisable() {
        for (BuilderFeature feature : features) feature.deactivate();
    }

    @Override public String getSuffix() { return suffix; }

    public void updateSuffix() {
        StringBuilder result = new StringBuilder();
        for (BuilderFeature feature : features) {
            if (!feature.active) continue;
            if (result.length() != 0) result.append(", ");
            result.append(feature.name);
        }
        suffix = result.length() == 0 ? null : result.toString();
        if (ToggledList.INSTANCE != null) ToggledList.INSTANCE.dirty = true;
    }
}
