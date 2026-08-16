package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.block.ForgingAnvilBlock;
import com.artz.alvoradaforge.block.LapidarySawBlock;
import com.artz.alvoradaforge.block.RuneTableBlock;
import com.artz.alvoradaforge.block.MysteryRuneStoneBlock;
import com.artz.alvoradaforge.block.RuneBreakerTableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AlvoradaForge.MOD_ID);

    public static final DeferredBlock<ForgingAnvilBlock> FORGING_ANVIL = BLOCKS.register(
            "forging_anvil",
            () -> new ForgingAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL))
    );

    public static final DeferredBlock<RuneTableBlock> RUNE_TABLE = BLOCKS.register(
            "rune_table",
            () -> new RuneTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).noOcclusion())
    );

    public static final DeferredBlock<RuneTableBlock> ANCESTRAL_RUNE_TABLE = BLOCKS.register(
            "ancestral_rune_table",
            () -> new RuneTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE)
                    .strength(7.0F, 20.0F).lightLevel(state -> 8).noOcclusion())
    );

    public static final DeferredBlock<MysteryRuneStoneBlock> MYSTERY_RUNE_STONE = BLOCKS.register(
            "mystery_rune_stone",
            () -> new MysteryRuneStoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(4.0F, 9.0F).lightLevel(state -> 4))
    );

    public static final DeferredBlock<RuneBreakerTableBlock> RUNE_BREAKER_TABLE = BLOCKS.register(
            "rune_breaker_table",
            () -> new RuneBreakerTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE)
                    .strength(5.0F, 12.0F).noOcclusion())
    );

    public static final DeferredBlock<LapidarySawBlock> LAPIDARY_SAW = BLOCKS.register(
            "lapidary_saw",
            () -> new LapidarySawBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER)
                    .strength(4.5F, 10.0F).noOcclusion())
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
