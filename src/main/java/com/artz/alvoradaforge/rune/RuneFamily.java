package com.artz.alvoradaforge.rune;

import java.util.List;

public enum RuneFamily {
    EMBER(0xFFF04A32),
    TIDE(0xFF2BB9F2),
    VERDANT(0xFF65D94E),
    VOID(0xFFB45CFF);

    private final int color;

    RuneFamily(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }

    public List<RuneType> runes() {
        return RuneType.forFamily(this);
    }

    public RuneType firstRune() {
        return runes().getFirst();
    }
}
