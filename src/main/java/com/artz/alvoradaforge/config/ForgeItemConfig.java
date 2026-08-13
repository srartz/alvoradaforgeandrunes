package com.artz.alvoradaforge.config;

import com.artz.alvoradaforge.AlvoradaForge;
import com.artz.alvoradaforge.forging.ForgeRecipe;
import com.artz.alvoradaforge.forging.ForgeRecipeManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Configuracao externa voltada a donos de servidor e modpacks. Ela e mantida
 * separada dos datapacks para que IDs de qualquer mod possam ser habilitados
 * sem reconstruir o jar.
 */
public final class ForgeItemConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIRECTORY = FMLPaths.CONFIGDIR.get().resolve(AlvoradaForge.MOD_ID);
    private static final Path FILE = DIRECTORY.resolve("forgeable_items.json");

    private ForgeItemConfig() {
    }

    public static void ensureCreated() {
        if (Files.exists(FILE)) {
            return;
        }
        try {
            Files.createDirectories(DIRECTORY);
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(defaultConfig(), writer);
            }
            AlvoradaForge.LOGGER.info("Configuracao de itens forjaveis criada em {}", FILE);
        } catch (IOException exception) {
            AlvoradaForge.LOGGER.error("Nao foi possivel criar {}", FILE, exception);
        }
    }

    public static List<ForgeRecipe> loadRecipes() {
        ensureCreated();
        if (!Files.isRegularFile(FILE)) {
            return List.of();
        }

        List<ForgeRecipe> loaded = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            JsonArray entries = root == null || !root.has("forgeable_items")
                    ? new JsonArray()
                    : root.getAsJsonArray("forgeable_items");
            int index = 0;
            for (JsonElement element : entries) {
                index++;
                try {
                    JsonObject entry = normalizeEntry(element);
                    if (!entry.has("enabled") || entry.get("enabled").getAsBoolean()) {
                        ResourceLocation recipeId = configRecipeId(entry, index);
                        loaded.add(ForgeRecipeManager.parse(recipeId, toRecipeJson(entry)));
                    }
                } catch (RuntimeException exception) {
                    AlvoradaForge.LOGGER.warn("Entrada {} de {} ignorada: {}", index, FILE, exception.getMessage());
                }
            }
        } catch (IOException | RuntimeException exception) {
            AlvoradaForge.LOGGER.error("Nao foi possivel ler {}", FILE, exception);
        }
        return List.copyOf(loaded);
    }

    private static JsonObject normalizeEntry(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            JsonObject shorthand = new JsonObject();
            shorthand.addProperty("item", element.getAsString());
            shorthand.addProperty("material", "minecraft:amethyst_shard");
            shorthand.addProperty("material_count", 2);
            return shorthand;
        }
        return element.getAsJsonObject();
    }

    public static Path file() {
        return FILE;
    }

    private static ResourceLocation configRecipeId(JsonObject entry, int index) {
        String itemId = entry.get("item").getAsString();
        ResourceLocation item = ResourceLocation.parse(itemId);
        String safePath = item.getNamespace() + "/" + item.getPath().replace('/', '_') + "_" + index;
        return AlvoradaForge.id("config/" + safePath);
    }

    private static JsonObject toRecipeJson(JsonObject entry) {
        String item = entry.get("item").getAsString();
        String material = entry.get("material").getAsString();

        JsonArray inputs = new JsonArray();
        inputs.add(input(item, 1));
        inputs.add(input(material, getInt(entry, "material_count", 1)));

        JsonObject result = new JsonObject();
        result.addProperty("id", entry.has("result") ? entry.get("result").getAsString() : item);
        result.addProperty("count", getInt(entry, "result_count", 1));

        JsonObject recipe = new JsonObject();
        recipe.add("inputs", inputs);
        recipe.add("result", result);
        recipe.addProperty("experience_cost", getInt(entry, "experience_cost", 3));
        recipe.addProperty("hammering", getInt(entry, "hammering", 5));
        recipe.addProperty("cycle_ticks", getInt(entry, "cycle_ticks", 36));
        recipe.addProperty("quality", entry.has("quality") ? entry.get("quality").getAsString() : "well");
        recipe.addProperty("copy_input_components", true);
        recipe.addProperty("anvil_damage_chance", entry.has("anvil_damage_chance")
                ? entry.get("anvil_damage_chance").getAsFloat() : 0.12F);
        recipe.addProperty("priority", getInt(entry, "priority", 100));
        if (entry.has("benefits")) {
            recipe.add("benefits", entry.get("benefits").deepCopy());
        }
        if (entry.has("bonuses")) {
            recipe.add("bonuses", entry.get("bonuses").deepCopy());
        }
        return recipe;
    }

    private static JsonObject input(String item, int count) {
        JsonObject input = new JsonObject();
        input.addProperty("item", item);
        input.addProperty("count", count);
        return input;
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static JsonObject defaultConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("format", 1);
        root.addProperty("reload_hint", "Edite este arquivo e execute /reload. IDs de mods ausentes sao ignorados.");

        JsonObject example = new JsonObject();
        example.addProperty("enabled", true);
        example.addProperty("item", "minecraft:iron_pickaxe");
        example.addProperty("material", "minecraft:iron_ingot");
        example.addProperty("material_count", 3);
        example.addProperty("experience_cost", 3);
        example.addProperty("hammering", 5);
        example.addProperty("cycle_ticks", 36);
        example.addProperty("quality", "well");
        JsonArray benefits = new JsonArray();
        benefits.add("durability");
        benefits.add("tool");
        example.add("benefits", benefits);

        JsonArray entries = new JsonArray();
        entries.add(example);
        root.add("forgeable_items", entries);
        return root;
    }
}
