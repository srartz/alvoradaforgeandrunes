package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.block.entity.LapidarySawBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class LapidarySawRenderer implements BlockEntityRenderer<LapidarySawBlockEntity> {
    private final ItemRenderer itemRenderer;

    public LapidarySawRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(LapidarySawBlockEntity saw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderFlat(saw, saw.getDisplayedItem(LapidarySawBlockEntity.STONE_SLOT),
                0.50F, 1.04F, 0.53F, 0.46F, poseStack, buffers, packedLight, packedOverlay, 0);
        renderFlat(saw, saw.getDisplayedItem(LapidarySawBlockEntity.ABRASIVE_SLOT),
                0.22F, 1.04F, 0.72F, 0.30F, poseStack, buffers, packedLight, packedOverlay, 1);

        ItemStack blade = saw.getDisplayedItem(LapidarySawBlockEntity.BLADE_SLOT);
        if (!blade.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.72F, 1.10F, 0.50F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            if (saw.isProcessing()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(
                        (saw.getLevel().getGameTime() + partialTick) * 28.0F));
            }
            poseStack.scale(0.42F, 0.42F, 0.42F);
            itemRenderer.renderStatic(blade, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffers, saw.getLevel(), (int)saw.getBlockPos().asLong() + 2);
            poseStack.popPose();
        }

        ItemStack result = saw.getDisplayedItem(LapidarySawBlockEntity.RESULT_SLOT);
        if (!result.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.50F, 1.30F, 0.50F);
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    (saw.getLevel().getGameTime() + partialTick) * 3.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffers, saw.getLevel(), (int)saw.getBlockPos().asLong() + 3);
            poseStack.popPose();
        }
    }

    private void renderFlat(LapidarySawBlockEntity saw, ItemStack stack, float x, float y, float z, float scale,
                            PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay,
                            int seed) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffers, saw.getLevel(), (int)saw.getBlockPos().asLong() + seed);
        poseStack.popPose();
    }
}
