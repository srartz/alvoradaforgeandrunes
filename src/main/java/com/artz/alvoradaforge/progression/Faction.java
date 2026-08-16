package com.artz.alvoradaforge.progression;

import java.util.Locale;

public enum Faction {
    DWARVES,
    KOBOLDS;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Faction fromName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
