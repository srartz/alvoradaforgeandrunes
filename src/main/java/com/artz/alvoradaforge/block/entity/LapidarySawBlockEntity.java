package com.artz.alvoradaforge.block.entity;

import com.artz.alvoradaforge.registry.ModBlockEntities;
import com.artz.alvoradaforge.registry.ModItemTags;
import com.artz.alvoradaforge.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class LapidarySawBlockEntity extends BlockEntity {
    public static final int STONE_SLOT = 0;
    public static final int ABRASIVE_SLOT = 1;
    public static final int BLADE_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int MAX_WATER_USES = 4;

    private static final int LEY_CUT_TICKS = 300;
    private static final int ANCESTRAL_CUT_TICKS = 600;

    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };

    private int waterUses;
    private int progress;
    private int requiredTicks;
    private boolean processing;

    public LapidarySawBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAPIDARY_SAW.get(), pos, state);
    }

    public void interact(Player player, ItemStack held) {
        if (level == null || held.isEmpty()) {
            return;
        }
        if (processing) {
            message(player, "message.alvoradaforge.lapidary_processing", ChatFormatting.YELLOW,
                    progress, requiredTicks);
            return;
        }
        if (held.is(Items.WATER_BUCKET)) {
            fillWater(player, held);
            tryStart(player);
            return;
        }
        if (held.is(Items.BUCKET)) {
            drainWater(player, held);
            return;
        }

        int slot = slotFor(held);
        if (slot < 0) {
            message(player, "message.alvoradaforge.lapidary_invalid_item", ChatFormatting.RED);
            return;
        }
        if (!items.getStackInSlot(slot).isEmpty()) {
            message(player, "message.alvoradaforge.lapidary_slot_occupied", ChatFormatting.YELLOW);
            return;
        }

        items.setStackInSlot(slot, held.copyWithCount(1));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        playInsertionFeedback(items.getStackInSlot(slot));
        tryStart(player);
    }

    private void playInsertionFeedback(ItemStack inserted) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ParticleOptions particle;
        SoundEvent sound;
        int count;
        float pitch;
        if (inserted.is(ModItems.RAW_LEY_STONE.get())) {
            particle = ParticleTypes.PORTAL;
            sound = SoundEvents.AMETHYST_BLOCK_CHIME;
            count = 12;
            pitch = 0.75F;
        } else if (inserted.is(ModItems.ANCIENT_GEODE_HEART.get())) {
            particle = ParticleTypes.END_ROD;
            sound = SoundEvents.RESPAWN_ANCHOR_CHARGE;
            count = 20;
            pitch = 0.65F;
        } else if (inserted.is(ModItems.QUARTZ_DUST.get())) {
            particle = ParticleTypes.WHITE_ASH;
            sound = SoundEvents.SAND_PLACE;
            count = 10;
            pitch = 1.25F;
        } else if (inserted.is(ModItems.DIAMOND_DUST.get())) {
            particle = ParticleTypes.ENCHANT;
            sound = SoundEvents.AMETHYST_CLUSTER_HIT;
            count = 16;
            pitch = 1.55F;
        } else if (inserted.is(ModItems.DIAMOND_SAW_BLADE.get())) {
            particle = ParticleTypes.ELECTRIC_SPARK;
            sound = SoundEvents.BEACON_POWER_SELECT;
            count = 14;
            pitch = 1.35F;
        } else {
            particle = ParticleTypes.CRIT;
            sound = SoundEvents.CHAIN_PLACE;
            count = 8;
            pitch = 0.8F;
        }
        serverLevel.sendParticles(particle,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.08, worldPosition.getZ() + 0.5,
                count, 0.24, 0.08, 0.24, 0.035);
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 0.8F, pitch);
    }

    private int slotFor(ItemStack stack) {
        if (isCuttableStone(stack)) {
            return STONE_SLOT;
        }
        if (stack.is(ModItemTags.LAPIDARY_ABRASIVES)) {
            return ABRASIVE_SLOT;
        }
        if (stack.is(ModItemTags.LAPIDARY_SAW_BLADES)) {
            return BLADE_SLOT;
        }
        return -1;
    }

    private static boolean isCuttableStone(ItemStack stack) {
        return stack.is(ModItems.RAW_LEY_STONE.get()) || stack.is(ModItems.ANCIENT_GEODE_HEART.get());
    }

    private void fillWater(Player player, ItemStack held) {
        if (waterUses > 0) {
            message(player, "message.alvoradaforge.lapidary_tank_not_empty", ChatFormatting.AQUA, waterUses,
                    MAX_WATER_USES);
            return;
        }
        waterUses = MAX_WATER_USES;
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
            ItemStack bucket = new ItemStack(Items.BUCKET);
            if (!player.addItem(bucket)) {
                Block.popResource(level, worldPosition.above(), bucket);
            }
        }
        level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.9F, 1.0F);
        sync();
        message(player, "message.alvoradaforge.lapidary_water_filled", ChatFormatting.AQUA, waterUses);
    }

    private void drainWater(Player player, ItemStack held) {
        if (waterUses != MAX_WATER_USES) {
            message(player, "message.alvoradaforge.lapidary_water_cannot_drain", ChatFormatting.RED);
            return;
        }
        waterUses = 0;
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
            ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
            if (!player.addItem(waterBucket)) {
                Block.popResource(level, worldPosition.above(), waterBucket);
            }
        }
        level.playSound(null, worldPosition, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.9F, 1.0F);
        sync();
    }

    private void tryStart(Player player) {
        ItemStack stone = items.getStackInSlot(STONE_SLOT);
        if (stone.isEmpty() || items.getStackInSlot(ABRASIVE_SLOT).isEmpty()
                || items.getStackInSlot(BLADE_SLOT).isEmpty() || waterUses <= 0 || hasResult()) {
            return;
        }
        if (stone.is(ModItems.ANCIENT_GEODE_HEART.get())
                && !items.getStackInSlot(BLADE_SLOT).is(ModItems.DIAMOND_SAW_BLADE.get())) {
            message(player, "message.alvoradaforge.lapidary_requires_diamond_blade", ChatFormatting.RED);
            return;
        }

        processing = true;
        progress = 0;
        requiredTicks = stone.is(ModItems.ANCIENT_GEODE_HEART.get()) ? ANCESTRAL_CUT_TICKS : LEY_CUT_TICKS;
        level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8F, 0.65F);
        sync();
        message(player, "message.alvoradaforge.lapidary_started", ChatFormatting.GREEN,
                requiredTicks / 20);
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
                                  LapidarySawBlockEntity saw) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!saw.processing) {
            return;
        }
        if (!saw.hasValidActiveRecipe()) {
            saw.processing = false;
            saw.progress = 0;
            saw.requiredTicks = 0;
            saw.sync();
            return;
        }

        saw.progress++;
        saw.setChanged();
        if (saw.progress % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                    2, 0.28, 0.03, 0.28, 0.02);
        }
        if (saw.progress % 8 == 0) {
            ParticleOptions abrasiveParticle = saw.items.getStackInSlot(ABRASIVE_SLOT)
                    .is(ModItems.DIAMOND_DUST.get()) ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.WHITE_ASH;
            serverLevel.sendParticles(abrasiveParticle,
                    pos.getX() + 0.5, pos.getY() + 1.10, pos.getZ() + 0.5,
                    2, 0.18, 0.025, 0.18, 0.02);
        }
        if (saw.progress % 12 == 0) {
            serverLevel.sendParticles(saw.isAncestralCut() ? ParticleTypes.END_ROD : ParticleTypes.PORTAL,
                    pos.getX() + 0.5, pos.getY() + 1.08, pos.getZ() + 0.5,
                    1, 0.22, 0.04, 0.22, 0.015);
        }
        if (saw.progress % 20 == 0) {
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.45F,
                    saw.isAncestralCut() ? 0.55F : 0.75F);
            saw.sync();
        }
        if (saw.progress >= saw.requiredTicks) {
            saw.complete(serverLevel);
        }
    }

    private boolean hasValidActiveRecipe() {
        ItemStack stone = items.getStackInSlot(STONE_SLOT);
        return isCuttableStone(stone)
                && !items.getStackInSlot(ABRASIVE_SLOT).isEmpty()
                && items.getStackInSlot(ABRASIVE_SLOT).is(ModItemTags.LAPIDARY_ABRASIVES)
                && !items.getStackInSlot(BLADE_SLOT).isEmpty()
                && items.getStackInSlot(BLADE_SLOT).is(ModItemTags.LAPIDARY_SAW_BLADES)
                && (!stone.is(ModItems.ANCIENT_GEODE_HEART.get())
                    || items.getStackInSlot(BLADE_SLOT).is(ModItems.DIAMOND_SAW_BLADE.get()))
                && waterUses > 0
                && !hasResult();
    }

    private boolean isAncestralCut() {
        return items.getStackInSlot(STONE_SLOT).is(ModItems.ANCIENT_GEODE_HEART.get());
    }

    private void complete(ServerLevel level) {
        boolean ancestral = isAncestralCut();
        items.extractItem(STONE_SLOT, 1, false);
        items.extractItem(ABRASIVE_SLOT, 1, false);
        waterUses--;
        damageBlade(ancestral ? 4 : 1);
        items.setStackInSlot(RESULT_SLOT, new ItemStack(
                ancestral ? ModItems.ANCESTRAL_RUNE_STONE.get() : ModItems.RUNE_STONE.get()));

        processing = false;
        progress = 0;
        requiredTicks = 0;
        level.sendParticles(ancestral ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.12, worldPosition.getZ() + 0.5,
                ancestral ? 28 : 16, 0.34, 0.12, 0.34, 0.08);
        level.playSound(null, worldPosition,
                ancestral ? SoundEvents.BEACON_ACTIVATE : SoundEvents.GRINDSTONE_USE,
                SoundSource.BLOCKS, 1.0F, ancestral ? 0.85F : 1.0F);
        sync();
    }

    private void damageBlade(int amount) {
        ItemStack blade = items.getStackInSlot(BLADE_SLOT);
        if (blade.isEmpty() || !blade.isDamageableItem()) {
            return;
        }
        int newDamage = blade.getDamageValue() + amount;
        if (newDamage >= blade.getMaxDamage()) {
            items.setStackInSlot(BLADE_SLOT, ItemStack.EMPTY);
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.9F, 0.8F);
            }
        } else {
            blade.setDamageValue(newDamage);
            sync();
        }
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

    public void cancelAndReturnInputs(Player player) {
        if (items.getStackInSlot(STONE_SLOT).isEmpty()
                && items.getStackInSlot(ABRASIVE_SLOT).isEmpty()
                && items.getStackInSlot(BLADE_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.lapidary_empty", ChatFormatting.GRAY);
            return;
        }
        processing = false;
        progress = 0;
        requiredTicks = 0;
        for (int slot = STONE_SLOT; slot <= BLADE_SLOT; slot++) {
            returnToPlayer(player, items.getStackInSlot(slot));
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        sync();
        message(player, "message.alvoradaforge.inputs_returned", ChatFormatting.YELLOW);
    }

    private void returnToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack returned = stack.copy();
        if (!player.addItem(returned) && level != null) {
            Block.popResource(level, worldPosition.above(), returned);
        }
    }

    public void showStatus(Player player) {
        if (processing) {
            message(player, "message.alvoradaforge.lapidary_processing", ChatFormatting.YELLOW,
                    progress, requiredTicks);
        } else if (hasResult()) {
            message(player, "message.alvoradaforge.lapidary_collect", ChatFormatting.GREEN);
        } else {
            message(player, "message.alvoradaforge.lapidary_status", ChatFormatting.AQUA,
                    waterUses, MAX_WATER_USES);
        }
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
        processing = false;
        progress = 0;
        requiredTicks = 0;
    }

    public ItemStack getDisplayedItem(int slot) {
        return items.getStackInSlot(slot);
    }

    public boolean hasResult() {
        return !items.getStackInSlot(RESULT_SLOT).isEmpty();
    }

    public boolean isProcessing() {
        return processing;
    }

    public int getProgress() {
        return progress;
    }

    public int getRequiredTicks() {
        return requiredTicks;
    }

    public int getWaterUses() {
        return waterUses;
    }

    private void message(Player player, String key, ChatFormatting color, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(color), true);
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
        waterUses = tag.getInt("WaterUses");
        progress = tag.getInt("Progress");
        requiredTicks = tag.getInt("RequiredTicks");
        processing = tag.getBoolean("Processing");
    }

    private CompoundTag writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("Inventory", items.serializeNBT(registries));
        tag.putInt("WaterUses", waterUses);
        tag.putInt("Progress", progress);
        tag.putInt("RequiredTicks", requiredTicks);
        tag.putBoolean("Processing", processing);
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
