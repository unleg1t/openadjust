package com.adjustclient.core.gui.clickgui.setting;

import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefenseBlockType;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefensePlacement;
record BedBlockKey(BedDefenseBlockType blockType, int blockId) {
}
