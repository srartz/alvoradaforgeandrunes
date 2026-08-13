package com.artz.alvoradaforge;

import com.artz.alvoradaforge.forging.ForgeEvents;
import com.artz.alvoradaforge.config.ForgeItemConfig;
import com.artz.alvoradaforge.registry.ModBlocks;
import com.artz.alvoradaforge.registry.ModBlockEntities;
import com.artz.alvoradaforge.registry.ModDataComponents;
import com.artz.alvoradaforge.registry.ModCreativeModeTabs;
import com.artz.alvoradaforge.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(AlvoradaForge.MOD_ID)
public final class AlvoradaForge {
    public static final String MOD_ID = "alvoradaforge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AlvoradaForge(IEventBus modEventBus) {
        ForgeItemConfig.ensureCreated();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(ForgeEvents::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(ForgeEvents::onAnvilUpdate);
        NeoForge.EVENT_BUS.addListener(ForgeEvents::onAnvilRepair);
        NeoForge.EVENT_BUS.addListener(ForgeEvents::onItemAttributes);
        NeoForge.EVENT_BUS.addListener(ForgeEvents::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(ForgeEvents::onPlayerTick);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
