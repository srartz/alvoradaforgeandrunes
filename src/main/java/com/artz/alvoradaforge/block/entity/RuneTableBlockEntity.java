package com.artz.alvoradaforge.block.entity;

import com.artz.alvoradaforge.item.RuneInkItem;
import com.artz.alvoradaforge.network.OpenRuneScreenPayload;
import com.artz.alvoradaforge.registry.ModBlockEntities;
import com.artz.alvoradaforge.registry.ModBlocks;
import com.artz.alvoradaforge.registry.ModItems;
import com.artz.alvoradaforge.rune.RunePatternValidator;
import com.artz.alvoradaforge.rune.RuneType;
import com.artz.alvoradaforge.progression.PlayerProgression;
import com.artz.alvoradaforge.rune.RuneService;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

public final class RuneTableBlockEntity extends BlockEntity {
    public static final int STONE_SLOT = 0;
    public static final int FEATHER_SLOT = 1;
    public static final int INK_SLOT = 2;

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };
    @Nullable
    private UUID owner;
    private long ownerSince;

    public RuneTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNE_TABLE.get(), pos, state);
    }

    public void interact(Player player, ItemStack held) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (player.isCrouching()) {
            returnInputs(player);
            return;
        }
        if (isReady()) {
            openDrawingScreen(player);
            return;
        }
        if (held.isEmpty()) {
            showStatus(player);
            return;
        }

        int slot = slotFor(held);
        if (slot < 0) {
            message(player, isAncestral()
                    ? "message.alvoradaforge.ancestral_rune_table_invalid_item"
                    : "message.alvoradaforge.rune_table_invalid_item", ChatFormatting.RED);
            return;
        }
        if (!items.getStackInSlot(slot).isEmpty()) {
            message(player, "message.alvoradaforge.rune_table_slot_filled", ChatFormatting.YELLOW);
            return;
        }

        items.setStackInSlot(slot, held.copyWithCount(1));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.65F, 1.15F);
        if (isReady()) {
            openDrawingScreen(player);
        } else {
            showStatus(player);
        }
    }

    private int slotFor(ItemStack stack) {
        boolean ancestral = isAncestral();
        if (stack.is(ancestral ? ModItems.ANCESTRAL_RUNE_STONE.get() : ModItems.RUNE_STONE.get())) {
            return STONE_SLOT;
        }
        if (stack.is(ancestral ? ModItems.ANCESTRAL_FEATHER.get() : Items.FEATHER)) {
            return FEATHER_SLOT;
        }
        if (stack.getItem() instanceof RuneInkItem ink && (ancestral ? ink.maxTier() == 10 : ink.maxTier() <= 7)) {
            return INK_SLOT;
        }
        return -1;
    }

    private void openDrawingScreen(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(items.getStackInSlot(INK_SLOT).getItem() instanceof RuneInkItem ink)) {
            return;
        }
        if (owner != null && !owner.equals(player.getUUID()) && level.getGameTime() - ownerSince < 20L * 60L) {
            message(player, "message.alvoradaforge.rune_table_in_use", ChatFormatting.RED);
            return;
        }
        owner = player.getUUID();
        ownerSince = level.getGameTime();
        sync();
        int minTier = isAncestral() ? 8 : 1;
        RuneType initial = ink.runeFamily().runes().get(minTier - 1);
        PacketDistributor.sendToPlayer(serverPlayer,
                new OpenRuneScreenPayload(worldPosition, initial, minTier, ink.maxTier()));
    }

    public void completeRune(ServerPlayer player, RuneType submittedType, byte[] packedPoints) {
        if (!(level instanceof ServerLevel serverLevel)
                || owner == null
                || !owner.equals(player.getUUID())
                || !(items.getStackInSlot(INK_SLOT).getItem() instanceof RuneInkItem ink)
                || ink.runeFamily() != submittedType.family()
                || submittedType.tier() < (isAncestral() ? 8 : 1)
                || submittedType.tier() > ink.maxTier()
                || !isReady()) {
            message(player, "message.alvoradaforge.rune_table_invalid_attempt", ChatFormatting.RED);
            return;
        }
        if (!PlayerProgression.canDrawRune(player, submittedType.tier())) {
            message(player, "message.alvoradaforge.rune_knowledge_locked", ChatFormatting.RED);
            return;
        }

        RunePatternValidator.Result validation = RunePatternValidator.validate(submittedType, packedPoints);
        consumeInputs();
        owner = null;
        ownerSince = 0L;
        if (validation.success()) {
            ItemStack result = RuneService.createInscribedRune(submittedType, validation.accuracy());
            if (!player.addItem(result)) {
                Block.popResource(level, worldPosition.above(), result);
            }
            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(
                            ((submittedType.color() >> 16) & 255) / 255.0F,
                            ((submittedType.color() >> 8) & 255) / 255.0F,
                            (submittedType.color() & 255) / 255.0F
                    ), 1.25F),
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    22, 0.3, 0.2, 0.3, 0.04
            );
            level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.35F);
            player.displayClientMessage(Component.translatable(
                    "message.alvoradaforge.rune_success", validation.accuracy()
            ).withStyle(ChatFormatting.GREEN), true);
        } else {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.7F);
            player.displayClientMessage(Component.translatable(
                    "message.alvoradaforge.rune_failed", validation.accuracy()
            ).withStyle(ChatFormatting.RED), true);
        }
        sync();
    }

    private void consumeInputs() {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public void returnInputs(Player player) {
        if (owner != null && !owner.equals(player.getUUID())) {
            message(player, "message.alvoradaforge.rune_table_in_use", ChatFormatting.RED);
            return;
        }
        boolean returnedAny = false;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                returnedAny = true;
                ItemStack returned = stack.copy();
                items.setStackInSlot(slot, ItemStack.EMPTY);
                if (!player.addItem(returned) && level != null) {
                    Block.popResource(level, worldPosition.above(), returned);
                }
            }
        }
        owner = null;
        ownerSince = 0L;
        message(player, returnedAny
                ? "message.alvoradaforge.rune_inputs_returned"
                : "message.alvoradaforge.rune_table_empty", returnedAny ? ChatFormatting.YELLOW : ChatFormatting.GRAY);
        sync();
    }

    private void showStatus(Player player) {
        boolean ancestral = isAncestral();
        Component stone = statusComponent(STONE_SLOT, ancestral
                ? "item.alvoradaforge.ancestral_rune_stone" : "item.alvoradaforge.rune_stone");
        Component feather = statusComponent(FEATHER_SLOT, ancestral
                ? "item.alvoradaforge.ancestral_feather" : "item.minecraft.feather");
        Component ink = statusComponent(INK_SLOT, ancestral
                ? "message.alvoradaforge.any_ancestral_rune_ink" : "message.alvoradaforge.any_rune_ink");
        player.displayClientMessage(Component.translatable("message.alvoradaforge.rune_table_status", stone, feather, ink), true);
    }

    private Component statusComponent(int slot, String key) {
        return Component.translatable(key).withStyle(items.getStackInSlot(slot).isEmpty()
                ? ChatFormatting.RED : ChatFormatting.GREEN);
    }

    public boolean isReady() {
        return !items.getStackInSlot(STONE_SLOT).isEmpty()
                && !items.getStackInSlot(FEATHER_SLOT).isEmpty()
                && items.getStackInSlot(INK_SLOT).getItem() instanceof RuneInkItem;
    }

    public boolean isAncestral() {
        return getBlockState().is(ModBlocks.ANCESTRAL_RUNE_TABLE.get());
    }

    public ItemStack getDisplayedItem(int slot) {
        return items.getStackInSlot(slot);
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
        owner = null;
        ownerSince = 0L;
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
        writeData(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            items.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerSince = tag.getLong("OwnerSince");
    }

    private CompoundTag writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("Inventory", items.serializeNBT(registries));
        if (owner != null) {
            tag.putUUID("Owner", owner);
            tag.putLong("OwnerSince", ownerSince);
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return writeData(new CompoundTag(), registries);
    }
}
