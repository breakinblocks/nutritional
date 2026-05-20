package com.breakinblocks.nutritional.userpack;

import com.breakinblocks.nutritional.Nutritional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class UserPackWriter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private UserPackWriter() {}

    public static void addItemToNutrientTag(ResourceLocation nutrientId, ResourceLocation itemId) throws IOException {
        Path file = UserPackPaths.tagFile(nutrientId);
        Files.createDirectories(file.getParent());

        JsonObject root = readOrEmpty(file);
        root.addProperty("replace", false);
        JsonArray values = root.has("values") && root.get("values").isJsonArray()
                ? root.getAsJsonArray("values")
                : new JsonArray();

        if (containsItem(values, itemId)) {
            root.add("values", values);
            writeAtomic(file, root);
            return;
        }

        if (ModConditional.isForeign(itemId)) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", itemId.toString());
            entry.addProperty("required", false);
            values.add(entry);
        } else {
            values.add(itemId.toString());
        }

        root.add("values", values);
        writeAtomic(file, root);
    }

    public static void removeItemFromNutrientTag(ResourceLocation nutrientId, ResourceLocation itemId) throws IOException {
        Path file = UserPackPaths.tagFile(nutrientId);
        if (!Files.exists(file)) return;

        JsonObject root = readOrEmpty(file);
        if (!root.has("values") || !root.get("values").isJsonArray()) return;
        JsonArray values = root.getAsJsonArray("values");

        JsonArray filtered = new JsonArray();
        boolean removed = false;
        for (JsonElement element : values) {
            if (matchesItem(element, itemId)) {
                removed = true;
                continue;
            }
            filtered.add(element);
        }
        if (!removed) return;

        root.add("values", filtered);
        writeAtomic(file, root);
    }

    public static void setNutrientScale(ResourceLocation itemId, ResourceLocation nutrientId, float scale) throws IOException {
        modifyScales(itemId, scales -> scales.put(nutrientId, scale));
    }

    public static void removeNutrientScale(ResourceLocation itemId, ResourceLocation nutrientId) throws IOException {
        modifyScales(itemId, scales -> scales.remove(nutrientId));
    }

    private static void modifyScales(ResourceLocation itemId, ScaleEditor editor) throws IOException {
        Path file = UserPackPaths.scalesFile();
        Files.createDirectories(file.getParent());

        JsonObject root = readOrEmpty(file);
        root.addProperty("replace", false);
        JsonObject values = root.has("values") && root.get("values").isJsonObject()
                ? root.getAsJsonObject("values")
                : new JsonObject();

        String itemKey = itemId.toString();
        JsonObject entry = values.has(itemKey) && values.get(itemKey).isJsonObject()
                ? values.getAsJsonObject(itemKey)
                : new JsonObject();

        JsonObject valueObject = entry.has("value") && entry.get("value").isJsonObject()
                ? entry.getAsJsonObject("value")
                : extractInlineValue(entry);

        JsonObject scalesObject = valueObject.has("scales") && valueObject.get("scales").isJsonObject()
                ? valueObject.getAsJsonObject("scales")
                : new JsonObject();

        Map<ResourceLocation, Float> scaleMap = readScales(scalesObject);
        editor.apply(scaleMap);

        if (scaleMap.isEmpty()) {
            values.remove(itemKey);
        } else {
            JsonObject newScales = new JsonObject();
            for (Map.Entry<ResourceLocation, Float> e : scaleMap.entrySet()) {
                newScales.addProperty(e.getKey().toString(), e.getValue());
            }
            JsonObject newValue = new JsonObject();
            newValue.add("scales", newScales);

            JsonObject newEntry = new JsonObject();
            if (ModConditional.isForeign(itemId)) {
                newEntry.add("value", newValue);
                newEntry.add("conditions", ModConditional.modLoadedConditions(itemId));
            } else {
                copyInto(newEntry, newValue);
            }
            values.add(itemKey, newEntry);
        }

        root.add("values", values);
        writeAtomic(file, root);
    }

    private static JsonObject extractInlineValue(JsonObject entry) {
        JsonObject inline = new JsonObject();
        for (Map.Entry<String, JsonElement> e : entry.entrySet()) {
            if (!e.getKey().equals("conditions")) inline.add(e.getKey(), e.getValue());
        }
        return inline;
    }

    private static Map<ResourceLocation, Float> readScales(JsonObject scales) {
        Map<ResourceLocation, Float> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : scales.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(e.getKey());
            if (id != null && e.getValue().isJsonPrimitive()) {
                result.put(id, e.getValue().getAsFloat());
            }
        }
        return result;
    }

    private static void copyInto(JsonObject target, JsonObject source) {
        for (Map.Entry<String, JsonElement> e : source.entrySet()) {
            target.add(e.getKey(), e.getValue());
        }
    }

    private static boolean containsItem(JsonArray values, ResourceLocation itemId) {
        for (JsonElement element : values) {
            if (matchesItem(element, itemId)) return true;
        }
        return false;
    }

    private static boolean matchesItem(JsonElement element, ResourceLocation itemId) {
        String idString = itemId.toString();
        if (element.isJsonPrimitive() && element.getAsString().equals(idString)) return true;
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            return obj.has("id") && obj.get("id").getAsString().equals(idString);
        }
        return false;
    }

    private static JsonObject readOrEmpty(Path file) throws IOException {
        if (!Files.exists(file)) return new JsonObject();
        String text = Files.readString(file);
        if (text.isBlank()) return new JsonObject();
        JsonElement parsed = JsonParser.parseString(text);
        return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
    }

    private static void writeAtomic(Path target, JsonObject content) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(tmp, GSON.toJson(content));
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        Nutritional.LOGGER.info("UserPackWriter wrote {}", target.toAbsolutePath());
    }

    @FunctionalInterface
    private interface ScaleEditor {
        void apply(Map<ResourceLocation, Float> scales);
    }
}
