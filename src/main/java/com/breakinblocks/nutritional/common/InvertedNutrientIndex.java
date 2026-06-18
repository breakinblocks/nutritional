package com.breakinblocks.nutritional.common;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InvertedNutrientIndex {

    private static volatile Map<Item, List<Identifier>> index = Map.of();

    private InvertedNutrientIndex() {}

    public static void rebuild(RegistryAccess access) {
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(access);
        Map<Item, List<Identifier>> built = new HashMap<>();

        for (Holder.Reference<NutrientDefinition> ref : nutrients.listElements().toList()) {
            Identifier nutrientId = ref.key().identifier();
            NutrientDefinition def = ref.value();
            TagKey<Item> tag = def.items().orElseGet(() -> defaultTagFor(nutrientId));
            BuiltInRegistries.ITEM.get(tag).ifPresent(holderSet ->
                    holderSet.forEach(itemHolder ->
                            built.computeIfAbsent(itemHolder.value(), k -> new ArrayList<>()).add(nutrientId)));
        }

        index = Map.copyOf(built);
        Nutritional.LOGGER.info("Inverted nutrient index built: {} item -> nutrient mappings.", index.size());
    }

    public static List<Identifier> nutrientsFor(Item item) {
        return index.getOrDefault(item, List.of());
    }

    public static boolean isEmpty() {
        return index.isEmpty();
    }

    public static Set<Item> items() {
        return index.keySet();
    }

    private static TagKey<Item> defaultTagFor(Identifier nutrientId) {
        String path = nutrientId.getNamespace().equals(Nutritional.MOD_ID)
                ? "nutrient/" + nutrientId.getPath()
                : "nutrient/" + nutrientId.getNamespace() + "/" + nutrientId.getPath();
        return TagKey.create(Registries.ITEM, Nutritional.id(path));
    }
}
