package com.artz.alvoradaforge.forging;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import java.util.Locale;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;

public final class ForgeBenefits {
    public static final int DURABILITY = 1;
    public static final int TOOL = 1 << 1;
    public static final int WEAPON = 1 << 2;
    public static final int ARMOR = 1 << 3;

    private ForgeBenefits() {
    }

    public static int automatic(ItemStack result) {
        int mask = 0;
        if (result.isDamageableItem()) {
            mask |= DURABILITY;
        }
        if (result.getItem() instanceof DiggerItem) {
            mask |= TOOL;
        }
        if (result.getItem() instanceof TieredItem || result.getItem() instanceof ProjectileWeaponItem) {
            mask |= WEAPON;
        }
        if (result.getItem() instanceof ArmorItem) {
            mask |= ARMOR;
        }
        return mask;
    }

    public static int parse(JsonArray array, ItemStack result) {
        int mask = 0;
        for (JsonElement element : array) {
            String value = element.getAsString().toLowerCase(Locale.ROOT);
            mask |= switch (value) {
                case "auto" -> automatic(result);
                case "durability" -> DURABILITY;
                case "tool" -> TOOL;
                case "weapon" -> WEAPON;
                case "armor" -> ARMOR;
                case "none" -> 0;
                default -> throw new JsonSyntaxException("Beneficio de forja desconhecido: " + value);
            };
        }
        return mask;
    }

    public static boolean has(int mask, int benefit) {
        return (mask & benefit) != 0;
    }
}
