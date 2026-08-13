package com.artz.alvoradaforge.forging;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ForgeQuality implements StringRepresentable {
    POOR("poor", ChatFormatting.GRAY),
    WELL("well", ChatFormatting.WHITE),
    EXPERT("expert", ChatFormatting.AQUA),
    PERFECT("perfect", ChatFormatting.GOLD),
    MASTER("master", ChatFormatting.LIGHT_PURPLE);

    public static final Codec<ForgeQuality> CODEC = Codec.STRING.xmap(ForgeQuality::fromName, ForgeQuality::getSerializedName);
    public static final StreamCodec<ByteBuf, ForgeQuality> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(ForgeQuality::fromName, ForgeQuality::getSerializedName);

    private final String serializedName;
    private final ChatFormatting color;

    ForgeQuality(String serializedName, ChatFormatting color) {
        this.serializedName = serializedName;
        this.color = color;
    }

    public static ForgeQuality fromName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (ForgeQuality quality : values()) {
            if (quality.serializedName.equals(normalized)) {
                return quality;
            }
        }
        throw new IllegalArgumentException("Qualidade de forja desconhecida: " + name);
    }

    public ChatFormatting color() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
