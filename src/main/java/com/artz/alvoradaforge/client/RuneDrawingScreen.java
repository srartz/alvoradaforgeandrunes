package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.network.RuneTracePayload;
import com.artz.alvoradaforge.rune.RunePatternValidator;
import com.artz.alvoradaforge.rune.RuneType;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RuneDrawingScreen extends Screen {
    private static final int CANVAS_SIZE = 256;
    private final BlockPos tablePos;
    private RuneType runeType;
    private final List<RunePatternValidator.Point> trace = new ArrayList<>();
    private boolean drawing;

    public RuneDrawingScreen(BlockPos tablePos, RuneType runeType) {
        super(Component.translatable("screen.alvoradaforge.rune_drawing"));
        this.tablePos = tablePos;
        this.runeType = runeType;
    }

    @Override
    protected void init() {
        int buttonY = Math.max(4, canvasTop() - 25);
        addRenderableWidget(Button.builder(Component.literal("<"), button -> selectRelativeRune(-1))
                .bounds(width / 2 - 112, buttonY, 22, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> selectRelativeRune(1))
                .bounds(width / 2 + 90, buttonY, 22, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = canvasLeft();
        int top = canvasTop();

        graphics.fill(left - 8, top - 8, left + CANVAS_SIZE + 8, top + CANVAS_SIZE + 8, 0xEE17131D);
        graphics.fill(left, top, left + CANVAS_SIZE, top + CANVAS_SIZE, 0xFF292432);
        graphics.renderOutline(left - 1, top - 1, CANVAS_SIZE + 2, CANVAS_SIZE + 2, runeType.color());

        drawPolyline(graphics, RunePatternValidator.pattern(runeType), left, top,
                (runeType.color() & 0x00FFFFFF) | 0x66000000, 2);
        drawPolyline(graphics, trace, left, top, runeType.color(), 3);

        graphics.drawCenteredString(font, title, width / 2, Math.max(8, top - 32), 0xFFF2E7D5);
        graphics.drawCenteredString(font,
                Component.translatable("screen.alvoradaforge.rune_instruction"),
                width / 2, top + CANVAS_SIZE + 16, 0xFFC8B8D8);
        graphics.drawCenteredString(font,
                Component.translatable(
                        "screen.alvoradaforge.rune_selected",
                        Component.translatable("rune.alvoradaforge." + runeType.serializedName()),
                        runeType.tier()
                ),
                width / 2, Math.max(20, top - 18), runeType.color());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && insideCanvas(mouseX, mouseY)) {
            trace.clear();
            drawing = true;
            addPoint(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (drawing && button == 0) {
            if (insideCanvas(mouseX, mouseY)) {
                addPoint(mouseX, mouseY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (drawing && button == 0) {
            drawing = false;
            if (trace.size() >= 8) {
                PacketDistributor.sendToServer(new RuneTracePayload(tablePos, runeType, packTrace()));
                onClose();
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void addPoint(double mouseX, double mouseY) {
        int x = clamp((int)Math.round(mouseX - canvasLeft()));
        int y = clamp((int)Math.round(mouseY - canvasTop()));
        if (!trace.isEmpty()) {
            RunePatternValidator.Point previous = trace.getLast();
            if (Math.hypot(x - previous.x(), y - previous.y()) < 3.0) {
                return;
            }
        }
        if (trace.size() < RunePatternValidator.MAX_TRACE_POINTS) {
            trace.add(new RunePatternValidator.Point(x, y));
        }
    }

    private void selectRelativeRune(int offset) {
        if (!drawing) {
            runeType = runeType.relative(offset);
            trace.clear();
        }
    }

    private byte[] packTrace() {
        ByteArrayOutputStream output = new ByteArrayOutputStream(trace.size() * 2);
        for (RunePatternValidator.Point point : trace) {
            output.write(point.x());
            output.write(point.y());
        }
        return output.toByteArray();
    }

    private static void drawPolyline(GuiGraphics graphics, List<RunePatternValidator.Point> points,
                                     int left, int top, int color, int radius) {
        for (int index = 1; index < points.size(); index++) {
            RunePatternValidator.Point from = points.get(index - 1);
            RunePatternValidator.Point to = points.get(index);
            int steps = Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y()));
            for (int step = 0; step <= steps; step++) {
                double fraction = steps == 0 ? 0.0 : step / (double)steps;
                int x = left + (int)Math.round(from.x() + (to.x() - from.x()) * fraction);
                int y = top + (int)Math.round(from.y() + (to.y() - from.y()) * fraction);
                graphics.fill(x - radius, y - radius, x + radius + 1, y + radius + 1, color);
            }
        }
    }

    private boolean insideCanvas(double mouseX, double mouseY) {
        return mouseX >= canvasLeft() && mouseX <= canvasLeft() + CANVAS_SIZE
                && mouseY >= canvasTop() && mouseY <= canvasTop() + CANVAS_SIZE;
    }

    private int canvasLeft() {
        return (width - CANVAS_SIZE) / 2;
    }

    private int canvasTop() {
        return (height - CANVAS_SIZE) / 2;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
