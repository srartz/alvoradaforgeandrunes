package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.forging.ForgeQuality;
import com.artz.alvoradaforge.forging.ForgedStats;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AlvoradaForge.MOD_ID);

    public static final Supplier<DataComponentType<ForgeQuality>> FORGING_QUALITY = COMPONENTS.register(
            "forging_quality",
            () -> DataComponentType.<ForgeQuality>builder()
                    .persistent(ForgeQuality.CODEC)
                    .networkSynchronized(ForgeQuality.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<ForgedStats>> FORGED_STATS = COMPONENTS.register(
            "forged_stats",
            () -> DataComponentType.<ForgedStats>builder()
                    .persistent(ForgedStats.CODEC)
                    .networkSynchronized(ForgedStats.STREAM_CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<Integer>> FORGING_BENEFITS = COMPONENTS.register(
            "forging_benefits",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static final Supplier<DataComponentType<String>> FORGED_BY = COMPONENTS.register(
            "forged_by",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    public static final Supplier<DataComponentType<String>> FORGE_RECIPE = COMPONENTS.register(
            "forge_recipe",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    public static final Supplier<DataComponentType<String>> RUNE_TYPE = COMPONENTS.register(
            "rune_type",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
