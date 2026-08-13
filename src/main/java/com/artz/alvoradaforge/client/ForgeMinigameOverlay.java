package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.block.entity.ForgingAnvilBlockEntity;
import com.artz.alvoradaforge.item.ForgeHammerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public final class ForgeMinigameOverlay implements LayeredDraw.Layer {
    public static final ForgeMinigameOverlay INSTANCE = new ForgeMinigameOverlay();

    private static final int PANEL_WIDTH = 224;
    private static final int PANEL_HEIGHT = 59;
    private static final int BAR_WIDTH = 196;
    private static final int BAR_HEIGHT = 14;
    private static final int PROGRESS_SEGMENTS = 20;

    private ForgeMinigameOverlay() {
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null
                || !(minecraft.hitResult instanceof BlockHitResult blockHit)) {
            return;
        }
        if (!(minecraft.level.getBlockEntity(blockHit.getBlockPos()) instanceof ForgingAnvilBlockEntity anvil)
                || !anvil.isActive()
                || anvil.getOwner() == null
                || !anvil.getOwner().equals(minecraft.player.getUUID())) {
            return;
        }

        ItemStack held = minecraft.player.getMainHandItem();
        ForgeHammerItem hammer;
        if (held.getItem() instanceof ForgeHammerItem mainHandHammer) {
            hammer = mainHandHammer;
        } else {
            held = minecraft.player.getOffhandItem();
            if (!(held.getItem() instanceof ForgeHammerItem offhandHammer)) {
                int center = graphics.guiWidth() / 2;
                graphics.drawCenteredString(minecraft.font, Component.translatable("hud.alvoradaforge.equip_hammer"),
                        center, graphics.guiHeight() - 74, 0xFFFF5555);
                return;
            }
            hammer = offhandHammer;
        }

        int centerX = graphics.guiWidth() / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = graphics.guiHeight() - 100;
        int x = centerX - BAR_WIDTH / 2;
        int y = panelY + 25;

        // Moldura em camadas, desenhada em blocos para manter o estilo pixelado do jogo.
        graphics.fill(panelX + 2, panelY, panelX + PANEL_WIDTH - 2, panelY + PANEL_HEIGHT, 0xE60B0908);
        graphics.fill(panelX, panelY + 2, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT - 2, 0xE60B0908);
        graphics.fill(panelX + 2, panelY + 2, panelX + PANEL_WIDTH - 2, panelY + PANEL_HEIGHT - 2, 0xFFE09032);
        graphics.fill(panelX + 4, panelY + 4, panelX + PANEL_WIDTH - 4, panelY + PANEL_HEIGHT - 4, 0xFF44261B);
        graphics.fill(panelX + 6, panelY + 6, panelX + PANEL_WIDTH - 6, panelY + PANEL_HEIGHT - 6, 0xE6191412);
        graphics.fill(panelX + 6, panelY + 6, panelX + PANEL_WIDTH - 6, panelY + 8, 0xFF75442B);

        graphics.renderItem(held, panelX + 8, panelY + 7);
        graphics.drawString(minecraft.font, Component.translatable("hud.alvoradaforge.strike_timing"),
                panelX + 29, panelY + 9, 0xFFFFD58A, true);

        // Zonas de acerto com sombra, brilho, marcações e centro destacado.
        graphics.fill(x - 3, y - 3, x + BAR_WIDTH + 3, y + BAR_HEIGHT + 3, 0xFF070605);
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, 0xFFB56B2B);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF7D2026);
        int targetX = x + (int)Math.round(anvil.getTargetCenter() * (BAR_WIDTH - 1));
        fillTargetZone(graphics, x, y, targetX, 68, 0xFFBF6926);
        fillTargetZone(graphics, x, y, targetX, 38, 0xFFF0B72E);
        fillTargetZone(graphics, x, y, targetX, 16, 0xFF24866B);
        drawBrushTexture(graphics, x, y);
        graphics.fill(x, y, x + BAR_WIDTH, y + 2, 0x55FFFFFF);
        graphics.fill(x, y + BAR_HEIGHT - 3, x + BAR_WIDTH, y + BAR_HEIGHT, 0x66000000);

        for (int tick = 0; tick <= 14; tick++) {
            int tickX = x + tick * BAR_WIDTH / 14;
            graphics.fill(tickX, y + BAR_HEIGHT - 4, tickX + 1, y + BAR_HEIGHT, 0x88000000);
        }
        graphics.fill(targetX - 1, y + 2, targetX + 1, y + BAR_HEIGHT - 3, 0xCCFFF3A6);

        double markerPosition = anvil.markerPosition(
                minecraft.level.getGameTime(),
                deltaTracker.getGameTimeDeltaPartialTick(true),
                hammer
        );
        int markerX = x + (int)Math.round(markerPosition * (BAR_WIDTH - 1));
        drawPixelMarker(graphics, markerX, y);

        int litSegments = anvil.getRequiredHits() == 0
                ? 0
                : Mth.ceil((double)anvil.getProgress() * PROGRESS_SEGMENTS / anvil.getRequiredHits());
        int segmentY = y + BAR_HEIGHT + 7;
        for (int segment = 0; segment < PROGRESS_SEGMENTS; segment++) {
            int segmentX = x + segment * BAR_WIDTH / PROGRESS_SEGMENTS;
            int nextSegmentX = x + (segment + 1) * BAR_WIDTH / PROGRESS_SEGMENTS;
            int color = segment < litSegments ? progressColor(segment, litSegments) : 0xFF302924;
            graphics.fill(segmentX, segmentY, nextSegmentX - 1, segmentY + 4, 0xFF080706);
            graphics.fill(segmentX + 1, segmentY + 1, nextSegmentX - 2, segmentY + 3, color);
        }

        Component progressText = Component.translatable(
                "hud.alvoradaforge.forging_progress", anvil.getProgress(), anvil.getRequiredHits());
        graphics.drawString(minecraft.font, progressText,
                panelX + PANEL_WIDTH - 7 - minecraft.font.width(progressText),
                panelY + 9, 0xFFE7E0D6, true);
    }

    private static void drawPixelMarker(GuiGraphics graphics, int markerX, int y) {
        graphics.fill(markerX - 3, y - 7, markerX + 4, y - 4, 0xFF080706);
        graphics.fill(markerX - 2, y - 9, markerX + 3, y - 2, 0xFF080706);
        graphics.fill(markerX - 1, y - 10, markerX + 2, y + BAR_HEIGHT + 7, 0xFF080706);
        graphics.fill(markerX, y - 8, markerX + 1, y + BAR_HEIGHT + 5, 0xFFFFFFFF);
        graphics.fill(markerX - 2, y + BAR_HEIGHT + 4, markerX + 3, y + BAR_HEIGHT + 6, 0xFF080706);
        graphics.fill(markerX - 1, y + BAR_HEIGHT + 3, markerX + 2, y + BAR_HEIGHT + 5, 0xFFFFE39B);
    }

    private static void fillTargetZone(
            GuiGraphics graphics,
            int barX,
            int y,
            int targetX,
            int halfWidth,
            int color
    ) {
        int left = Math.max(barX, targetX - halfWidth);
        int right = Math.min(barX + BAR_WIDTH, targetX + halfWidth);
        graphics.fill(left, y, right, y + BAR_HEIGHT, color);
    }

    private static int progressColor(int segment, int litSegments) {
        if (segment == litSegments - 1) {
            return 0xFFFFFF9A;
        }
        if (segment >= 14) {
            return 0xFF52E57E;
        }
        if (segment >= 7) {
            return 0xFF48C6A0;
        }
        return 0xFFEAA23D;
    }

    private static void drawBrushTexture(GuiGraphics graphics, int x, int y) {
        // Riscos curtos e irregulares imitam uma pincelada sobre as cores originais.
        // A sequência é fixa para a textura não piscar enquanto o HUD é redesenhado.
        for (int stroke = 0; stroke < 38; stroke++) {
            int strokeX = x + Math.floorMod(stroke * 47 + 13, BAR_WIDTH - 5);
            int strokeY = y + 2 + Math.floorMod(stroke * 7 + stroke / 3, BAR_HEIGHT - 5);
            int length = 2 + Math.floorMod(stroke * 11, 5);
            int color = stroke % 3 == 0 ? 0x24FFFFFF : 0x24000000;
            graphics.fill(strokeX, strokeY, Math.min(x + BAR_WIDTH, strokeX + length), strokeY + 1, color);
        }

        for (int fleck = 0; fleck < 22; fleck++) {
            int fleckX = x + Math.floorMod(fleck * 71 + 5, BAR_WIDTH - 1);
            int fleckY = y + 1 + Math.floorMod(fleck * 5, BAR_HEIGHT - 3);
            graphics.fill(fleckX, fleckY, fleckX + 1, fleckY + 1,
                    fleck % 2 == 0 ? 0x32FFF4D2 : 0x30080504);
        }
    }
}
