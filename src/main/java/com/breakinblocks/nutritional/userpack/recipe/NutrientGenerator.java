package com.breakinblocks.nutritional.userpack.recipe;

import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class NutrientGenerator {

    private static final int MAX_DEPTH = 4;

    private NutrientGenerator() {}

    public static Optional<Map<ResourceLocation, Float>> derive(Item target, MinecraftServer server) {
        Map<ResourceLocation, Float> result = deriveInner(target, server, 0, new HashSet<>());
        if (result == null || result.isEmpty()) return Optional.empty();
        return Optional.of(result);
    }

    private static Map<ResourceLocation, Float> deriveInner(Item target, MinecraftServer server, int depth, Set<Item> inflight) {
        if (inflight.contains(target)) return null;
        if (depth >= MAX_DEPTH) return null;

        Map<ResourceLocation, Float> direct = nutrientScales(target);
        if (direct != null) return direct;

        RecipeHolder<?> recipe = findRecipeFor(target, server);
        if (recipe == null) return null;

        inflight.add(target);
        try {
            return computeFromRecipe(target, recipe, server, depth, inflight);
        } finally {
            inflight.remove(target);
        }
    }

    private static Map<ResourceLocation, Float> computeFromRecipe(Item target,
                                                                  RecipeHolder<?> recipeHolder,
                                                                  MinecraftServer server,
                                                                  int depth,
                                                                  Set<Item> inflight) {
        Recipe<?> recipe = recipeHolder.value();
        RegistryAccess access = server.registryAccess();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) return null;

        Map<ResourceLocation, Float> sumScale = new HashMap<>();
        int totalCount = 0;

        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;
            Map<ResourceLocation, Float> picked = pickResolvedScales(ingredient, server, depth + 1, inflight);
            if (picked == null) continue;
            picked.forEach((nutrient, scale) -> sumScale.merge(nutrient, scale, Float::sum));
            totalCount += 1;
        }

        if (totalCount == 0) return null;

        int outputCount = Math.max(1, recipe.getResultItem(access).getCount());
        double exponent = NutritionalConfig.SERVER.diminishingExponent.get();
        double dampening = Math.pow(totalCount, exponent);

        Map<ResourceLocation, Float> result = new HashMap<>();
        sumScale.forEach((nutrient, sum) -> {
            float scaled = (float) (sum / dampening / outputCount);
            if (scaled > 0.0f) result.put(nutrient, scaled);
        });
        return result;
    }

    private static Map<ResourceLocation, Float> pickResolvedScales(Ingredient ingredient,
                                                                   MinecraftServer server,
                                                                   int depth,
                                                                   Set<Item> inflight) {
        ItemStack[] options = ingredient.getItems();
        for (ItemStack option : options) {
            Item item = option.getItem();
            Map<ResourceLocation, Float> direct = nutrientScales(item);
            if (direct != null) return direct;
        }
        for (ItemStack option : options) {
            Map<ResourceLocation, Float> recurse = deriveInner(option.getItem(), server, depth, inflight);
            if (recurse != null && !recurse.isEmpty()) return recurse;
        }
        return null;
    }

    private static Map<ResourceLocation, Float> nutrientScales(Item item) {
        var nutrients = InvertedNutrientIndex.nutrientsFor(item);
        if (nutrients.isEmpty()) return null;

        NutrientScales scales = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(NutritionalDataMaps.NUTRIENT_SCALES);
        Map<ResourceLocation, Float> result = new HashMap<>();
        for (ResourceLocation id : nutrients) {
            float scale = scales != null ? scales.scaleFor(id) : 1.0f;
            result.put(id, scale);
        }
        return result;
    }

    private static RecipeHolder<?> findRecipeFor(Item target, MinecraftServer server) {
        RegistryAccess access = server.registryAccess();
        for (RecipeHolder<?> recipe : server.getRecipeManager().getRecipes()) {
            ItemStack out = recipe.value().getResultItem(access);
            if (!out.isEmpty() && out.is(target)) return recipe;
        }
        return null;
    }
}
