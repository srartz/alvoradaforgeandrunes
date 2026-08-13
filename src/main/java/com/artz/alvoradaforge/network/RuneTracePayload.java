package com.artz.alvoradaforge.network;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.rune.RunePatternValidator;
import com.artz.alvoradaforge.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RuneTracePayload(BlockPos pos, RuneType runeType, byte[] points) implements CustomPacketPayload {
    public static final Type<RuneTracePayload> TYPE = new Type<>(AlvoradaForge.id("rune_trace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RuneTracePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RuneTracePayload decode(RegistryFriendlyByteBuf buffer) {
                    BlockPos pos = buffer.readBlockPos();
                    RuneType type = RuneType.byOrdinal(buffer.readUnsignedByte());
                    return new RuneTracePayload(pos, type,
                            buffer.readByteArray(RunePatternValidator.MAX_PACKED_TRACE_BYTES));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, RuneTracePayload payload) {
                    buffer.writeBlockPos(payload.pos());
                    buffer.writeByte(payload.runeType().ordinal());
                    buffer.writeByteArray(payload.points());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
