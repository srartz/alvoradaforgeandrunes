package com.artz.alvoradaforge.client;

import com.artz.alvoradaforge.block.entity.ForgingAnvilBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;

public final class ForgingAnvilRenderer implements BlockEntityRenderer<ForgingAnvilBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ForgingAnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            ForgingAnvilBlockEntity anvil,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing = anvil.getBlockState().getValue(AnvilBlock.FACING);
        renderItem(anvil, anvil.getDisplayedItem(ForgingAnvilBlockEntity.BASE_SLOT), -0.19F, 1.035F,
                facing, poseStack, buffers, packedLight, packedOverlay, 0);
        renderItem(anvil, anvil.getDisplayedItem(ForgingAnvilBlockEntity.ADDITION_SLOT), 0.19F, 1.04F,
                facing, poseStack, buffers, packedLight, packedOverlay, 1);

        ItemStack result = anvil.getDisplayedItem(ForgingAnvilBlockEntity.RESULT_SLOT);
        if (!result.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.23F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees((anvil.getLevel().getGameTime() + partialTick) * 2.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            itemRenderer.renderStatic(result, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffers, anvil.getLevel(), (int)anvil.getBlockPos().asLong() + 2);
            poseStack.popPose();
        }
    }

    private void renderItem(
            ForgingAnvilBlockEntity anvil,
            ItemStack stack,
            float offset,
            float height,
            Direction facing,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            int seedOffset
    ) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, height, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(offset, 0.0F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.42F, 0.42F, 0.42F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffers, anvil.getLevel(), (int)anvil.getBlockPos().asLong() + seedOffset);
        poseStack.popPose();
    }
}
