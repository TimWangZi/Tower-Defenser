package com.timwang.mc_tower_defenser.fundation.system;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// 全局塔管理器：维护所有阵营（LocalTowerManagerBase）及其塔实例，并持久化到存档,是服务器类
@Mod(value = MinecraftTowerDefenser.MODID, dist = Dist.DEDICATED_SERVER)
public class GlobalNationManager extends SavedData {
    private static final String DATA_NAME = "tower_manager";

    private static final List<NationManager> nationList = new ArrayList<>();           // 全部阵营列表
    private static Map<String, String> playerNationality = new HashMap<>();              // 玩家 -> 阵营名

    public GlobalNationManager() {
    }

    public static GlobalNationManager create() {
        return new GlobalNationManager();
    }

    public static GlobalNationManager load(CompoundTag tag, HolderLookup.Provider provider) {
        GlobalNationManager manager = new GlobalNationManager();
        ListTag nations = tag.getList("Nations", Tag.TAG_COMPOUND);
        for (int i = 0; i < nations.size(); i++) {
            manager.nationList.add(NationManager.deserialize(nations.getCompound(i)));
        }
        CompoundTag players = tag.getCompound("PlayerNationality");
        for (String key : players.getAllKeys()) {
            manager.playerNationality.put(key, players.getString(key));
        }
        return manager;
    }

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
        return playerNationality.containsKey(playerName);
    }

    // 创建新阵营并记录玩家归属
    public synchronized err_type createNation(String player_name, String nation_name) {
        if (findNation(nation_name) != null) {
            return err_type.NATION_CREATE_ALREADYHAVE;
        }
        NationManager new_nation = new NationManager(nation_name);
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
            playerNationality.put(playerName, nation.getNationName());
            setDirty();
        }
    }

    // 根据玩家名获取阵营
    public synchronized NationManager getNationByPlayer(String playerName) {
        return findNation(playerNationality.get(playerName));
    }

    // 将UrbanCore注册到指定阵营
    public synchronized boolean registerTower(NationManager nation, BlockPos pos, String memberName) {
        if (nation == null || pos == null) {
            return false;
        }
        nation.registerTower(memberName, pos);
        setDirty();
        return true;
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

    // 从所有阵营中注销一个 UrbanCore 塔
    public synchronized boolean unregisterTower(BlockPos pos) {
        boolean removed = false;
        for (NationManager nation : nationList) {
            removed |= nation.unregisterTower(pos);
        }
        if (removed) {
            setDirty();
        }
        return removed;
    }



    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag nations = new ListTag();
        for (NationManager nation : nationList) {
            nations.add(nation.serialize());
        }
        tag.put("Nations", nations);

        CompoundTag players = new CompoundTag();
        for (Map.Entry<String, String> entry : playerNationality.entrySet()) {
            players.putString(entry.getKey(), entry.getValue());
        }
        tag.put("PlayerNationality", players);
        return tag;
    }

    enum err_type {
        NATION_CREATE_ALREADYHAVE,
        NATION_CREATE_SUCCESS,
        NATION_CANNOT_FIND,
        NATION_GET_SUCCESS
    }
}
