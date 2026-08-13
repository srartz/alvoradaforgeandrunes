package com.artz.alvoradaforge.rune;

import com.artz.alvoradaforge.registry.ModItems;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public enum RuneType {
    EMBER(RuneFamily.EMBER, 1, Enchantments.FIRE_PROTECTION, 1, Enchantments.FIRE_ASPECT, 1, MobEffects.FIRE_RESISTANCE, 0),
    SPARK(RuneFamily.EMBER, 2, Enchantments.PROJECTILE_PROTECTION, 1, Enchantments.UNBREAKING, 1, MobEffects.MOVEMENT_SPEED, 0),
    CINDER(RuneFamily.EMBER, 3, Enchantments.BLAST_PROTECTION, 2, Enchantments.SMITE, 2, MobEffects.DAMAGE_RESISTANCE, 0),
    FLAME(RuneFamily.EMBER, 4, Enchantments.FIRE_PROTECTION, 2, Enchantments.FLAME, 1, MobEffects.FIRE_RESISTANCE, 0),
    BLAZE(RuneFamily.EMBER, 5, Enchantments.PROTECTION, 3, Enchantments.SHARPNESS, 3, MobEffects.DAMAGE_BOOST, 0),
    MAGMA(RuneFamily.EMBER, 6, Enchantments.THORNS, 2, Enchantments.EFFICIENCY, 3, MobEffects.DIG_SPEED, 0),
    INFERNO(RuneFamily.EMBER, 7, Enchantments.FIRE_PROTECTION, 4, Enchantments.LOOTING, 3, MobEffects.FIRE_RESISTANCE, 0),
    PHOENIX(RuneFamily.EMBER, 8, Enchantments.PROTECTION, 4, Enchantments.MENDING, 1, MobEffects.REGENERATION, 0),
    SUNFIRE(RuneFamily.EMBER, 9, Enchantments.PROTECTION, 4, Enchantments.FIRE_ASPECT, 3, MobEffects.ABSORPTION, 0),
    CATACLYSM(RuneFamily.EMBER, 10, Enchantments.FIRE_PROTECTION, 6, Enchantments.SHARPNESS, 6, MobEffects.DAMAGE_BOOST, 1),

    TIDE(RuneFamily.TIDE, 1, Enchantments.RESPIRATION, 1, Enchantments.EFFICIENCY, 2, MobEffects.WATER_BREATHING, 0),
    DEW(RuneFamily.TIDE, 2, Enchantments.PROJECTILE_PROTECTION, 1, Enchantments.SILK_TOUCH, 1, MobEffects.REGENERATION, 0),
    CURRENT(RuneFamily.TIDE, 3, Enchantments.DEPTH_STRIDER, 2, Enchantments.EFFICIENCY, 3, MobEffects.DOLPHINS_GRACE, 0),
    WAVE(RuneFamily.TIDE, 4, Enchantments.PROJECTILE_PROTECTION, 2, Enchantments.KNOCKBACK, 2, MobEffects.MOVEMENT_SPEED, 0),
    CORAL(RuneFamily.TIDE, 5, Enchantments.AQUA_AFFINITY, 1, Enchantments.UNBREAKING, 3, MobEffects.WATER_BREATHING, 0),
    FROST(RuneFamily.TIDE, 6, Enchantments.FROST_WALKER, 2, Enchantments.SILK_TOUCH, 1, MobEffects.SLOW_FALLING, 0),
    TEMPEST(RuneFamily.TIDE, 7, Enchantments.BLAST_PROTECTION, 4, Enchantments.CHANNELING, 1, MobEffects.MOVEMENT_SPEED, 0),
    LEVIATHAN(RuneFamily.TIDE, 8, Enchantments.THORNS, 3, Enchantments.IMPALING, 5, MobEffects.DAMAGE_RESISTANCE, 0),
    OCEAN(RuneFamily.TIDE, 9, Enchantments.RESPIRATION, 4, Enchantments.LUCK_OF_THE_SEA, 4, MobEffects.CONDUIT_POWER, 0),
    MAELSTROM(RuneFamily.TIDE, 10, Enchantments.PROTECTION, 6, Enchantments.RIPTIDE, 5, MobEffects.DOLPHINS_GRACE, 1),

    VERDANT(RuneFamily.VERDANT, 1, Enchantments.PROTECTION, 1, Enchantments.UNBREAKING, 1, MobEffects.REGENERATION, 0),
    SPROUT(RuneFamily.VERDANT, 2, Enchantments.PROJECTILE_PROTECTION, 1, Enchantments.FORTUNE, 1, MobEffects.LUCK, 0),
    ROOT(RuneFamily.VERDANT, 3, Enchantments.BLAST_PROTECTION, 2, Enchantments.EFFICIENCY, 2, MobEffects.DAMAGE_RESISTANCE, 0),
    VINE(RuneFamily.VERDANT, 4, Enchantments.FEATHER_FALLING, 3, Enchantments.UNBREAKING, 2, MobEffects.MOVEMENT_SPEED, 0),
    THORN(RuneFamily.VERDANT, 5, Enchantments.THORNS, 2, Enchantments.SHARPNESS, 2, MobEffects.DAMAGE_BOOST, 0),
    BLOOM(RuneFamily.VERDANT, 6, Enchantments.PROTECTION, 3, Enchantments.MENDING, 1, MobEffects.REGENERATION, 0),
    GROVE(RuneFamily.VERDANT, 7, Enchantments.PROJECTILE_PROTECTION, 4, Enchantments.SILK_TOUCH, 1, MobEffects.DIG_SPEED, 0),
    ANCIENT_OAK(RuneFamily.VERDANT, 8, Enchantments.PROTECTION, 4, Enchantments.EFFICIENCY, 5, MobEffects.HEALTH_BOOST, 0),
    GAIA(RuneFamily.VERDANT, 9, Enchantments.THORNS, 4, Enchantments.FORTUNE, 4, MobEffects.ABSORPTION, 0),
    WORLD_TREE(RuneFamily.VERDANT, 10, Enchantments.PROTECTION, 6, Enchantments.UNBREAKING, 6, MobEffects.REGENERATION, 1),

    VOID(RuneFamily.VOID, 1, Enchantments.THORNS, 1, Enchantments.SHARPNESS, 2, MobEffects.NIGHT_VISION, 0),
    SHADE(RuneFamily.VOID, 2, Enchantments.PROJECTILE_PROTECTION, 2, Enchantments.BANE_OF_ARTHROPODS, 2, MobEffects.INVISIBILITY, 0),
    ECHO(RuneFamily.VOID, 3, Enchantments.PROTECTION, 2, Enchantments.KNOCKBACK, 2, MobEffects.MOVEMENT_SPEED, 0),
    DUSK(RuneFamily.VOID, 4, Enchantments.FEATHER_FALLING, 4, Enchantments.LOOTING, 3, MobEffects.NIGHT_VISION, 0),
    NIGHT(RuneFamily.VOID, 5, Enchantments.PROTECTION, 4, Enchantments.SMITE, 4, MobEffects.INVISIBILITY, 0),
    ECLIPSE(RuneFamily.VOID, 6, Enchantments.FIRE_PROTECTION, 4, Enchantments.SWEEPING_EDGE, 3, MobEffects.ABSORPTION, 0),
    RIFT(RuneFamily.VOID, 7, Enchantments.BLAST_PROTECTION, 4, Enchantments.BREACH, 4, MobEffects.SLOW_FALLING, 0),
    OBLIVION(RuneFamily.VOID, 8, Enchantments.THORNS, 4, Enchantments.DENSITY, 5, MobEffects.DAMAGE_RESISTANCE, 0),
    COSMOS(RuneFamily.VOID, 9, Enchantments.PROTECTION, 5, Enchantments.FORTUNE, 5, MobEffects.NIGHT_VISION, 0),
    SINGULARITY(RuneFamily.VOID, 10, Enchantments.PROTECTION, 7, Enchantments.SHARPNESS, 7, MobEffects.DAMAGE_BOOST, 1);

    private final RuneFamily family;
    private final int tier;
    private final ResourceKey<Enchantment> armorEnchantment;
    private final int armorLevel;
    private final ResourceKey<Enchantment> toolEnchantment;
    private final int toolLevel;
    private final Holder<MobEffect> passiveEffect;
    private final int passiveAmplifier;

    RuneType(RuneFamily family, int tier, ResourceKey<Enchantment> armorEnchantment, int armorLevel,
             ResourceKey<Enchantment> toolEnchantment, int toolLevel, Holder<MobEffect> passiveEffect,
             int passiveAmplifier) {
        this.family = family;
        this.tier = tier;
        this.armorEnchantment = armorEnchantment;
        this.armorLevel = armorLevel;
        this.toolEnchantment = toolEnchantment;
        this.toolLevel = toolLevel;
        this.passiveEffect = passiveEffect;
        this.passiveAmplifier = passiveAmplifier;
    }

    public RuneFamily family() {
        return family;
    }

    public int tier() {
        return tier;
    }

    public int color() {
        return family.color();
    }

    public double passingDistance() {
        return tier == 10 ? 18.0 : 38.0 - tier * 1.5;
    }

    public ResourceKey<Enchantment> armorEnchantment() {
        return armorEnchantment;
    }

    public int armorLevel() {
        return armorLevel;
    }

    public ResourceKey<Enchantment> toolEnchantment() {
        return toolEnchantment;
    }

    public int toolLevel() {
        return toolLevel;
    }

    public Holder<MobEffect> passiveEffect() {
        return passiveEffect;
    }

    public int passiveAmplifier() {
        return passiveAmplifier;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static RuneType byOrdinal(int ordinal) {
        RuneType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : EMBER;
    }

    public static List<RuneType> forFamily(RuneFamily family) {
        return Arrays.stream(values()).filter(type -> type.family == family).toList();
    }

    public RuneType relative(int offset) {
        List<RuneType> familyRunes = forFamily(family);
        return familyRunes.get(Math.floorMod(familyRunes.indexOf(this) + offset, familyRunes.size()));
    }

    public Supplier<? extends Item> runeItem() {
        return ModItems.runeItem(this);
    }
}
