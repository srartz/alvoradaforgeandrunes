package com.artz.alvoradaforge.block.entity;

import com.artz.alvoradaforge.block.MysteryRuneStoneBlock;
import com.artz.alvoradaforge.registry.ModBlockEntities;
import com.artz.alvoradaforge.registry.ModItems;
import com.artz.alvoradaforge.rune.RuneType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class RuneBreakerTableBlockEntity extends BlockEntity {
    public static final int STONE_SLOT = 0;
    public static final int RESULT_SLOT = 1;

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };

    public RuneBreakerTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNE_BREAKER_TABLE.get(), pos, state);
    }

    public void insertStone(Player player, ItemStack held) {
        if (!held.is(ModItems.MYSTERY_RUNE_STONE.get())) {
            message(player, "message.alvoradaforge.breaker_requires_mystery_stone", ChatFormatting.RED);
            return;
        }
        if (!items.getStackInSlot(STONE_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.breaker_occupied", ChatFormatting.YELLOW);
            return;
        }
        items.setStackInSlot(STONE_SLOT, held.copyWithCount(1));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.7F, 0.8F);
        }
        message(player, "message.alvoradaforge.breaker_ready", ChatFormatting.LIGHT_PURPLE);
    }

    public void crack(Player player, ItemStack hammerStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (items.getStackInSlot(STONE_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.breaker_no_stone", ChatFormatting.RED);
            return;
        }
        if (!items.getStackInSlot(RESULT_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.collect_result", ChatFormatting.YELLOW);
            return;
        }

        RuneType rune = MysteryRuneStoneBlock.roll(serverLevel.random);
        items.setStackInSlot(STONE_SLOT, ItemStack.EMPTY);
        items.setStackInSlot(RESULT_SLOT, new ItemStack(rune.runeItem().get()));
        if (!hammerStack.isEmpty()) {
            EquipmentSlot slot = hammerStack == player.getOffhandItem() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            hammerStack.hurtAndBreak(1, player, slot);
        }

        serverLevel.sendParticles(ParticleTypes.PORTAL,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.05, worldPosition.getZ() + 0.5,
                32, 0.32, 0.18, 0.32, 0.12);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1.0F, 0.6F);
        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.9F, 1.3F);
        player.displayClientMessage(Component.translatable(
                "message.alvoradaforge.breaker_revealed",
                Component.translatable("rune.alvoradaforge." + rune.serializedName())
        ).withStyle(rune.tier() >= 8 ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE), true);
        sync();
    }

    public boolean takeResult(Player player) {
        ItemStack result = items.getStackInSlot(RESULT_SLOT);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack taken = result.copy();
        items.setStackInSlot(RESULT_SLOT, ItemStack.EMPTY);
        if (!player.addItem(taken) && level != null) {
            Block.popResource(level, worldPosition.above(), taken);
        }
        sync();
        return true;
    }

    public void returnStone(Player player) {
        ItemStack stone = items.getStackInSlot(STONE_SLOT);
        if (stone.isEmpty()) {
            message(player, "message.alvoradaforge.breaker_empty", ChatFormatting.GRAY);
            return;
        }
        ItemStack returned = stone.copy();
        items.setStackInSlot(STONE_SLOT, ItemStack.EMPTY);
        if (!player.addItem(returned) && level != null) {
            Block.popResource(level, worldPosition.above(), returned);
        }
        message(player, "message.alvoradaforge.inputs_returned", ChatFormatting.YELLOW);
        sync();
    }

    public void showStatus(Player player) {
        if (!items.getStackInSlot(RESULT_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.collect_result", ChatFormatting.YELLOW);
        } else if (!items.getStackInSlot(STONE_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.breaker_ready", ChatFormatting.LIGHT_PURPLE);
        } else {
            message(player, "message.alvoradaforge.breaker_place_stone", ChatFormatting.GRAY);
        }
    }

    public ItemStack getDisplayedItem(int slot) {
        return items.getStackInSlot(slot);
    }

    public boolean hasResult() {
        return !items.getStackInSlot(RESULT_SLOT).isEmpty();
    }

    public boolean hasStone() {
        return !items.getStackInSlot(STONE_SLOT).isEmpty();
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private void message(Player player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            items.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("Inventory", items.serializeNBT(registries));
        return tag;
    }
}
