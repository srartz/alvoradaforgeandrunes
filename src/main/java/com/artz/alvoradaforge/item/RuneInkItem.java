package com.artz.alvoradaforge.item;

import com.artz.alvoradaforge.rune.RuneFamily;
import net.minecraft.world.item.Item;

public final class RuneInkItem extends Item {
    private final RuneFamily runeFamily;

    public RuneInkItem(Properties properties, RuneFamily runeFamily) {
        super(properties);
        this.runeFamily = runeFamily;
    }

    public RuneFamily runeFamily() {
        return runeFamily;
    }
}
