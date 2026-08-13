package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.network.OpenRuneScreenPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RuneClientPayloadHandler {
    private RuneClientPayloadHandler() {
    }

    public static void handleOpenScreen(OpenRuneScreenPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new RuneDrawingScreen(payload.pos(), payload.runeType()));
    }
}
