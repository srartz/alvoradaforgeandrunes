package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.block.entity.RuneBreakerTableBlockEntity;
import com.artz.alvoradaforge.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RuneBreakerTableRenderer implements BlockEntityRenderer<RuneBreakerTableBlockEntity> {
    private final ItemRenderer itemRenderer;

    public RuneBreakerTableRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RuneBreakerTableBlockEntity table, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderFlat(table, table.getDisplayedItem(RuneBreakerTableBlockEntity.STONE_SLOT),
                0.5F, 1.04F, 0.5F, 0.48F, poseStack, buffers, packedLight, packedOverlay, 0);

        ItemStack result = table.getDisplayedItem(RuneBreakerTableBlockEntity.RESULT_SLOT);
        if (!result.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.30F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees((table.getLevel().getGameTime() + partialTick) * 3.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffers, table.getLevel(), (int)table.getBlockPos().asLong() + 1);
            poseStack.popPose();
        }

        // Martelo fixo da bancada: indica visualmente como a estação funciona.
        poseStack.pushPose();
        poseStack.translate(0.77F, 1.08F, 0.53F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-32.0F));
        poseStack.scale(0.46F, 0.46F, 0.46F);
        itemRenderer.renderStatic(new ItemStack(ModItems.IRON_HAMMER.get()), ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, buffers, table.getLevel(),
                (int)table.getBlockPos().asLong() + 2);
        poseStack.popPose();
    }

    private void renderFlat(RuneBreakerTableBlockEntity table, ItemStack stack, float x, float y, float z, float scale,
                            PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffers, table.getLevel(), (int)table.getBlockPos().asLong() + seed);
        poseStack.popPose();
    }
}
