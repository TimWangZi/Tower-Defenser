package com.timwang.mc_tower_defenser.fundation.blockEntities.Core;

import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
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

    public UrbanCoreBlockEntities(BlockPos pos, BlockState state) {
         super(ModBlockEntities.URBAN_CORE.get(), pos, state);
     }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }

    protected <E extends UrbanCoreBlockEntities> PlayState deployAnimController(final AnimationState<E> state) {
        return state.setAndContinue(DEPLOY_ANIM);
    }

    @Override
    @Nullable
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
