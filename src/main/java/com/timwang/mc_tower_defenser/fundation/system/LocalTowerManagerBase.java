package com.timwang.mc_tower_defenser.fundation.system;

import net.minecraft.world.entity.Entity;
import java.util.Map;
import java.util.function.Supplier;

public class LocalTowerManagerBase {
    private String name = "";//阵营名称
    Map<Entity ,String> member;

    public void LocalTowerManagerBase(String regime_name) {
        this.name = regime_name;
    }
    public Supplier<Entity> register_member(String member_name, )
    public String getRegimeName() {
        return this.name;
    }

}
