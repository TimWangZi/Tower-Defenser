package com.timwang.mc_tower_defenser.fundation.system;

import org.checkerframework.checker.units.qual.A;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class GlobalTowerManager {
    private static ArrayList<LocalTowerManagerBase> nation_list;
    private static Map<String,LocalTowerManagerBase> player_nationality;
    GlobalTowerManager() {

    }

    public err_type createNation(String player_name,String nation_name) {
        Iterator<LocalTowerManagerBase> it = this.nation_list.iterator();
        while (it.hasNext()) {
            if(it.next().getNationName() == nation_name) {
                return err_type.NATION_CREATE_ALREADYHAVE;
            }
        }
        LocalTowerManagerBase new_nation = new LocalTowerManagerBase(nation_name);
        nation_list.add(new_nation);
        player_nationality.put(player_name, new_nation);
        return err_type.NATION_CREATE_SUCCESS;
    }

    public Pair<err_type ,LocalTowerManagerBase> getNation(String nation_name) {
        Iterator<LocalTowerManagerBase> it = this.nation_list.iterator();
        LocalTowerManagerBase nation = null;
        while (it.hasNext()) {
            if(it.next().getNationName() == nation_name){
                nation = it.next();
            }
        }
        if(nation == null){
            return new Pair<>(err_type.NATION_CANNOT_FIND, nation);
        } else {
            return new Pair<>(err_type.NATION_GET_SUCCESS, nation);
        }
    }

    public err_type deleteNation(String nation_name) {
        Pair<err_type ,LocalTowerManagerBase> nation = getNation(nation_name);
        this.nation_list.remove(nation.getB());
        return nation.getA();
    }

    public ArrayList<LocalTowerManagerBase> getNationList() {
        return this.nation_list;
    }
    enum err_type {
        NATION_CREATE_ALREADYHAVE,
        NATION_CREATE_SUCCESS,
        NATION_CANNOT_FIND,
        NATION_GET_SUCCESS
    }
}
