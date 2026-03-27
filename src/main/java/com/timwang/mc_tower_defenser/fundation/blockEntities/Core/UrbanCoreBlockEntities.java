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

public class UrbanCoreBlockEntities extends BlockEntity implements GeoBlockEntity {
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

        // 服务端：注册到全局管理器
        if (this.level instanceof ServerLevel serverLevel) {
            GlobalNationManager manager = GlobalNationManager.get(serverLevel);
            NationManager nation = manager.getOrCreateNation("system", "test");
            
            if (manager.registerTower(nation, this.worldPosition, "system")) {
                MinecraftTowerDefenser.LOGGER.info("[test] UrbanCore registered at {} during onLoad", this.getBlockPos());
            }
        }
        if(this.level.isClientSide()){
            //Minecraft.getInstance().setScreen(new CreateCountryScreen(Minecraft.getInstance().screen));
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
