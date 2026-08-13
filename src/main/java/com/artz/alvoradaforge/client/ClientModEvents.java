package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AlvoradaForge.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FORGING_ANVIL.get(), ForgingAnvilRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RUNE_TABLE.get(), RuneTableRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, AlvoradaForge.id("forge_minigame"), ForgeMinigameOverlay.INSTANCE);
    }
}
