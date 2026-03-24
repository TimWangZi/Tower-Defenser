package com.timwang.mc_tower_defenser.fundation.system;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

// 本地塔管理器：维护单个国家/阵营的塔列表和成员
public class NationManager {
    private final String name;                   // 阵营名称
    private final List<String> memberNames;      // 属于这个阵营的玩家名
    private final List<BlockPos> towerPositions; // 已注册的UrbanCore塔坐标

    public NationManager(String nation_name) {
        this.name = nation_name;
        this.memberNames = new ArrayList<>();
        this.towerPositions = new ArrayList<>();
    }

    // 注册一个塔到当前阵营，同时记录玩家（若提供）
    public void registerTower(String member_name, BlockPos pos){
        if (member_name != null && !member_name.isEmpty() && !memberNames.contains(member_name)) {
            memberNames.add(member_name);
        }
        if (pos != null && !towerPositions.contains(pos)) {
            towerPositions.add(pos);
        }
    }

    // 注销一个塔（方块实体被移除时调用）
    public boolean unregisterTower(BlockPos pos) {
        return pos != null && towerPositions.remove(pos);
    }

    // 检查坐标是否落在任意塔的领地范围内
    public boolean isInTerritory(BlockPos pos) {
        for (BlockPos tower : this.towerPositions) {
            if (tower.closerThan(pos, 10.0)) {
                return true;
            }
        }
        return false;
    }

    public String getNationName() {
        return this.name;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public List<BlockPos> getTowerPositions() {
        return towerPositions;
    }

    // 序列化当前阵营信息，用于 SavedData
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", this.name);

        ListTag members = new ListTag();
        for (String member : memberNames) {
            members.add(StringTag.valueOf(member));
        }
        tag.put("Members", members);

        ListTag towers = new ListTag();
        for (BlockPos pos : towerPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            towers.add(posTag);
        }
        tag.put("Towers", towers);
        return tag;
    }

    // 反序列化
    public static NationManager deserialize(CompoundTag tag) {
        NationManager nation = new NationManager(tag.getString("Name"));
        ListTag members = tag.getList("Members", Tag.TAG_STRING);
        for (int i = 0; i < members.size(); i++) {
            nation.memberNames.add(members.getString(i));
        }
        ListTag towers = tag.getList("Towers", Tag.TAG_COMPOUND);
        for (int i = 0; i < towers.size(); i++) {
            CompoundTag posTag = towers.getCompound(i);
            nation.towerPositions.add(BlockPos.of(posTag.getLong("Pos")));
        }
        return nation;
    }
}
