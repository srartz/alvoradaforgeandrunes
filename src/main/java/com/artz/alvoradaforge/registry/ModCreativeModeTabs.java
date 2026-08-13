package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.rune.RuneType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlvoradaForge.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FORGING = TABS.register(
            "forging",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.alvoradaforge.forging"))
                    .icon(() -> new ItemStack(ModItems.IRON_HAMMER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FORGING_ANVIL.get());
                        output.accept(ModItems.RUNE_TABLE.get());
                        output.accept(ModItems.RUNE_STONE.get());
                        output.accept(ModItems.EMBER_INK.get());
                        output.accept(ModItems.TIDE_INK.get());
                        output.accept(ModItems.VERDANT_INK.get());
                        output.accept(ModItems.VOID_INK.get());
                        for (RuneType type : RuneType.values()) {
                            output.accept(ModItems.runeItem(type).get());
                        }
                        output.accept(ModItems.COPPER_HAMMER.get());
                        output.accept(ModItems.IRON_HAMMER.get());
                        output.accept(ModItems.GOLD_HAMMER.get());
                        output.accept(ModItems.DIAMOND_HAMMER.get());
                        output.accept(ModItems.NETHERITE_HAMMER.get());
                    })
                    .build()
    );

    private ModCreativeModeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
