package com.artz.alvoradaforge.forging;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.config.ForgeItemConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.artz.alvoradaforge.progression.Faction;
import com.artz.alvoradaforge.progression.Knowledge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class ForgeRecipeManager extends SimpleJsonResourceReloadListener {
    public static final ForgeRecipeManager INSTANCE = new ForgeRecipeManager();
    private static final String DIRECTORY = "alvorada_forge";

    private volatile List<ForgeRecipe> recipes = List.of();

    private ForgeRecipeManager() {
        super(new Gson(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        List<ForgeRecipe> loaded = new ArrayList<>();
        int skipped = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            try {
                loaded.add(parse(entry.getKey(), entry.getValue().getAsJsonObject()));
            } catch (RuntimeException exception) {
                skipped++;
                AlvoradaForge.LOGGER.warn("Receita de forja {} ignorada: {}", entry.getKey(), exception.getMessage());
            }
        }

        loaded.addAll(ForgeItemConfig.loadRecipes());

        loaded.sort(Comparator.comparingInt(ForgeRecipe::priority).reversed().thenComparing(recipe -> recipe.id().toString()));
        recipes = List.copyOf(loaded);
        AlvoradaForge.LOGGER.info("Carregadas {} receitas de forja ({} ignoradas)", loaded.size(), skipped);
    }

    public Optional<ForgeRecipe> find(ItemStack left, ItemStack right) {
        return recipes.stream().filter(recipe -> recipe.matches(left, right)).findFirst();
    }

    public Optional<ForgeRecipe> findExact(ItemStack left, ItemStack right) {
        return recipes.stream()
                .filter(recipe -> recipe.matches(left, right))
                .filter(recipe -> recipe.inputs().size() == 1 || right.getCount() == recipe.materialCost())
                .findFirst();
    }

    public Optional<ForgeRecipe> byId(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            return Optional.empty();
        }
        return recipes.stream().filter(recipe -> recipe.id().equals(parsed)).findFirst();
    }

    public List<ForgeRecipe> recipes() {
        return recipes;
    }

    public static ForgeRecipe parse(ResourceLocation id, JsonObject json) {
        JsonArray inputArray = GsonHelper.getAsJsonArray(json, "inputs");
        if (inputArray.isEmpty() || inputArray.size() > 2) {
            throw new JsonSyntaxException("A bigorna vanilla exige uma ou duas entradas");
        }

        List<ForgeRecipe.Input> inputs = new ArrayList<>();
        for (JsonElement element : inputArray) {
            inputs.add(parseInput(element.getAsJsonObject()));
        }
        if (inputs.getFirst().count() != 1) {
            throw new JsonSyntaxException("A primeira entrada deve ter count 1 enquanto a bigorna vanilla estiver em uso");
        }

        JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation resultId = ResourceLocation.parse(GsonHelper.getAsString(resultJson, "id"));
        Item resultItem = BuiltInRegistries.ITEM.getOptional(resultId)
                .orElseThrow(() -> new JsonSyntaxException("Item de resultado ausente: " + resultId));
        int resultCount = GsonHelper.getAsInt(resultJson, "count", 1);
        ItemStack resultStack = new ItemStack(resultItem, resultCount);
        if (resultCount < 1 || resultCount > resultStack.getMaxStackSize()) {
            throw new JsonSyntaxException("Quantidade de resultado invalida: " + resultCount);
        }

        int experienceCost = Math.max(1, GsonHelper.getAsInt(json, "experience_cost", 1));
        ForgeQuality quality = ForgeQuality.fromName(GsonHelper.getAsString(json, "quality", "well"));
        ForgedStats stats = parseStats(json, quality);
        int benefits = json.has("benefits")
                ? ForgeBenefits.parse(GsonHelper.getAsJsonArray(json, "benefits"), resultStack)
                : ForgeBenefits.automatic(resultStack);
        boolean copyComponents = GsonHelper.getAsBoolean(json, "copy_input_components", false);
        float damageChance = GsonHelper.getAsFloat(json, "anvil_damage_chance", 0.12F);
        if (damageChance < 0.0F || damageChance > 1.0F) {
            throw new JsonSyntaxException("anvil_damage_chance deve estar entre 0 e 1");
        }
        int requiredHits = GsonHelper.getAsInt(json, "hammering", 5);
        int cycleTicks = GsonHelper.getAsInt(json, "cycle_ticks", 36);
        if (requiredHits < 1 || requiredHits > 100) {
            throw new JsonSyntaxException("hammering deve estar entre 1 e 100");
        }
        if (cycleTicks < 12 || cycleTicks > 200) {
            throw new JsonSyntaxException("cycle_ticks deve estar entre 12 e 200");
        }
        int priority = GsonHelper.getAsInt(json, "priority", 0);
        Knowledge requiredKnowledge = null;
        Faction requiredFaction = null;
        int requiredReputation = 0;
        if (json.has("requirements")) {
            JsonObject requirements = GsonHelper.getAsJsonObject(json, "requirements");
            if (requirements.has("knowledge")) {
                requiredKnowledge = Knowledge.fromName(GsonHelper.getAsString(requirements, "knowledge"));
            }
            if (requirements.has("faction")) {
                requiredFaction = Faction.fromName(GsonHelper.getAsString(requirements, "faction"));
                requiredReputation = Math.max(0, GsonHelper.getAsInt(requirements, "reputation", 0));
            }
        }

        return new ForgeRecipe(
                id,
                List.copyOf(inputs),
                resultItem,
                resultCount,
                experienceCost,
                quality,
                stats,
                benefits,
                copyComponents,
                damageChance,
                requiredHits,
                cycleTicks,
                json.has("bonuses"),
                priority,
                requiredKnowledge,
                requiredFaction,
                requiredReputation
        );
    }

    private static ForgeRecipe.Input parseInput(JsonObject json) {
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count < 1) {
            throw new JsonSyntaxException("Quantidade de entrada deve ser positiva");
        }

        JsonElement ingredientJson;
        if (json.has("ingredient")) {
            ingredientJson = json.get("ingredient");
        } else {
            JsonObject copy = json.deepCopy();
            copy.remove("count");
            ingredientJson = copy;
        }

        validateReferencedItems(ingredientJson);
        Ingredient ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, ingredientJson)
                .getOrThrow(JsonSyntaxException::new);
        if (ingredient.hasNoItems()) {
            throw new JsonSyntaxException("Ingrediente vazio ou pertencente a um mod ausente");
        }
        return new ForgeRecipe.Input(ingredient, count);
    }

    private static void validateReferencedItems(JsonElement element) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(ForgeRecipeManager::validateReferencedItems);
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("item")) {
            ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(object, "item"));
            if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                throw new JsonSyntaxException("Item de entrada ausente: " + itemId);
            }
        }
        object.entrySet().forEach(entry -> validateReferencedItems(entry.getValue()));
    }

    private static ForgedStats parseStats(JsonObject json, ForgeQuality quality) {
        ForgedStats defaults = ForgedStats.defaults(quality);
        if (!json.has("bonuses")) {
            return defaults;
        }
        JsonObject bonuses = GsonHelper.getAsJsonObject(json, "bonuses");
        float durability = GsonHelper.getAsFloat(bonuses, "durability_multiplier", defaults.durabilityMultiplier());
        float mining = GsonHelper.getAsFloat(bonuses, "mining_speed_multiplier", defaults.miningSpeedMultiplier());
        if (durability < 0.01F || mining < 0.01F) {
            throw new JsonSyntaxException("Multiplicadores de durabilidade e mineracao devem ser maiores que zero");
        }
        return new ForgedStats(
                durability,
                mining,
                GsonHelper.getAsDouble(bonuses, "attack_damage", defaults.attackDamage()),
                GsonHelper.getAsDouble(bonuses, "attack_speed", defaults.attackSpeed()),
                GsonHelper.getAsDouble(bonuses, "armor", defaults.armor()),
                GsonHelper.getAsDouble(bonuses, "armor_toughness", defaults.armorToughness())
        );
    }
}
