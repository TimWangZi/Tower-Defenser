package com.timwang.mc_tower_defenser.fundation.system;

import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import net.minecraft.core.BlockPos;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GlobalTowerManager {
    private static final GlobalTowerManager INSTANCE = new GlobalTowerManager();
    private static final ArrayList<LocalTowerManagerBase> nation_list = new ArrayList<>();
    private static final Map<String, LocalTowerManagerBase> player_nationality = new HashMap<>();

    private GlobalTowerManager() {

    }

    public static GlobalTowerManager getInstance() {
        return INSTANCE;
    }

    public synchronized err_type createNation(String player_name, String nation_name) {
        Iterator<LocalTowerManagerBase> it = nation_list.iterator();
        while (it.hasNext()) {
            if (Objects.equals(it.next().getNationName(), nation_name)) {
                return err_type.NATION_CREATE_ALREADYHAVE;
            }
        }
        LocalTowerManagerBase new_nation = new LocalTowerManagerBase(nation_name);
        nation_list.add(new_nation);
        if (player_name != null) {
            player_nationality.put(player_name, new_nation);
        }
        return err_type.NATION_CREATE_SUCCESS;
    }

    public synchronized Pair<err_type, LocalTowerManagerBase> getNation(String nation_name) {
        for (LocalTowerManagerBase nation : nation_list) {
            if (Objects.equals(nation.getNationName(), nation_name)) {
                return new Pair<>(err_type.NATION_GET_SUCCESS, nation);
            }
        }
        return new Pair<>(err_type.NATION_CANNOT_FIND, null);
    }

    public synchronized LocalTowerManagerBase getOrCreateNation(String player_name, String nation_name) {
        Optional<LocalTowerManagerBase> existing = nation_list.stream()
                .filter(nation -> Objects.equals(nation.getNationName(), nation_name))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        createNation(player_name, nation_name);
        return nation_list.get(nation_list.size() - 1);
    }

    public synchronized boolean registerTower(LocalTowerManagerBase nation, UrbanCoreBlockEntities tower) {
        if (nation == null || tower == null) {
            return false;
        }
        nation.registerTower("test", tower);
        return true;
    }

    public synchronized err_type deleteNation(String nation_name) {
        Pair<err_type, LocalTowerManagerBase> nation = getNation(nation_name);
        if (nation.getB() != null) {
            nation_list.remove(nation.getB());
        }
        return nation.getA();
    }

    public synchronized ArrayList<LocalTowerManagerBase> getNationList() {
        return nation_list;
    }

    public synchronized boolean isInAnyTerritory(BlockPos pos) {
        for (LocalTowerManagerBase nation : nation_list) {
            if (nation.isInTerritory(pos)) {
                return true;
            }
        }
        return false;
    }

    enum err_type {
        NATION_CREATE_ALREADYHAVE,
        NATION_CREATE_SUCCESS,
        NATION_CANNOT_FIND,
        NATION_GET_SUCCESS
    }
}
