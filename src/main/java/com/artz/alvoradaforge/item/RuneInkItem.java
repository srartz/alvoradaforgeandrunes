package com.artz.alvoradaforge.item;

import com.artz.alvoradaforge.rune.RuneFamily;
import net.minecraft.world.item.Item;

public final class RuneInkItem extends Item {
    private final RuneFamily runeFamily;
    private final int maxTier;

    public RuneInkItem(Properties properties, RuneFamily runeFamily, int maxTier) {
        super(properties);
        this.runeFamily = runeFamily;
        this.maxTier = maxTier;
    }

    public RuneFamily runeFamily() {
        return runeFamily;
    }

    public int maxTier() {
        return maxTier;
    }
}
