package com.timwang.mc_tower_defenser.fundation.system.work;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

/**
 * 工作图中的一条有向边。
 * 当前只保存起点和终点，后续可以继续扩展边权重、资源类型、容量等信息。
 */
public final class Edge {
    private static final String FROM_TAG = "From";
    private static final String TO_TAG = "To";

    public static final StreamCodec<ByteBuf, Edge> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(Edge::deserializeNBT, Edge::serializeNBT);

    private final BlockPos from;
    private final BlockPos to;

    public Edge(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Edge endpoints cannot be null");
        }

        this.from = BlockPos.of(from.asLong());
        this.to = BlockPos.of(to.asLong());
    }

    public BlockPos getFrom() {
        return this.from;
    }

    public BlockPos getTo() {
        return this.to;
    }

    public boolean matches(BlockPos from, BlockPos to) {
        return this.from.equals(from) && this.to.equals(to);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(FROM_TAG, this.from.asLong());
        tag.putLong(TO_TAG, this.to.asLong());
        return tag;
    }

    public static Edge deserializeNBT(CompoundTag tag) {
        return new Edge(BlockPos.of(tag.getLong(FROM_TAG)), BlockPos.of(tag.getLong(TO_TAG)));
    }

    public Edge copy() {
        return new Edge(this.from, this.to);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Edge other)) {
            return false;
        }
        return this.from.equals(other.from) && this.to.equals(other.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.from, this.to);
    }
}
