package com.timwang.mc_tower_defenser.fundation.blockEntities.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.system.GlobalTowerManager;
import com.timwang.mc_tower_defenser.fundation.system.LocalTowerManagerBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
        
        // 客户端：打开创建国家菜单
        if (this.level != null && this.level.isClientSide) {
            this.openCreateCountryScreen();
        }
        
        // 服务端：注册到全局管理器
        if (this.level instanceof ServerLevel serverLevel) {
            GlobalTowerManager manager = GlobalTowerManager.get(serverLevel);
            // 测试: 默认将 UrbanCore 归入 test 阵营，真实项目可改为玩家创建结果
            LocalTowerManagerBase nation = manager.getOrCreateNation("system", "test");
            
            if (manager.registerTower(nation, this.worldPosition, "system")) {
                MinecraftTowerDefenser.LOGGER.info("[test] UrbanCore registered at {} during onLoad", this.getBlockPos());
            }
        }
    }

    /**
     * 打开创建国家的菜单
     * 仅在客户端执行
     */
    private void openCreateCountryScreen() {
        try {
            // 获取 Minecraft 客户端实例
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            
            // 检查玩家是否存在
            if (minecraft.player == null) {
                return;
            }
            
            // 打开创建国家的菜单
            minecraft.setScreen(new com.timwang.mc_tower_defenser.fundation.gui.Screen.CreateCountryScreen(minecraft.screen));
            
            MinecraftTowerDefenser.LOGGER.info("[UrbanCore] CreateCountryScreen opened at {}", this.getBlockPos());
        } catch (Exception e) {
            MinecraftTowerDefenser.LOGGER.error("[UrbanCore] Failed to open CreateCountryScreen", e);
        }
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel serverLevel) {
            boolean removed = GlobalTowerManager.get(serverLevel).unregisterTower(this.worldPosition);
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
