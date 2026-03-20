package com.timwang.mc_tower_defenser.fundation.system;

import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;

public class LocalTowerManagerBase {
    private String name = "";             // 阵营名称
    private ArrayList<String> member_uuid;// 属于这个阵营的玩家uuid

    private ArrayList<UrbanCoreBlockEntities> local_tower_list;

    public LocalTowerManagerBase(String nation_name) {
        this.name = nation_name;
    }
    public void registerTower(String member_name, UrbanCoreBlockEntities tower){
        this.local_tower_list.add(tower);
    }
    public boolean isInTerritory(BlockPos pos) {
        boolean tag = false;
        Iterator<UrbanCoreBlockEntities> it = this.local_tower_list.iterator();
        while (it.hasNext()) {
            tag |= it.next().check_territory(pos);
        }
        return tag;
    }
    public String getNationName() {
        return this.name;
    }
}
