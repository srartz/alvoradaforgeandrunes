package com.artz.alvoradaforge.network;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenRuneScreenPayload(BlockPos pos, RuneType runeType, int minTier, int maxTier) implements CustomPacketPayload {
    public static final Type<OpenRuneScreenPayload> TYPE = new Type<>(AlvoradaForge.id("open_rune_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRuneScreenPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenRuneScreenPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new OpenRuneScreenPayload(buffer.readBlockPos(), RuneType.byOrdinal(buffer.readUnsignedByte()),
                            buffer.readUnsignedByte(), buffer.readUnsignedByte());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, OpenRuneScreenPayload payload) {
                    buffer.writeBlockPos(payload.pos());
                    buffer.writeByte(payload.runeType().ordinal());
                    buffer.writeByte(payload.minTier());
                    buffer.writeByte(payload.maxTier());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
