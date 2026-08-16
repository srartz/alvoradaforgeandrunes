package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> LAPIDARY_ABRASIVES = tag("lapidary_abrasives");
    public static final TagKey<Item> LAPIDARY_SAW_BLADES = tag("lapidary_saw_blades");

    private ModItemTags() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, AlvoradaForge.id(path));
    }
}
