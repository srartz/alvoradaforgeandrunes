package com.artz.alvoradaforge.progression;

import com.artz.alvoradaforge.AlvoradaForge;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/** Persistent progression shared by commands, quest NPCs, forging and runes. */
public final class PlayerProgression {
    private static final String ROOT_KEY = AlvoradaForge.MOD_ID + ":progression";
    private static final String REPUTATION_KEY = "Reputation";
    private static final String KNOWLEDGE_KEY = "Knowledge";

    private PlayerProgression() {
    }

    public static int reputation(Player player, Faction faction) {
        return data(player).getCompound(REPUTATION_KEY).getInt(faction.serializedName());
    }

    public static int addReputation(Player player, Faction faction, int amount) {
        CompoundTag data = data(player);
        CompoundTag reputations = data.getCompound(REPUTATION_KEY);
        int updated = Math.max(0, reputation(player, faction) + amount);
        reputations.putInt(faction.serializedName(), updated);
        data.put(REPUTATION_KEY, reputations);
        return updated;
    }

    public static boolean knows(Player player, Knowledge knowledge) {
        return knowledge(player).contains(knowledge);
    }

    public static boolean grant(Player player, Knowledge knowledge) {
        if (knows(player, knowledge)) {
            return false;
        }
        CompoundTag data = data(player);
        ListTag list = data.getList(KNOWLEDGE_KEY, Tag.TAG_STRING);
        list.add(StringTag.valueOf(knowledge.serializedName()));
        data.put(KNOWLEDGE_KEY, list);
        return true;
    }

    public static boolean revoke(Player player, Knowledge knowledge) {
        CompoundTag data = data(player);
        ListTag list = data.getList(KNOWLEDGE_KEY, Tag.TAG_STRING);
        boolean removed = list.removeIf(tag -> tag.getAsString().equals(knowledge.serializedName()));
        data.put(KNOWLEDGE_KEY, list);
        return removed;
    }

    public static Set<Knowledge> knowledge(Player player) {
        EnumSet<Knowledge> result = EnumSet.noneOf(Knowledge.class);
        ListTag list = data(player).getList(KNOWLEDGE_KEY, Tag.TAG_STRING);
        for (Tag tag : list) {
            try {
                result.add(Knowledge.fromName(tag.getAsString()));
            } catch (IllegalArgumentException ignored) {
                // Unknown entries are retained for forward compatibility.
            }
        }
        return result;
    }

    public static boolean canLearn(Player player, Knowledge knowledge) {
        return reputation(player, knowledge.faction()) >= knowledge.requiredReputation();
    }

    public static boolean canDrawRune(Player player, int tier) {
        if (tier <= 7) {
            return true;
        }
        if (tier < 10) {
            return knows(player, Knowledge.KOBOLD_ADVANCED_RUNES);
        }
        return knows(player, Knowledge.KOBOLD_LEGENDARY_RUNES);
    }

    private static CompoundTag data(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag progression = persisted.getCompound(ROOT_KEY);
        persisted.put(ROOT_KEY, progression);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return progression;
    }
}
