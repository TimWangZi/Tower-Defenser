package com.timwang.mc_tower_defenser.fundation.blockEntities.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.gui.Screen.CreateCountryScreen;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * UrbanCore 的方块实体。
 * 负责注册/注销国家据点，并给 Geckolib 提供动画控制入口。
 */
public class UrbanCoreBlockEntities extends BlockEntity implements GeoBlockEntity {
    // 部署动画播完后转入待机循环，适合表现据点落地后的持续运作状态。
    protected static final RawAnimation DEPLOY_ANIM = RawAnimation.begin()
                    .thenPlay("UrbanCore_Start")
                    .thenLoop("UrbanCore_idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public UrbanCoreBlockEntities(BlockPos pos, BlockState state) {
         super(ModBlockEntities.URBAN_CORE.get(), pos, state);

     }

    @Override
    public void onLoad() {
        super.onLoad();

        // 只在服务端操作世界级国家数据，避免客户端误改状态。
        if (this.level instanceof ServerLevel serverLevel) {
            GlobalNationManager manager = GlobalNationManager.get(serverLevel);
            NationManager nation = manager.getOrCreateNation("system", "test");
            
            if (manager.registerTower(nation, this.worldPosition, "system")) {
                MinecraftTowerDefenser.LOGGER.info("[test] UrbanCore registered at {} during onLoad", this.getBlockPos());
            }
        }
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel serverLevel) {
            boolean removed = GlobalNationManager.get(serverLevel).unregisterTower(this.worldPosition);
            if (removed) {
                MinecraftTowerDefenser.LOGGER.info("[test] UrbanCore {} removed from tower manager", this.getBlockPos());
            }
        }
        super.setRemoved();
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
