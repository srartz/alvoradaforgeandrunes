package com.artz.alvoradaforge.network;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.block.entity.RuneTableBlockEntity;
import com.artz.alvoradaforge.client.RuneClientPayloadHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AlvoradaForge.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class RuneNetwork {
    private RuneNetwork() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(OpenRuneScreenPayload.TYPE, OpenRuneScreenPayload.STREAM_CODEC,
                RuneNetwork::handleOpenScreen);
        registrar.playToServer(RuneTracePayload.TYPE, RuneTracePayload.STREAM_CODEC, RuneNetwork::handleTrace);
    }

    private static void handleOpenScreen(OpenRuneScreenPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RuneClientPayloadHandler.handleOpenScreen(payload, context);
        }
    }

    private static void handleTrace(RuneTracePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof RuneTableBlockEntity table
                && player.distanceToSqr(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5,
                payload.pos().getZ() + 0.5) <= 64.0) {
            table.completeRune(player, payload.runeType(), payload.points());
        }
    }
}
