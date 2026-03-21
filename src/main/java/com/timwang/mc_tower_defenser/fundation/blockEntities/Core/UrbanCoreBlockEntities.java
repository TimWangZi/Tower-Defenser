package com.timwang.mc_tower_defenser.fundation.blockEntities.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.system.GlobalTowerManager;
import com.timwang.mc_tower_defenser.fundation.system.LocalTowerManagerBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class UrbanCoreBlockEntities extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation DEPLOY_ANIM = RawAnimation.begin()
                    .thenPlay("UrbanCore_Start")
                    .thenLoop("UrbanCore_idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean registeredToTowerManager = false;

    public UrbanCoreBlockEntities(BlockPos pos, BlockState state) {
         super(ModBlockEntities.URBAN_CORE.get(), pos, state);

     }

    @Override
    public void onLoad() {
        super.onLoad();
        // 测试: 方块实体加入世界时自动注册到全局塔管理器
        if (!registeredToTowerManager && this.level != null && !this.level.isClientSide()) {
            GlobalTowerManager manager = GlobalTowerManager.getInstance();
            LocalTowerManagerBase testNation = manager.getOrCreateNation("test", "test");
            manager.registerTower(testNation, this);
            registeredToTowerManager = true;
            MinecraftTowerDefenser.LOGGER.info("[test] UrbanCore registered at {} during onLoad", this.getBlockPos());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }

    protected <E extends UrbanCoreBlockEntities> PlayState deployAnimController(final AnimationState<E> state) {
        return state.setAndContinue(DEPLOY_ANIM);
    }

    public boolean check_territory(BlockPos pos) {
        return this.getBlockPos().closerThan(pos, 10.0);
    }

    @Override
    @Nullable
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
