package com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock;

import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/*
* 工作方块基类
* 包含关系管理，招募，注册等
* 动画与渲染需要在其子类中实现
* */
public class WorkBlockEntities extends BlockEntity {
    private String TYPE="";
    private String NATION="";
    public WorkBlockEntities(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    public boolean registerWorkBlock(ServerLevel level, ServerPlayer player, String work_block_type) {
        NationManager nation = GlobalNationManager.get(level).getNationByPlayer(player.getName().getString());
        if(nation.isInTerritory(getBlockPos())) {
            nation.registerWorkNode(getBlockPos());
            TYPE = work_block_type;
            NATION = nation.getNationName();
            return true;
        }else {
            return false;
        }
    }
    public String getTYPE(){return TYPE;}
    public String getNationName(){return NATION;}
}
