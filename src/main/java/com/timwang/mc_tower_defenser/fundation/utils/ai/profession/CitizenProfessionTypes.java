package com.timwang.mc_tower_defenser.fundation.utils.ai.profession;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/**
 * 市民职业类型表。
 * 当前只保存可序列化的职业类型标识，并在实体加载后重建 Profession 实例。
 */
public final class CitizenProfessionTypes {
    public static final String FARMER = "farmer";
    private static final String LEGACY_DEFAULT_FARMER = "default_farmer";

    private CitizenProfessionTypes() {
    }

    @Nullable
    public static ProfessionBase<? extends CitizenEntity, ?> create(String professionTypeId, CitizenEntity citizen, ServerLevel level) {
        if (FARMER.equals(professionTypeId) || LEGACY_DEFAULT_FARMER.equals(professionTypeId)) {
            return new FarmerProfession(citizen, level);
        }

        return null;
    }
}
