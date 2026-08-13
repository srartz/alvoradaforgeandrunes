package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.block.entity.ForgingAnvilBlockEntity;
import com.artz.alvoradaforge.block.entity.RuneTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AlvoradaForge.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgingAnvilBlockEntity>> FORGING_ANVIL =
            BLOCK_ENTITIES.register(
                    "forging_anvil",
                    () -> BlockEntityType.Builder.of(ForgingAnvilBlockEntity::new, ModBlocks.FORGING_ANVIL.get()).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RuneTableBlockEntity>> RUNE_TABLE =
            BLOCK_ENTITIES.register(
                    "rune_table",
                    () -> BlockEntityType.Builder.of(RuneTableBlockEntity::new, ModBlocks.RUNE_TABLE.get()).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
