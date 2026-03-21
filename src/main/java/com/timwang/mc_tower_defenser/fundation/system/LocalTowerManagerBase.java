package com.timwang.mc_tower_defenser.fundation.system;

import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;

public class LocalTowerManagerBase {
    private String name = "";             // 阵营名称
    private ArrayList<String> member_uuid;// 属于这个阵营的玩家uuid

    private ArrayList<UrbanCoreBlockEntities> local_tower_list;

    public LocalTowerManagerBase(String nation_name) {
        this.name = nation_name;
        this.member_uuid = new ArrayList<>();
        this.local_tower_list = new ArrayList<>();
    }
    public void registerTower(String member_name, UrbanCoreBlockEntities tower){
        this.local_tower_list.add(tower);
    }
    public boolean isInTerritory(BlockPos pos) {
        for (UrbanCoreBlockEntities tower : this.local_tower_list) {
            if (tower.check_territory(pos)) {
                return true;
            }
        }
        return false;
    }
    public String getNationName() {
        return this.name;
    }
}
