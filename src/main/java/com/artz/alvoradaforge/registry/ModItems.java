package com.artz.alvoradaforge.registry;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.item.ForgeHammerItem;
import com.artz.alvoradaforge.item.RuneInkItem;
import com.artz.alvoradaforge.item.RuneItem;
import com.artz.alvoradaforge.item.KnowledgeManualItem;
import com.artz.alvoradaforge.progression.Knowledge;
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
    public static final DeferredItem<BlockItem> ANCESTRAL_RUNE_TABLE = ITEMS.registerSimpleBlockItem(ModBlocks.ANCESTRAL_RUNE_TABLE);
    public static final DeferredItem<BlockItem> MYSTERY_RUNE_STONE = ITEMS.registerSimpleBlockItem(ModBlocks.MYSTERY_RUNE_STONE);
    public static final DeferredItem<BlockItem> RUNE_BREAKER_TABLE = ITEMS.registerSimpleBlockItem(ModBlocks.RUNE_BREAKER_TABLE);
    public static final DeferredItem<BlockItem> LAPIDARY_SAW = ITEMS.registerSimpleBlockItem(ModBlocks.LAPIDARY_SAW);

    public static final DeferredItem<Item> RAW_LEY_STONE = ITEMS.registerSimpleItem("raw_ley_stone", new Item.Properties());
    public static final DeferredItem<Item> ANCIENT_GEODE_HEART = ITEMS.registerSimpleItem(
            "ancient_geode_heart", new Item.Properties().stacksTo(16).fireResistant());
    public static final DeferredItem<Item> QUARTZ_DUST = ITEMS.registerSimpleItem("quartz_dust", new Item.Properties());
    public static final DeferredItem<Item> DIAMOND_DUST = ITEMS.registerSimpleItem("diamond_dust", new Item.Properties());
    public static final DeferredItem<Item> IRON_SAW_BLADE = ITEMS.registerSimpleItem(
            "iron_saw_blade", new Item.Properties().durability(64));
    public static final DeferredItem<Item> DIAMOND_SAW_BLADE = ITEMS.registerSimpleItem(
            "diamond_saw_blade", new Item.Properties().durability(256));
    public static final DeferredItem<Item> RUNE_STONE = ITEMS.registerSimpleItem("rune_stone", new Item.Properties());
    public static final DeferredItem<Item> ANCESTRAL_RUNE_STONE = ITEMS.registerSimpleItem("ancestral_rune_stone", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> ANCESTRAL_FEATHER = ITEMS.registerSimpleItem("ancestral_feather", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> MYTHRIL_STEEL_INGOT = ITEMS.registerSimpleItem("mythril_steel_ingot", new Item.Properties());
    public static final DeferredItem<Item> REINFORCED_HAMMER_HANDLE = ITEMS.registerSimpleItem("reinforced_hammer_handle", new Item.Properties());
    public static final DeferredItem<RuneInkItem> EMBER_INK = registerInk("ember_ink", RuneFamily.EMBER, 7);
    public static final DeferredItem<RuneInkItem> TIDE_INK = registerInk("tide_ink", RuneFamily.TIDE, 7);
    public static final DeferredItem<RuneInkItem> VERDANT_INK = registerInk("verdant_ink", RuneFamily.VERDANT, 7);
    public static final DeferredItem<RuneInkItem> VOID_INK = registerInk("void_ink", RuneFamily.VOID, 7);
    public static final DeferredItem<RuneInkItem> ANCESTRAL_EMBER_INK = registerInk("ancestral_ember_ink", RuneFamily.EMBER, 10);
    public static final DeferredItem<RuneInkItem> ANCESTRAL_TIDE_INK = registerInk("ancestral_tide_ink", RuneFamily.TIDE, 10);
    public static final DeferredItem<RuneInkItem> ANCESTRAL_VERDANT_INK = registerInk("ancestral_verdant_ink", RuneFamily.VERDANT, 10);
    public static final DeferredItem<RuneInkItem> ANCESTRAL_VOID_INK = registerInk("ancestral_void_ink", RuneFamily.VOID, 10);
    public static final DeferredItem<KnowledgeManualItem> DWARVEN_TEMPERING_MANUAL = registerManual("dwarven_tempering_manual", Knowledge.DWARVEN_TEMPERING);
    public static final DeferredItem<KnowledgeManualItem> DWARVEN_MASTERWORK_MANUAL = registerManual("dwarven_masterwork_manual", Knowledge.DWARVEN_MASTERWORK);
    public static final DeferredItem<KnowledgeManualItem> KOBOLD_ADVANCED_RUNES_MANUAL = registerManual("kobold_advanced_runes_manual", Knowledge.KOBOLD_ADVANCED_RUNES);
    public static final DeferredItem<KnowledgeManualItem> KOBOLD_LEGENDARY_RUNES_MANUAL = registerManual("kobold_legendary_runes_manual", Knowledge.KOBOLD_LEGENDARY_RUNES);
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

    private static DeferredItem<RuneInkItem> registerInk(String name, RuneFamily family, int maxTier) {
        return ITEMS.register(name, () -> new RuneInkItem(new Item.Properties().stacksTo(16), family, maxTier));
    }

    private static DeferredItem<KnowledgeManualItem> registerManual(String name, Knowledge knowledge) {
        return ITEMS.register(name, () -> new KnowledgeManualItem(new Item.Properties().stacksTo(1), knowledge));
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
