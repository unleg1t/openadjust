package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseBlockType;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefensePlacement;
record BedBlockKey(BedDefenseBlockType blockType, int blockId) {
}
