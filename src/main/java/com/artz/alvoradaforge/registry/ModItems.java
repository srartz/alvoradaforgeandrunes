package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.item.ForgeHammerItem;
import com.artz.alvoradaforge.item.RuneInkItem;
import com.artz.alvoradaforge.item.RuneItem;
import com.artz.alvoradaforge.rune.RuneFamily;
import com.artz.alvoradaforge.rune.RuneType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AlvoradaForge.MOD_ID);
    private static final Map<RuneType, DeferredItem<RuneItem>> RUNE_ITEMS = new EnumMap<>(RuneType.class);

    public static final DeferredItem<BlockItem> FORGING_ANVIL = ITEMS.registerSimpleBlockItem(ModBlocks.FORGING_ANVIL);
    public static final DeferredItem<BlockItem> RUNE_TABLE = ITEMS.registerSimpleBlockItem(ModBlocks.RUNE_TABLE);

    public static final DeferredItem<Item> RUNE_STONE = ITEMS.registerSimpleItem("rune_stone", new Item.Properties());
    public static final DeferredItem<RuneInkItem> EMBER_INK = registerInk("ember_ink", RuneFamily.EMBER);
    public static final DeferredItem<RuneInkItem> TIDE_INK = registerInk("tide_ink", RuneFamily.TIDE);
    public static final DeferredItem<RuneInkItem> VERDANT_INK = registerInk("verdant_ink", RuneFamily.VERDANT);
    public static final DeferredItem<RuneInkItem> VOID_INK = registerInk("void_ink", RuneFamily.VOID);
    public static final DeferredItem<RuneItem> EMBER_RUNE = registerRune(RuneType.EMBER);
    public static final DeferredItem<RuneItem> TIDE_RUNE = registerRune(RuneType.TIDE);
    public static final DeferredItem<RuneItem> VERDANT_RUNE = registerRune(RuneType.VERDANT);
    public static final DeferredItem<RuneItem> VOID_RUNE = registerRune(RuneType.VOID);

    static {
        for (RuneType type : RuneType.values()) {
            if (!RUNE_ITEMS.containsKey(type)) {
                registerRune(type);
            }
        }
    }

    public static final DeferredItem<ForgeHammerItem> COPPER_HAMMER = registerHammer("copper_hammer", 160, 0.00F, 0.82F, 1, 4.4F, 1.00F, false);
    public static final DeferredItem<ForgeHammerItem> IRON_HAMMER = registerHammer("iron_hammer", 384, 0.02F, 0.88F, 1, 4.0F, 0.75F, false);
    public static final DeferredItem<ForgeHammerItem> GOLD_HAMMER = registerHammer("gold_hammer", 96, 0.04F, 0.94F, 1, 3.6F, 0.50F, false);
    public static final DeferredItem<ForgeHammerItem> DIAMOND_HAMMER = registerHammer("diamond_hammer", 1024, 0.06F, 1.02F, 2, 3.2F, 0.25F, false);
    public static final DeferredItem<ForgeHammerItem> NETHERITE_HAMMER = registerHammer("netherite_hammer", 2048, 0.08F, 1.12F, 2, 2.8F, 0.05F, true);

    private ModItems() {
    }

    private static DeferredItem<RuneInkItem> registerInk(String name, RuneFamily family) {
        return ITEMS.register(name, () -> new RuneInkItem(new Item.Properties().stacksTo(16), family));
    }

    private static DeferredItem<RuneItem> registerRune(RuneType type) {
        DeferredItem<RuneItem> item = ITEMS.register(type.serializedName() + "_rune",
                () -> new RuneItem(new Item.Properties().stacksTo(16), type));
        RUNE_ITEMS.put(type, item);
        return item;
    }

    public static DeferredItem<RuneItem> runeItem(RuneType type) {
        return RUNE_ITEMS.get(type);
    }

    private static DeferredItem<ForgeHammerItem> registerHammer(
            String name,
            int durability,
            float precision,
            float control,
            int power,
            float speedupPerHit,
            float targetRelocationChance,
            boolean fireResistant
    ) {
        Item.Properties properties = new Item.Properties().durability(durability);
        if (fireResistant) {
            properties.fireResistant();
        }
        return ITEMS.register(name, () -> new ForgeHammerItem(
                properties, precision, control, power, speedupPerHit, targetRelocationChance));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
