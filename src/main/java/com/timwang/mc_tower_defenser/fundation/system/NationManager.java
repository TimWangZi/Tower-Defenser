package com.timwang.mc_tower_defenser.fundation.system;

import com.timwang.mc_tower_defenser.fundation.system.work.WorkGraphManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 单个国家的数据对象。
 * 保存国家名称、成员列表、该国家持有的 UrbanCore 坐标，以及工作图数据。
 */
public class NationManager {
    private static final String NAME_TAG = "Name";
    private static final String MEMBERS_TAG = "Members";
    private static final String TOWERS_TAG = "Towers";
    private static final String WORK_GRAPH_TAG = "WorkGraph";
    private static final String POS_TAG = "Pos";

    public static final StreamCodec<ByteBuf, NationManager> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(NationManager::deserializeNBT, NationManager::serializeNBT);
    public static final StreamCodec<ByteBuf, Optional<NationManager>> OPTIONAL_STREAM_CODEC =
            ByteBufCodecs.OPTIONAL_COMPOUND_TAG.map(
                    optionalTag -> optionalTag.map(NationManager::deserializeNBT),
                    optionalNation -> optionalNation.map(NationManager::serializeNBT)
            );

    private final String name;                   // 阵营名称
    private final List<String> memberNames;      // 属于这个阵营的玩家名
    private final List<BlockPos> towerPositions; // 已注册的UrbanCore塔坐标
    private final WorkGraphManager workGraphManager;// 工作方块管理器

    public NationManager(String nation_name) {
        this(nation_name, new WorkGraphManager());
    }

    private NationManager(String nationName, WorkGraphManager workGraphManager) {
        this.name = nationName;
        this.memberNames = new ArrayList<>();
        this.towerPositions = new ArrayList<>();
        this.workGraphManager = workGraphManager == null ? new WorkGraphManager() : workGraphManager;
    }

    /** 为国家补录成员，不重复添加。 */
    public boolean addMember(String memberName) {
        if (memberName == null || memberName.isBlank() || memberNames.contains(memberName)) {
            return false;
        }

        memberNames.add(memberName);
        return true;
    }

    /** 判断玩家名是否已经归属于当前国家。 */
    public boolean hasMember(String memberName) {
        return memberName != null && memberNames.contains(memberName);
    }

    // 注册一个塔到当前阵营，同时记录玩家（若提供）
    public boolean registerTower(String member_name, BlockPos pos) {
        boolean changed = addMember(member_name);
        if (pos != null && !towerPositions.contains(pos)) {
            towerPositions.add(pos);
            changed = true;
        }
        return changed;
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
        return List.copyOf(memberNames);
    }

    public List<BlockPos> getTowerPositions() {
        return List.copyOf(towerPositions);
    }

    public WorkGraphManager getWorkGraphManager() {
        return this.workGraphManager;
    }

    public boolean registerWorkNode(BlockPos pos) {
        return this.workGraphManager.registerNode(pos);
    }

    public boolean unregisterWorkNode(BlockPos pos) {
        return this.workGraphManager.unregisterNode(pos);
    }

    public boolean connectWorkNodes(BlockPos from, BlockPos to) {
        return this.workGraphManager.connect(from, to);
    }

    public boolean disconnectWorkNodes(BlockPos from, BlockPos to) {
        return this.workGraphManager.disconnect(from, to);
    }

    /** 返回一份深拷贝，便于客户端缓存使用。 */
    public NationManager copy() {
        return deserializeNBT(this.serializeNBT());
    }

    /** 序列化当前阵营信息，可同时用于 SavedData 与网络同步。 */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(NAME_TAG, this.name);

        ListTag members = new ListTag();
        for (String member : memberNames) {
            members.add(StringTag.valueOf(member));
        }
        tag.put(MEMBERS_TAG, members);

        ListTag towers = new ListTag();
        for (BlockPos pos : towerPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong(POS_TAG, pos.asLong());
            towers.add(posTag);
        }
        tag.put(TOWERS_TAG, towers);
        tag.put(WORK_GRAPH_TAG, this.workGraphManager.serializeNBT());
        return tag;
    }

    /** 为兼容旧调用方保留的别名。 */
    public CompoundTag serialize() {
        return serializeNBT();
    }

    /** 反序列化当前阵营信息，可同时用于 SavedData 与网络同步。 */
    public static NationManager deserializeNBT(CompoundTag tag) {
        NationManager nation = new NationManager(
                tag.getString(NAME_TAG),
                WorkGraphManager.deserializeNBT(tag.getCompound(WORK_GRAPH_TAG))
        );
        ListTag members = tag.getList(MEMBERS_TAG, Tag.TAG_STRING);
        for (int i = 0; i < members.size(); i++) {
            nation.memberNames.add(members.getString(i));
        }
        ListTag towers = tag.getList(TOWERS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < towers.size(); i++) {
            CompoundTag posTag = towers.getCompound(i);
            nation.towerPositions.add(BlockPos.of(posTag.getLong(POS_TAG)));
        }
        return nation;
    }

    /** 为兼容旧调用方保留的别名。 */
    public static NationManager deserialize(CompoundTag tag) {
        return deserializeNBT(tag);
    }
}
