package com.artz.alvoradaforge.forging;

import com.artz.alvoradaforge.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ForgingService {
    private ForgingService() {
    }

    public static ItemStack createResult(ForgeRecipe recipe, ItemStack base, Player player, ForgeQuality quality) {
        ItemStack result = recipe.createResult(base);
        ForgedStats stats = recipe.statsFor(quality);

        result.set(ModDataComponents.FORGING_QUALITY.get(), quality);
        result.set(ModDataComponents.FORGED_STATS.get(), stats);
        result.set(ModDataComponents.FORGING_BENEFITS.get(), recipe.benefits());
        result.set(ModDataComponents.FORGED_BY.get(), player.getScoreboardName());
        result.set(ModDataComponents.FORGE_RECIPE.get(), recipe.id().toString());

        if (ForgeBenefits.has(recipe.benefits(), ForgeBenefits.DURABILITY) && result.isDamageableItem()) {
            int baseDurability = new ItemStack(recipe.resultItem()).getMaxDamage();
            result.set(DataComponents.MAX_DAMAGE, Math.max(1, Math.round(baseDurability * stats.durabilityMultiplier())));
        }
        return result;
    }

    public static void applyRequestedName(ItemStack result, String requestedName) {
        if (requestedName == null) {
            return;
        }
        if (requestedName.isBlank()) {
            result.remove(DataComponents.CUSTOM_NAME);
        } else {
            result.set(DataComponents.CUSTOM_NAME, Component.literal(requestedName));
        }
    }
}
