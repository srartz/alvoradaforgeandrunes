package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.block.entity.RuneTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RuneTableRenderer implements BlockEntityRenderer<RuneTableBlockEntity> {
    private final ItemRenderer itemRenderer;

    public RuneTableRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RuneTableBlockEntity table, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderItem(table, table.getDisplayedItem(RuneTableBlockEntity.STONE_SLOT), 0.28F, 0.90F, 0.30F,
                poseStack, buffers, packedLight, packedOverlay, 0);
        renderItem(table, table.getDisplayedItem(RuneTableBlockEntity.FEATHER_SLOT), 0.50F, 0.91F, 0.54F,
                poseStack, buffers, packedLight, packedOverlay, 1);
        renderItem(table, table.getDisplayedItem(RuneTableBlockEntity.INK_SLOT), 0.72F, 0.91F, 0.30F,
                poseStack, buffers, packedLight, packedOverlay, 2);
    }

    private void renderItem(RuneTableBlockEntity table, ItemStack stack, float x, float y, float z,
                            PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                            int packedOverlay, int seedOffset) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.34F, 0.34F, 0.34F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffers, table.getLevel(), (int)table.getBlockPos().asLong() + seedOffset);
        poseStack.popPose();
    }
}
