package com.artz.alvoradaforge.block;

import com.artz.alvoradaforge.block.entity.ForgingAnvilBlockEntity;
import com.artz.alvoradaforge.item.ForgeHammerItem;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ForgingAnvilBlock extends AnvilBlock implements EntityBlock {
    public static final MapCodec<AnvilBlock> CODEC = simpleCodec(ForgingAnvilBlock::new);

    public ForgingAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AnvilBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForgingAnvilBlockEntity(pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // A estação mantém inventário e progresso; ela não pode virar uma FallingBlockEntity.
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Sem partículas de queda, pois esta versão da bigorna é uma estação fixa.
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof ForgingAnvilBlockEntity anvil)) {
            return ItemInteractionResult.FAIL;
        }

        // O resultado sempre tem prioridade, inclusive quando o jogador ainda está
        // segurando o martelo. Agachar também deve funcionar com qualquer item na mão.
        if (anvil.hasResult()) {
            anvil.takeResult(player);
            return ItemInteractionResult.CONSUME;
        }
        if (player.isCrouching()) {
            anvil.cancelAndReturn(player);
            return ItemInteractionResult.CONSUME;
        }

        if (stack.getItem() instanceof ForgeHammerItem hammer && player instanceof ServerPlayer serverPlayer) {
            anvil.strike(serverPlayer, stack, hammer, hand);
        } else {
            anvil.insert(player, stack);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof ForgingAnvilBlockEntity anvil)) {
            return InteractionResult.FAIL;
        }

        // Primeiro entrega o item pronto. Antes, Shift + clique caía no cancelamento
        // e nunca alcançava takeResult(), deixando o resultado preso na bigorna.
        if (anvil.hasResult()) {
            anvil.takeResult(player);
        } else if (player.isCrouching()) {
            anvil.cancelAndReturn(player);
        } else {
            anvil.showStatus(player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ForgingAnvilBlockEntity anvil) {
                anvil.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
