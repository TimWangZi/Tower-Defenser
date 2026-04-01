package com.timwang.mc_tower_defenser.fundation.entities.Mobs;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import com.timwang.mc_tower_defenser.fundation.utils.ai.goal.AttackOtherGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 普通士兵实体。
 * 已停止注册，保留该类仅用于兼容旧逻辑与后续迁移。
 */
@Deprecated
public class NormalSoldier extends PathfinderMob implements GeoEntity {
    private static final String NATION_TAG = "NationBelongTo";
    private static final EntityDataAccessor<String> DATA_NATION_BELONG_TO =
            SynchedEntityData.defineId(NormalSoldier.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public NormalSoldier(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        MinecraftTowerDefenser.LOGGER.info("Entity spawn");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_NATION_BELONG_TO, "");
    }

    @Override
    protected void registerGoals() {
        // [test-only] 验证用：普通士兵只会主动索敌非本阵营目标，后续可整段删除替换。
        this.targetSelector.addGoal(0, new AttackOtherGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
    }

    /** 返回实体基础属性表，供实体类型注册阶段挂载。 */
    public static AttributeSupplier.Builder createAttributes(){
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    /**
     * 实体出生时根据所在坐标判断其是否处于某个国家领地内。
     * 若出生点落在 UrbanCore 领地范围，则自动记录所属国家名称。
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        NationManager nation = GlobalNationManager.get(level.getLevel()).getNationByTerritory(this.blockPosition());
        setNationBelongTo(nation != null ? nation.getNationName() : "");
        return result;
    }

    /** 将阵营名称写入同步数据，供服务端、客户端和存档共用。 */
    public void setNationBelongTo(String nationName) {
        this.entityData.set(DATA_NATION_BELONG_TO, nationName == null ? "" : nationName);
    }

    /** 返回当前士兵所属阵营名；空字符串表示尚未归属于任何国家。 */
    public String getNationBelongTo() {
        return this.entityData.get(DATA_NATION_BELONG_TO);
    }

    /** 士兵是否已经绑定到某个国家。 */
    public boolean hasNationBelongTo() {
        return !getNationBelongTo().isBlank();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(NATION_TAG, getNationBelongTo());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNationBelongTo(tag.getString(NATION_TAG));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
