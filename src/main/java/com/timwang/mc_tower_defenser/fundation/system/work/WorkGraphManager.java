package com.timwang.mc_tower_defenser.fundation.system.work;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 单个国家持有的工作图。
 * 负责节点注册、节点注销，以及有向边连接关系的维护。
 */
public class WorkGraphManager {
    private static final String NODES_TAG = "Nodes";
    private static final String EDGES_TAG = "Edges";
    private static final String POS_TAG = "Pos";

    public static final StreamCodec<ByteBuf, WorkGraphManager> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(WorkGraphManager::deserializeNBT, WorkGraphManager::serializeNBT);

    private final Set<BlockPos> nodes;
    private final Set<Edge> edges;

    public WorkGraphManager() {
        this.nodes = new LinkedHashSet<>();
        this.edges = new LinkedHashSet<>();
    }

    /** 注册一个节点；若已存在则返回 false。 */
    public boolean registerNode(BlockPos pos) {
        BlockPos normalized = normalize(pos);
        if (normalized == null) {
            return false;
        }

        return this.nodes.add(normalized);
    }

    /**
     * 注销一个节点，同时移除所有以该节点为起点或终点的边。
     * 若节点不存在则返回 false。
     */
    public boolean unregisterNode(BlockPos pos) {
        BlockPos normalized = normalize(pos);
        if (normalized == null || !this.nodes.remove(normalized)) {
            return false;
        }

        this.edges.removeIf(edge -> edge.getFrom().equals(normalized) || edge.getTo().equals(normalized));
        return true;
    }

    /**
     * 建立一条有向边：from -> to。
     * 只有两个端点都已注册时才允许连接。
     */
    public boolean connect(BlockPos from, BlockPos to) {
        BlockPos normalizedFrom = normalize(from);
        BlockPos normalizedTo = normalize(to);
        if (normalizedFrom == null || normalizedTo == null || normalizedFrom.equals(normalizedTo)) {
            return false;
        }
        if (!this.nodes.contains(normalizedFrom) || !this.nodes.contains(normalizedTo)) {
            return false;
        }

        return this.edges.add(new Edge(normalizedFrom, normalizedTo));
    }

    /** 移除一条已有的有向边。 */
    public boolean disconnect(BlockPos from, BlockPos to) {
        BlockPos normalizedFrom = normalize(from);
        BlockPos normalizedTo = normalize(to);
        if (normalizedFrom == null || normalizedTo == null) {
            return false;
        }

        return this.edges.remove(new Edge(normalizedFrom, normalizedTo));
    }

    public boolean hasNode(BlockPos pos) {
        BlockPos normalized = normalize(pos);
        return normalized != null && this.nodes.contains(normalized);
    }

    public boolean hasEdge(BlockPos from, BlockPos to) {
        BlockPos normalizedFrom = normalize(from);
        BlockPos normalizedTo = normalize(to);
        if (normalizedFrom == null || normalizedTo == null) {
            return false;
        }

        return this.edges.contains(new Edge(normalizedFrom, normalizedTo));
    }

    public Set<BlockPos> getNodes() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.nodes));
    }

    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.edges));
    }

    public List<Edge> getOutgoingEdges(BlockPos from) {
        BlockPos normalized = normalize(from);
        if (normalized == null) {
            return List.of();
        }

        List<Edge> outgoingEdges = new ArrayList<>();
        for (Edge edge : this.edges) {
            if (edge.getFrom().equals(normalized)) {
                outgoingEdges.add(edge.copy());
            }
        }
        return List.copyOf(outgoingEdges);
    }

    public WorkGraphManager copy() {
        return deserializeNBT(this.serializeNBT());
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag nodeTags = new ListTag();
        for (BlockPos node : this.nodes) {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putLong(POS_TAG, node.asLong());
            nodeTags.add(nodeTag);
        }
        tag.put(NODES_TAG, nodeTags);

        ListTag edgeTags = new ListTag();
        for (Edge edge : this.edges) {
            edgeTags.add(edge.serializeNBT());
        }
        tag.put(EDGES_TAG, edgeTags);

        return tag;
    }

    public static WorkGraphManager deserializeNBT(CompoundTag tag) {
        WorkGraphManager graph = new WorkGraphManager();

        ListTag nodeTags = tag.getList(NODES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeTags.size(); i++) {
            CompoundTag nodeTag = nodeTags.getCompound(i);
            graph.registerNode(BlockPos.of(nodeTag.getLong(POS_TAG)));
        }

        ListTag edgeTags = tag.getList(EDGES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < edgeTags.size(); i++) {
            Edge edge = Edge.deserializeNBT(edgeTags.getCompound(i));
            graph.registerNode(edge.getFrom());
            graph.registerNode(edge.getTo());
            graph.edges.add(edge);
        }

        return graph;
    }

    private static BlockPos normalize(BlockPos pos) {
        return pos == null ? null : BlockPos.of(pos.asLong());
    }
}
