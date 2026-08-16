package com.artz.alvoradaforge.rune;

import com.artz.alvoradaforge.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class RuneService {
    private RuneService() {
    }

    public static ItemStack createInscribedRune(RuneType type, int accuracy) {
        ItemStack result = new ItemStack(type.runeItem().get());
        result.set(ModDataComponents.RUNE_ACCURACY.get(), Math.max(0, Math.min(100, accuracy)));
        return result;
    }
}
