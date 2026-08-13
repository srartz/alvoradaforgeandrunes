package com.artz.alvoradaforge.forging;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record ForgeRecipe(
        ResourceLocation id,
        List<Input> inputs,
        Item resultItem,
        int resultCount,
        int experienceCost,
        ForgeQuality quality,
        ForgedStats stats,
        int benefits,
        boolean copyInputComponents,
        float anvilDamageChance,
        int requiredHits,
        int cycleTicks,
        boolean customBonuses,
        int priority
) {
    public boolean matches(ItemStack left, ItemStack right) {
        if (inputs.isEmpty() || inputs.size() > 2 || left.getCount() != inputs.getFirst().count()) {
            return false;
        }
        if (!inputs.getFirst().ingredient().test(left)) {
            return false;
        }
        if (inputs.size() == 1) {
            return right.isEmpty();
        }
        Input addition = inputs.get(1);
        return addition.ingredient().test(right) && right.getCount() >= addition.count();
    }

    public int materialCost() {
        return inputs.size() == 2 ? inputs.get(1).count() : 0;
    }

    public ItemStack createResult(ItemStack left) {
        return copyInputComponents
                ? left.transmuteCopy(resultItem, resultCount)
                : new ItemStack(resultItem, resultCount);
    }

    public ForgedStats statsFor(ForgeQuality forgedQuality) {
        return customBonuses ? stats : ForgedStats.defaults(forgedQuality);
    }

    public record Input(Ingredient ingredient, int count) {
    }
}
