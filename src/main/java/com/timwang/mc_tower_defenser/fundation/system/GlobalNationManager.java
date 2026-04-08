package com.timwang.mc_tower_defenser.fundation.system;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 世界级国家数据管理器。
 * 基于 SavedData 持久化全部国家、成员归属和 UrbanCore 注册信息。
 */
public class GlobalNationManager extends SavedData {
    private static final String DATA_NAME = "tower_manager";

    private static final List<NationManager> nationList = new ArrayList<>();           // 全部阵营列表
    private static Map<String, String> playerNationality = new HashMap<>();              // 玩家 -> 阵营名

    public GlobalNationManager() {
    }

    /** 世界第一次没有现成存档数据时，创建一个空管理器实例。 */
    public static GlobalNationManager create() {
        nationList.clear();
        playerNationality.clear();
        return new GlobalNationManager();
    }

    /** 从存档 NBT 还原国家列表与玩家归属索引。 */
    public static GlobalNationManager load(CompoundTag tag, HolderLookup.Provider provider) {
        GlobalNationManager manager = new GlobalNationManager();
        nationList.clear();
        playerNationality.clear();
        ListTag nations = tag.getList("Nations", Tag.TAG_COMPOUND);
        for (int i = 0; i < nations.size(); i++) {
            manager.nationList.add(NationManager.deserializeNBT(nations.getCompound(i)));
        }
        CompoundTag players = tag.getCompound("PlayerNationality");
        for (String key : players.getAllKeys()) {
            manager.playerNationality.put(key, players.getString(key));
        }
        return manager;
    }

    /** 获取当前维度对应世界共享的国家管理器。 */
    public static GlobalNationManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GlobalNationManager::create, GlobalNationManager::load, null),
                DATA_NAME);
    }

    private NationManager findNation(String nationName) {
        if (nationName == null) {
            return null;
        }
        for (NationManager nation : nationList) {
            if (Objects.equals(nation.getNationName(), nationName)) {
                return nation;
            }
        }
        return null;
    }

    // 判断玩家是否已有阵营
    public synchronized boolean hasNation(String playerName) {
        return getNationByPlayer(playerName) != null;
    }

    // 创建新阵营并记录玩家归属
    public synchronized err_type createNation(String player_name, String nation_name) {
        if (player_name == null || player_name.isBlank() || nation_name == null || nation_name.isBlank()) {
            return err_type.NATION_CREATE_ALREADYHAVE;
        }

        if (hasNation(player_name) || findNation(nation_name) != null) {
            return err_type.NATION_CREATE_ALREADYHAVE;
        }
        NationManager new_nation = new NationManager(nation_name);
        new_nation.addMember(player_name);
        nationList.add(new_nation);
        if (player_name != null) {
            playerNationality.put(player_name, nation_name);
        }
        setDirty();
        return err_type.NATION_CREATE_SUCCESS;
    }

    // 根据阵营名获取阵营（若不存在返回错误码）
    public synchronized Pair<err_type, NationManager> getNation(String nation_name) {
        NationManager nation = findNation(nation_name);
        if (nation != null) {
            return new Pair<>(err_type.NATION_GET_SUCCESS, nation);
        }
        return new Pair<>(err_type.NATION_CANNOT_FIND, null);
    }

    // 获取或创建指定阵营
    public synchronized NationManager getOrCreateNation(String player_name, String nation_name) {
        NationManager existing = findNation(nation_name);
        if (existing != null) {
            return existing;
        }
        createNation(player_name, nation_name);
        return findNation(nation_name);
    }

    // 将玩家绑定到阵营
    public synchronized void bindPlayerToNation(String playerName, NationManager nation) {
        if (playerName != null && nation != null) {
            boolean changed = !Objects.equals(playerNationality.put(playerName, nation.getNationName()), nation.getNationName());
            changed |= nation.addMember(playerName);
            if (changed) {
                setDirty();
            }
        }
    }

    // 根据玩家名获取阵营
    /**
     * 先查玩家到国家的索引映射，映射缺失时再回退到 NationManager 成员列表重建索引。
     * 这样能兼容旧存档或映射与成员列表不一致的情况。
     */
    public synchronized NationManager getNationByPlayer(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        String nationName = playerNationality.get(playerName);
        NationManager mappedNation = findNation(nationName);
        if (mappedNation != null) {
            return mappedNation;
        }

        if (nationName != null) {
            playerNationality.remove(playerName);
        }

        for (NationManager nation : nationList) {
            if (nation.hasMember(playerName)) {
                playerNationality.put(playerName, nation.getNationName());
                setDirty();
                return nation;
            }
        }

        if (nationName != null) {
            setDirty();
        }
        return null;
    }

    public synchronized NationManager getNationByName(String nationName) {
        return findNation(nationName);
    }

    // 将UrbanCore注册到指定阵营
    public synchronized boolean registerTower(NationManager nation, BlockPos pos, String memberName) {
        if (nation == null || pos == null) {
            return false;
        }

        boolean changed = nation.registerTower(memberName, pos);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    /** 向指定国家的工作图注册一个节点。 */
    public synchronized boolean registerWorkNode(NationManager nation, BlockPos pos) {
        if (nation == null || pos == null) {
            return false;
        }

        boolean changed = nation.registerWorkNode(pos);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    /** 从指定国家的工作图移除一个节点。 */
    public synchronized boolean unregisterWorkNode(NationManager nation, BlockPos pos) {
        if (nation == null || pos == null) {
            return false;
        }

        boolean changed = nation.unregisterWorkNode(pos);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    /** 在指定国家的工作图中新增一条有向边。 */
    public synchronized boolean connectWorkNodes(NationManager nation, BlockPos from, BlockPos to) {
        if (nation == null || from == null || to == null) {
            return false;
        }

        boolean changed = nation.connectWorkNodes(from, to);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    /** 从指定国家的工作图中删除一条有向边。 */
    public synchronized boolean disconnectWorkNodes(NationManager nation, BlockPos from, BlockPos to) {
        if (nation == null || from == null || to == null) {
            return false;
        }

        boolean changed = nation.disconnectWorkNodes(from, to);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    // 删除阵营
    public synchronized err_type deleteNation(String nation_name) {
        NationManager nation = findNation(nation_name);
        if (nation != null) {
            nationList.remove(nation);
            playerNationality.entrySet().removeIf(entry -> Objects.equals(entry.getValue(), nation_name));
            setDirty();
            return err_type.NATION_GET_SUCCESS;
        }
        return err_type.NATION_CANNOT_FIND;
    }

    // 获取所有阵营列表
    public synchronized List<NationManager> getNationList() {
        return new ArrayList<>(nationList);
    }

    // 检查指定坐标是否落入任意阵营领地
    public synchronized boolean isInAnyTerritory(BlockPos pos) {
        for (NationManager nation : nationList) {
            if (nation.isInTerritory(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据坐标查找所属领地的国家。
     * 如果多个国家领地发生重叠，当前返回遍历到的第一个国家。
     */
    public synchronized NationManager getNationByTerritory(BlockPos pos) {
        if (pos == null) {
            return null;
        }

        for (NationManager nation : nationList) {
            if (nation.isInTerritory(pos)) {
                return nation;
            }
        }
        return null;
    }

    // 从所有阵营中注销一个 UrbanCore 塔
    public synchronized boolean unregisterTower(BlockPos pos) {
        return unregisterTowerAndGetNation(pos) != null;
    }

    /**
     * 从所有阵营中注销一个 UrbanCore 塔，并返回实际发生变更的国家。
     * 当前默认一个塔只归属于一个国家，因此找到后会立即返回。
     */
    public synchronized NationManager unregisterTowerAndGetNation(BlockPos pos) {
        if (pos == null) {
            return null;
        }

        for (NationManager nation : nationList) {
            if (nation.unregisterTower(pos)) {
                setDirty();
                return nation;
            }
        }

        return null;
    }



    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag nations = new ListTag();
        for (NationManager nation : nationList) {
            nations.add(nation.serializeNBT());
        }
        tag.put("Nations", nations);

        CompoundTag players = new CompoundTag();
        for (Map.Entry<String, String> entry : playerNationality.entrySet()) {
            players.putString(entry.getKey(), entry.getValue());
        }
        tag.put("PlayerNationality", players);
        return tag;
    }

    public enum err_type {
        NATION_CREATE_ALREADYHAVE,
        NATION_CREATE_SUCCESS,
        NATION_CANNOT_FIND,
        NATION_GET_SUCCESS
    }
}
