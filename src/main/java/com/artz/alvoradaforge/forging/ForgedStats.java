package com.artz.alvoradaforge.forging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ForgedStats(
        float durabilityMultiplier,
        float miningSpeedMultiplier,
        double attackDamage,
        double attackSpeed,
        double armor,
        double armorToughness
) {
    public static final Codec<ForgedStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("durability_multiplier").forGetter(ForgedStats::durabilityMultiplier),
            Codec.FLOAT.fieldOf("mining_speed_multiplier").forGetter(ForgedStats::miningSpeedMultiplier),
            Codec.DOUBLE.fieldOf("attack_damage").forGetter(ForgedStats::attackDamage),
            Codec.DOUBLE.fieldOf("attack_speed").forGetter(ForgedStats::attackSpeed),
            Codec.DOUBLE.fieldOf("armor").forGetter(ForgedStats::armor),
            Codec.DOUBLE.fieldOf("armor_toughness").forGetter(ForgedStats::armorToughness)
    ).apply(instance, ForgedStats::new));

    public static final StreamCodec<ByteBuf, ForgedStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ForgedStats::durabilityMultiplier,
            ByteBufCodecs.FLOAT, ForgedStats::miningSpeedMultiplier,
            ByteBufCodecs.DOUBLE, ForgedStats::attackDamage,
            ByteBufCodecs.DOUBLE, ForgedStats::attackSpeed,
            ByteBufCodecs.DOUBLE, ForgedStats::armor,
            ByteBufCodecs.DOUBLE, ForgedStats::armorToughness,
            ForgedStats::new
    );

    public static ForgedStats defaults(ForgeQuality quality) {
        return switch (quality) {
            case POOR -> new ForgedStats(0.70F, 0.70F, -1.0, -0.5, -1.0, -0.5);
            case WELL -> new ForgedStats(1.00F, 1.00F, 0.0, 0.0, 0.0, 0.0);
            case EXPERT -> new ForgedStats(1.30F, 1.15F, 1.5, 0.25, 1.0, 0.5);
            case PERFECT -> new ForgedStats(1.50F, 1.30F, 2.0, 0.5, 1.5, 1.0);
            case MASTER -> new ForgedStats(1.60F, 1.50F, 3.0, 1.0, 2.0, 1.5);
        };
    }
}
