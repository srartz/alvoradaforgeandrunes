package com.artz.alvoradaforge.forging;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.registry.ModDataComponents;
import com.artz.alvoradaforge.rune.RuneType;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ForgeEvents {
    private static final ResourceLocation ATTACK_DAMAGE_ID = AlvoradaForge.id("forged_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_ID = AlvoradaForge.id("forged_attack_speed");
    private static final ResourceLocation ARMOR_ID = AlvoradaForge.id("forged_armor");
    private static final ResourceLocation ARMOR_TOUGHNESS_ID = AlvoradaForge.id("forged_armor_toughness");
    private static final ResourceLocation MINING_SPEED_ID = AlvoradaForge.id("forged_mining_speed");

    private ForgeEvents() {
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(ForgeRecipeManager.INSTANCE);
    }

    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ForgeRecipeManager.INSTANCE.find(event.getLeft(), event.getRight()).ifPresent(recipe -> {
            ItemStack result = ForgingService.createResult(recipe, event.getLeft(), event.getPlayer(), recipe.quality());
            ForgingService.applyRequestedName(result, event.getName());

            event.setOutput(result);
            event.setCost(recipe.experienceCost());
            event.setMaterialCost(recipe.materialCost());
        });
    }

    public static void onAnvilRepair(AnvilRepairEvent event) {
        String recipeId = event.getOutput().get(ModDataComponents.FORGE_RECIPE.get());
        if (recipeId != null) {
            ForgeRecipeManager.INSTANCE.byId(recipeId)
                    .ifPresent(recipe -> event.setBreakChance(recipe.anvilDamageChance()));
        }
    }

    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ForgedStats stats = stack.get(ModDataComponents.FORGED_STATS.get());
        Integer benefits = stack.get(ModDataComponents.FORGING_BENEFITS.get());
        if (stats == null || benefits == null) {
            return;
        }

        if (ForgeBenefits.has(benefits, ForgeBenefits.WEAPON)) {
            addModifier(event, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, stats.attackDamage(),
                    AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
            addModifier(event, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID, stats.attackSpeed(),
                    AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.MAINHAND);
        }

        if (ForgeBenefits.has(benefits, ForgeBenefits.TOOL)) {
            double multiplierBonus = stats.miningSpeedMultiplier() - 1.0;
            addModifier(event, Attributes.BLOCK_BREAK_SPEED, MINING_SPEED_ID, multiplierBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, EquipmentSlotGroup.MAINHAND);
        }

        if (ForgeBenefits.has(benefits, ForgeBenefits.ARMOR)) {
            Set<EquipmentSlotGroup> slots = armorSlots(event, stack);
            for (EquipmentSlotGroup slot : slots) {
                addModifier(event, Attributes.ARMOR, ARMOR_ID, stats.armor(),
                        AttributeModifier.Operation.ADD_VALUE, slot);
                addModifier(event, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_ID, stats.armorToughness(),
                        AttributeModifier.Operation.ADD_VALUE, slot);
            }
        }
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ForgeQuality quality = event.getItemStack().get(ModDataComponents.FORGING_QUALITY.get());
        int detailLine = Math.min(1, event.getToolTip().size());
        if (quality != null) {
            event.getToolTip().add(detailLine++, Component.translatable(
                    "tooltip.alvoradaforge.forging_quality",
                    Component.translatable("quality.alvoradaforge." + quality.getSerializedName()).withStyle(quality.color())
            ));

            String creator = event.getItemStack().get(ModDataComponents.FORGED_BY.get());
            if (creator != null && !creator.isBlank()) {
                event.getToolTip().add(detailLine++, Component.translatable("tooltip.alvoradaforge.forged_by", creator));
            }
        }

        String rune = event.getItemStack().get(ModDataComponents.RUNE_TYPE.get());
        if (rune != null && !rune.isBlank()) {
            event.getToolTip().add(detailLine, Component.translatable(
                    "tooltip.alvoradaforge.applied_rune",
                    Component.translatable("rune.alvoradaforge." + rune).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE)
            ));
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % 20 != 0) {
            return;
        }
        for (ItemStack armor : player.getArmorSlots()) {
            applyRuneEffect(player, armor);
        }
        applyRuneEffect(player, player.getMainHandItem());
        applyRuneEffect(player, player.getOffhandItem());
    }

    private static void applyRuneEffect(Player player, ItemStack stack) {
        String serialized = stack.get(ModDataComponents.RUNE_TYPE.get());
        if (serialized == null) {
            return;
        }
        try {
            RuneType type = RuneType.valueOf(serialized.toUpperCase(java.util.Locale.ROOT));
            player.addEffect(new MobEffectInstance(
                    type.passiveEffect(), 40, type.passiveAmplifier(), true, false, true));
        } catch (IllegalArgumentException ignored) {
            // Componente antigo/desconhecido: nao derruba o servidor por dados de item externos.
        }
    }

    private static Set<EquipmentSlotGroup> armorSlots(ItemAttributeModifierEvent event, ItemStack stack) {
        Set<EquipmentSlotGroup> slots = new LinkedHashSet<>();
        for (ItemAttributeModifiers.Entry entry : event.getModifiers()) {
            if (entry.attribute().equals(Attributes.ARMOR) || entry.attribute().equals(Attributes.ARMOR_TOUGHNESS)) {
                slots.add(entry.slot());
            }
        }
        if (slots.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
            slots.add(EquipmentSlotGroup.bySlot(armorItem.getEquipmentSlot()));
        }
        return slots;
    }

    private static void addModifier(
            ItemAttributeModifierEvent event,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount,
            AttributeModifier.Operation operation,
            EquipmentSlotGroup slot
    ) {
        if (amount == 0.0) {
            return;
        }
        event.addModifier(attribute, new AttributeModifier(id, amount, operation), slot);
    }
}
