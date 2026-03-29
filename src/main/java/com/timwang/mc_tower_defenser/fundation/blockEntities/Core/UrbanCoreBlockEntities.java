package com.timwang.mc_tower_defenser.fundation.blockEntities.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.network.NationSyncService;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * UrbanCore 的方块实体。
 * 负责注册/注销国家据点，并给 Geckolib 提供动画控制入口。
 */
public class UrbanCoreBlockEntities extends BlockEntity implements GeoBlockEntity {
    private static final String OWNER_PLAYER_TAG = "OwnerPlayerName";
    private static final String NATION_NAME_TAG = "NationName";

    // 部署动画播完后转入待机循环，适合表现据点落地后的持续运作状态。
    protected static final RawAnimation DEPLOY_ANIM = RawAnimation.begin()
                    .thenPlay("UrbanCore_Start")
                    .thenLoop("UrbanCore_idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private String ownerPlayerName = "";
    private String nationName = "";

    public UrbanCoreBlockEntities(BlockPos pos, BlockState state) {
         super(ModBlockEntities.URBAN_CORE.get(), pos, state);

     }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.level instanceof ServerLevel serverLevel) {
            registerBoundNation(serverLevel);
        }
    }

    public void bindToNation(String ownerPlayerName, String nationName) {
        String nextOwnerName = ownerPlayerName == null ? "" : ownerPlayerName;
        String nextNationName = nationName == null ? "" : nationName;
        if (this.ownerPlayerName.equals(nextOwnerName) && this.nationName.equals(nextNationName)) {
            return;
        }

        this.ownerPlayerName = nextOwnerName;
        this.nationName = nextNationName;
        this.setChanged();
    }

    public void registerBoundNation(ServerLevel serverLevel) {
        GlobalNationManager manager = GlobalNationManager.get(serverLevel);
        NationManager nation = resolveBoundNation(manager);
        if (nation == null) {
            return;
        }

        if (manager.registerTower(nation, this.worldPosition, ownerPlayerName)) {
            this.setChanged();
            NationSyncService.syncNationMembers(serverLevel.getServer(), nation);
            MinecraftTowerDefenser.LOGGER.info(
                    "[test] UrbanCore registered at {} for nation {}",
                    this.getBlockPos(),
                    nation.getNationName()
            );
        }
    }

    public void unregisterBoundNation(ServerLevel serverLevel) {
        NationManager affectedNation = GlobalNationManager.get(serverLevel).unregisterTowerAndGetNation(this.worldPosition);
        if (affectedNation != null) {
            NationSyncService.syncNationMembers(serverLevel.getServer(), affectedNation);
            MinecraftTowerDefenser.LOGGER.info("[test] UrbanCore {} removed from tower manager", this.getBlockPos());
        }
    }

    private NationManager resolveBoundNation(GlobalNationManager manager) {
        if (!nationName.isBlank()) {
            return manager.getNationByName(nationName);
        }

        if (ownerPlayerName.isBlank()) {
            return null;
        }

        NationManager nation = manager.getNationByPlayer(ownerPlayerName);
        if (nation != null && !nation.getNationName().equals(this.nationName)) {
            this.nationName = nation.getNationName();
            this.setChanged();
        }
        return nation;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(OWNER_PLAYER_TAG, this.ownerPlayerName);
        tag.putString(NATION_NAME_TAG, this.nationName);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerPlayerName = tag.getString(OWNER_PLAYER_TAG);
        this.nationName = tag.getString(NATION_NAME_TAG);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }

    /** 控制 UrbanCore 的默认部署/待机动画。 */
    protected <E extends UrbanCoreBlockEntities> PlayState deployAnimController(final AnimationState<E> state) {
        return state.setAndContinue(DEPLOY_ANIM);
    }

    /** 判断某个坐标是否位于当前 UrbanCore 的领地半径内。 */
    public boolean check_territory(BlockPos pos) {
        return this.getBlockPos().closerThan(pos, 10.0);
    }

    @Override
    @Nullable
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
