package com.artz.alvoradaforge.progression;

import java.util.Locale;

public enum Knowledge {
    DWARVEN_TEMPERING(Faction.DWARVES, 10),
    DWARVEN_MASTERWORK(Faction.DWARVES, 50),
    KOBOLD_ADVANCED_RUNES(Faction.KOBOLDS, 10),
    KOBOLD_LEGENDARY_RUNES(Faction.KOBOLDS, 50);

    private final Faction faction;
    private final int requiredReputation;

    Knowledge(Faction faction, int requiredReputation) {
        this.faction = faction;
        this.requiredReputation = requiredReputation;
    }

    public Faction faction() {
        return faction;
    }

    public int requiredReputation() {
        return requiredReputation;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Knowledge fromName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
