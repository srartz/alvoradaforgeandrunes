package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.block.ForgingAnvilBlock;
import com.artz.alvoradaforge.block.RuneTableBlock;
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

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
