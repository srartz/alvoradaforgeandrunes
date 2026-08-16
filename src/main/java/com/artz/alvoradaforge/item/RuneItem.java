package com.artz.alvoradaforge.item;

import com.artz.alvoradaforge.registry.ModDataComponents;
import com.artz.alvoradaforge.rune.RuneType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

public final class RuneItem extends Item {
    private final RuneType runeType;

    public RuneItem(Properties properties, RuneType runeType) {
        super(properties);
        this.runeType = runeType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Component armorEnchantment = Component.translatable(
                Util.makeDescriptionId("enchantment", runeType.armorEnchantment().location()));
        Component toolEnchantment = Component.translatable(
                Util.makeDescriptionId("enchantment", runeType.toolEnchantment().location()));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.rune_tier", runeType.tier())
                .withStyle(runeType.tier() == 10 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.rune_armor_effect",
                armorEnchantment, runeType.armorLevel()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.rune_tool_effect",
                toolEnchantment, runeType.toolLevel()).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.alvoradaforge.rune_passive_effect",
                runeType.passiveEffect().value().getDisplayName(), runeType.passiveAmplifier() + 1)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        Integer accuracy = stack.get(ModDataComponents.RUNE_ACCURACY.get());
        if (accuracy != null) {
            tooltip.add(Component.translatable("tooltip.alvoradaforge.rune_accuracy", accuracy)
                    .withStyle(accuracy >= 90 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack rune = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);
        if (target.isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.alvoradaforge.rune_hold_target")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResultHolder.fail(rune);
        }
        if (target.has(ModDataComponents.RUNE_TYPE.get())) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.alvoradaforge.rune_already_applied")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(rune);
        }

        ResourceKey<Enchantment> enchantmentKey = target.getItem() instanceof ArmorItem
                ? runeType.armorEnchantment()
                : runeType.toolEnchantment();
        int enchantmentLevel = target.getItem() instanceof ArmorItem ? runeType.armorLevel() : runeType.toolLevel();
        Holder<Enchantment> enchantment = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(enchantmentKey);
        if (!enchantment.value().canEnchant(target)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.alvoradaforge.rune_incompatible")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(rune);
        }

        if (!level.isClientSide) {
            target.enchant(enchantment, enchantmentLevel);
            target.set(ModDataComponents.RUNE_TYPE.get(), runeType.serializedName());
            Integer accuracy = rune.get(ModDataComponents.RUNE_ACCURACY.get());
            if (accuracy != null) {
                target.set(ModDataComponents.RUNE_ACCURACY.get(), accuracy);
            }
            if (!player.getAbilities().instabuild) {
                rune.shrink(1);
            }
            player.displayClientMessage(Component.translatable(
                    "message.alvoradaforge.rune_applied",
                    Component.translatable("rune.alvoradaforge." + runeType.serializedName())
            ).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return InteractionResultHolder.sidedSuccess(rune, level.isClientSide);
    }
}
