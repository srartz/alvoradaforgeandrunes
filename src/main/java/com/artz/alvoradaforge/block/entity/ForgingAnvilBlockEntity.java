package com.artz.alvoradaforge.block.entity;

import com.artz.alvoradaforge.forging.ForgeQuality;
import com.artz.alvoradaforge.forging.ForgeRecipe;
import com.artz.alvoradaforge.forging.ForgeRecipeManager;
import com.artz.alvoradaforge.forging.ForgingService;
import com.artz.alvoradaforge.item.ForgeHammerItem;
import com.artz.alvoradaforge.registry.ModBlockEntities;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ForgingAnvilBlockEntity extends BlockEntity {
    public static final int BASE_SLOT = 0;
    public static final int ADDITION_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final double CENTER_HIT_TOLERANCE = 0.055;
    private static final double MIN_TARGET_CENTER = 0.18;
    private static final double MAX_TARGET_CENTER = 0.82;
    private static final double MIN_TARGET_MOVE = 0.16;

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }
    };

    private String activeRecipeId = "";
    @Nullable
    private UUID owner;
    private int progress;
    private int successfulHits;
    private int experienceSpent;
    private int requiredHits;
    private int baseCycleTicks = 36;
    private double totalScore;
    private double targetCenter = 0.5;
    private long cycleStarted;
    private long lastHitGameTime = Long.MIN_VALUE;

    public ForgingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGING_ANVIL.get(), pos, state);
    }

    public void insert(Player player, ItemStack held) {
        if (level == null || held.isEmpty()) {
            return;
        }
        if (hasResult()) {
            message(player, "message.alvoradaforge.collect_result", ChatFormatting.YELLOW);
            return;
        }
        if (isActive()) {
            message(player, "message.alvoradaforge.forging_in_progress", ChatFormatting.RED);
            return;
        }

        int slot;
        if (items.getStackInSlot(BASE_SLOT).isEmpty()) {
            slot = BASE_SLOT;
        } else {
            slot = ADDITION_SLOT;
            ItemStack existing = items.getStackInSlot(slot);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, held)) {
                message(player, "message.alvoradaforge.remove_inputs", ChatFormatting.RED);
                return;
            }
        }

        ItemStack inserted = items.getStackInSlot(slot);
        if (inserted.isEmpty()) {
            items.setStackInSlot(slot, held.copyWithCount(1));
        } else if (inserted.getCount() < inserted.getMaxStackSize()) {
            inserted.grow(1);
            sync();
        } else {
            return;
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.7F, 0.9F);

        Optional<ForgeRecipe> recipe = ForgeRecipeManager.INSTANCE.findExact(
                items.getStackInSlot(BASE_SLOT),
                items.getStackInSlot(ADDITION_SLOT)
        );
        recipe.ifPresent(value -> {
            if (value.canUse(player)) {
                start(value, player);
            } else {
                message(player, "message.alvoradaforge.forging_knowledge_locked", ChatFormatting.RED);
            }
        });
    }

    private void start(ForgeRecipe recipe, Player player) {
        activeRecipeId = recipe.id().toString();
        owner = player.getUUID();
        progress = 0;
        successfulHits = 0;
        experienceSpent = 0;
        totalScore = 0.0;
        targetCenter = 0.5;
        requiredHits = recipe.requiredHits();
        baseCycleTicks = recipe.cycleTicks();
        cycleStarted = level == null ? 0L : level.getGameTime();
        sync();
        message(player, "message.alvoradaforge.forging_started", ChatFormatting.GOLD, requiredHits);
    }

    public void strike(ServerPlayer player, ItemStack hammerStack, ForgeHammerItem hammer, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!isActive()) {
            message(player, "message.alvoradaforge.no_recipe", ChatFormatting.RED);
            return;
        }
        if (owner != null && !owner.equals(player.getUUID())) {
            message(player, "message.alvoradaforge.anvil_in_use", ChatFormatting.RED);
            return;
        }

        ForgeRecipe recipe = ForgeRecipeManager.INSTANCE.byId(activeRecipeId).orElse(null);
        if (recipe == null) {
            cancelAndReturn(player);
            message(player, "message.alvoradaforge.recipe_removed", ChatFormatting.RED);
            return;
        }
        if (!recipe.canUse(player)) {
            message(player, "message.alvoradaforge.forging_knowledge_locked", ChatFormatting.RED);
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime == lastHitGameTime) {
            return;
        }
        lastHitGameTime = gameTime;

        int nextProgress = Math.min(requiredHits, progress + hammer.forgingPower());
        int nextExperienceTotal = Mth.ceil((double)recipe.experienceCost() * nextProgress / requiredHits);
        int experienceThisHit = Math.max(0, nextExperienceTotal - experienceSpent);
        if (!player.getAbilities().instabuild && player.experienceLevel < experienceThisHit) {
            message(player, "message.alvoradaforge.not_enough_levels", ChatFormatting.RED, experienceThisHit);
            return;
        }

        double distanceFromTarget = closestRecentMarkerDistance(gameTime, hammer);
        boolean centralHit = distanceFromTarget <= CENTER_HIT_TOLERANCE;
        double score = Mth.clamp(1.0 - distanceFromTarget * 2.0 + hammer.precisionBonus(), 0.0, 1.0);
        totalScore += score;
        successfulHits++;
        progress = nextProgress;
        if (!player.getAbilities().instabuild && experienceThisHit > 0) {
            player.giveExperienceLevels(-experienceThisHit);
            experienceSpent += experienceThisHit;
        }
        cycleStarted = gameTime;

        boolean targetShifted = centralHit
                && progress < requiredHits
                && serverLevel.random.nextFloat() < hammer.targetRelocationChance();
        if (targetShifted) {
            relocateTarget(serverLevel);
        }

        EquipmentSlot equipmentSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        hammerStack.hurtAndBreak(1, player, equipmentSlot);
        serverLevel.sendParticles(
                score >= 0.85 ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.CRIT,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.05,
                worldPosition.getZ() + 0.5,
                8,
                0.28,
                0.08,
                0.28,
                0.08
        );
        level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.9F, 0.85F + (float)score * 0.3F);
        showHitMessage(player, score);
        if (targetShifted) {
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.45F);
            message(player, "message.alvoradaforge.target_shifted", ChatFormatting.LIGHT_PURPLE);
        }

        if (progress >= requiredHits) {
            complete(player, recipe);
        } else {
            sync();
        }
    }

    private void complete(ServerPlayer player, ForgeRecipe recipe) {
        double average = successfulHits == 0 ? 0.0 : totalScore / successfulHits;
        ForgeQuality quality = qualityFromScore(average);
        ItemStack result = ForgingService.createResult(recipe, items.getStackInSlot(BASE_SLOT), player, quality);

        items.setStackInSlot(BASE_SLOT, ItemStack.EMPTY);
        items.setStackInSlot(ADDITION_SLOT, ItemStack.EMPTY);
        items.setStackInSlot(RESULT_SLOT, result);
        resetActive();
        sync();

        player.displayClientMessage(Component.translatable(
                "message.alvoradaforge.forging_complete",
                Component.translatable("quality.alvoradaforge." + quality.getSerializedName()).withStyle(quality.color())
        ).withStyle(ChatFormatting.GREEN), true);
    }

    private static ForgeQuality qualityFromScore(double score) {
        if (score >= 0.97) {
            return ForgeQuality.MASTER;
        }
        if (score >= 0.84) {
            return ForgeQuality.PERFECT;
        }
        if (score >= 0.60) {
            return ForgeQuality.EXPERT;
        }
        if (score >= 0.32) {
            return ForgeQuality.WELL;
        }
        return ForgeQuality.POOR;
    }

    private static void showHitMessage(Player player, double score) {
        String key;
        ChatFormatting color;
        if (score >= 0.90) {
            key = "message.alvoradaforge.hit_perfect";
            color = ChatFormatting.GOLD;
        } else if (score >= 0.65) {
            key = "message.alvoradaforge.hit_expert";
            color = ChatFormatting.AQUA;
        } else if (score >= 0.35) {
            key = "message.alvoradaforge.hit_good";
            color = ChatFormatting.GREEN;
        } else {
            key = "message.alvoradaforge.hit_poor";
            color = ChatFormatting.RED;
        }
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
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
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.1F);
        }
        sync();
        return true;
    }

    public void cancelAndReturn(Player player) {
        if (items.getStackInSlot(BASE_SLOT).isEmpty() && items.getStackInSlot(ADDITION_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.nothing_to_remove", ChatFormatting.GRAY);
            return;
        }
        returnToPlayer(player, items.getStackInSlot(BASE_SLOT));
        returnToPlayer(player, items.getStackInSlot(ADDITION_SLOT));
        items.setStackInSlot(BASE_SLOT, ItemStack.EMPTY);
        items.setStackInSlot(ADDITION_SLOT, ItemStack.EMPTY);
        resetActive();
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
        if (isActive()) {
            player.displayClientMessage(Component.translatable(
                    "message.alvoradaforge.progress", progress, requiredHits
            ).withStyle(ChatFormatting.GOLD), true);
        } else if (!items.getStackInSlot(BASE_SLOT).isEmpty()) {
            message(player, "message.alvoradaforge.waiting_ingredient", ChatFormatting.YELLOW);
        } else {
            message(player, "message.alvoradaforge.place_ingredient", ChatFormatting.GRAY);
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
        resetActive();
    }

    public double markerPosition(long gameTime, float partialTick, ForgeHammerItem hammer) {
        int cycle = effectiveCycleTicks(hammer);
        double elapsed = Math.max(0.0, gameTime + partialTick - cycleStarted);
        double phase = (elapsed % cycle) / (cycle / 2.0);
        return phase <= 1.0 ? phase : 2.0 - phase;
    }

    public int effectiveCycleTicks(ForgeHammerItem hammer) {
        float initialCycle = baseCycleTicks * hammer.controlMultiplier();
        float accelerated = initialCycle - successfulHits * hammer.speedupPerHit();
        return Math.max(8, Math.round(accelerated));
    }

    private double closestRecentMarkerDistance(long gameTime, ForgeHammerItem hammer) {
        double closest = Double.MAX_VALUE;
        // Compensa a pequena diferença entre o quadro visto pelo jogador e o tick
        // em que o pacote de interação é processado no servidor.
        for (int ticksAgo = 0; ticksAgo <= 2; ticksAgo++) {
            double marker = markerPosition(gameTime - ticksAgo, 0.0F, hammer);
            closest = Math.min(closest, Math.abs(marker - targetCenter));
        }
        return closest;
    }

    private void relocateTarget(ServerLevel serverLevel) {
        double previous = targetCenter;
        double candidate = previous;
        for (int attempt = 0; attempt < 8 && Math.abs(candidate - previous) < MIN_TARGET_MOVE; attempt++) {
            candidate = Mth.lerp(serverLevel.random.nextDouble(), MIN_TARGET_CENTER, MAX_TARGET_CENTER);
        }
        if (Math.abs(candidate - previous) < MIN_TARGET_MOVE) {
            candidate = previous < 0.5 ? MAX_TARGET_CENTER : MIN_TARGET_CENTER;
        }
        targetCenter = candidate;
    }

    private void resetActive() {
        activeRecipeId = "";
        owner = null;
        progress = 0;
        successfulHits = 0;
        experienceSpent = 0;
        requiredHits = 0;
        totalScore = 0.0;
        targetCenter = 0.5;
        cycleStarted = 0L;
        lastHitGameTime = Long.MIN_VALUE;
    }

    public ItemStack getDisplayedItem(int slot) {
        return items.getStackInSlot(slot);
    }

    public boolean hasResult() {
        return !items.getStackInSlot(RESULT_SLOT).isEmpty();
    }

    public boolean isActive() {
        return !activeRecipeId.isEmpty();
    }

    public int getProgress() {
        return progress;
    }

    public int getRequiredHits() {
        return requiredHits;
    }

    public double getTargetCenter() {
        return targetCenter;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
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
        activeRecipeId = tag.getString("ActiveRecipe");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        progress = tag.getInt("Progress");
        successfulHits = tag.getInt("SuccessfulHits");
        experienceSpent = tag.getInt("ExperienceSpent");
        requiredHits = tag.getInt("RequiredHits");
        baseCycleTicks = tag.contains("CycleTicks") ? tag.getInt("CycleTicks") : 36;
        totalScore = tag.getDouble("TotalScore");
        targetCenter = tag.contains("TargetCenter") ? tag.getDouble("TargetCenter") : 0.5;
        cycleStarted = tag.getLong("CycleStarted");
    }

    private CompoundTag writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("Inventory", items.serializeNBT(registries));
        tag.putString("ActiveRecipe", activeRecipeId);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        tag.putInt("Progress", progress);
        tag.putInt("SuccessfulHits", successfulHits);
        tag.putInt("ExperienceSpent", experienceSpent);
        tag.putInt("RequiredHits", requiredHits);
        tag.putInt("CycleTicks", baseCycleTicks);
        tag.putDouble("TotalScore", totalScore);
        tag.putDouble("TargetCenter", targetCenter);
        tag.putLong("CycleStarted", cycleStarted);
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
