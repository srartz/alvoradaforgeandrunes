package com.artz.alvoradaforge.block;

import com.artz.alvoradaforge.block.entity.RuneBreakerTableBlockEntity;
import com.artz.alvoradaforge.item.ForgeHammerItem;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class RuneBreakerTableBlock extends BaseEntityBlock {
    public static final MapCodec<RuneBreakerTableBlock> CODEC = simpleCodec(RuneBreakerTableBlock::new);

    public RuneBreakerTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RuneBreakerTableBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof RuneBreakerTableBlockEntity table)) {
            return ItemInteractionResult.FAIL;
        }
        if (table.hasResult()) {
            table.takeResult(player);
        } else if (player.isCrouching()) {
            table.returnStone(player);
        } else if (stack.getItem() instanceof ForgeHammerItem) {
            table.crack(player, stack);
        } else {
            table.insertStone(player, stack);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof RuneBreakerTableBlockEntity table)) {
            return InteractionResult.FAIL;
        }
        if (table.hasResult()) {
            table.takeResult(player);
        } else if (player.isCrouching()) {
            table.returnStone(player);
        } else if (table.hasStone()) {
            table.crack(player, ItemStack.EMPTY);
        } else {
            table.showStatus(player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RuneBreakerTableBlockEntity table) {
                table.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
