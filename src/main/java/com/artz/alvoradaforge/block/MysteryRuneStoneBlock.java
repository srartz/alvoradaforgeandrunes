package com.artz.alvoradaforge.block;

import com.artz.alvoradaforge.item.ForgeHammerItem;
import com.artz.alvoradaforge.rune.RuneType;
import com.artz.alvoradaforge.rune.RuneFamily;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Gacha stone that yields one weighted random rune when mined with a forge hammer. */
public final class MysteryRuneStoneBlock extends Block {
    // Chance total por nivel. Nivel 10: 0,0001% (uma lendaria a cada ~1 milhao de pedras).
    private static final double[] TIER_CHANCES = {
            42.0, 25.0, 15.0, 9.0, 5.0, 2.5, 1.0, 0.4, 0.0999, 0.0001
    };

    public MysteryRuneStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!(player.getMainHandItem().getItem() instanceof ForgeHammerItem hammer)) {
            return 0.0F;
        }
        return 0.05F + hammer.precisionBonus() + (hammer.forgingPower() - 1) * 0.03F;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null || !(tool.getItem() instanceof ForgeHammerItem)) {
            return List.of();
        }
        RuneType rune = roll(params.getLevel().getRandom());
        return List.of(new ItemStack(rune.runeItem().get()));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!(tool.getItem() instanceof ForgeHammerItem)) {
            return;
        }
        EquipmentSlot slot = tool == player.getOffhandItem() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        tool.hurtAndBreak(1, player, slot);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    28, 0.35, 0.35, 0.35, 0.12);
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1.0F, 0.65F);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.8F, 1.35F);
        }
    }

    public static RuneType roll(RandomSource random) {
        double rolled = random.nextDouble() * 100.0;
        int tier = 10;
        for (int index = 0; index < TIER_CHANCES.length; index++) {
            rolled -= TIER_CHANCES[index];
            if (rolled < 0.0) {
                tier = index + 1;
                break;
            }
        }
        RuneFamily family = RuneFamily.values()[random.nextInt(RuneFamily.values().length)];
        return family.runes().get(tier - 1);
    }

    public static double tierChance(int tier) {
        return tier >= 1 && tier <= TIER_CHANCES.length ? TIER_CHANCES[tier - 1] : 0.0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.alvoradaforge.mystery_rune_stone.hammer")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.mystery_rune_stone.rarity")
                .withStyle(ChatFormatting.GRAY));
    }
}
