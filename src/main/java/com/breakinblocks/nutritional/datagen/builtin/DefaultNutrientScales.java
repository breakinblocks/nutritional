package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class DefaultNutrientScales extends DataMapProvider {

    public DefaultNutrientScales(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather() {
        Builder<NutrientScales, Item> builder = builder(NutritionalDataMaps.NUTRIENT_SCALES);
        builder.add(itemKey(Items.ROTTEN_FLESH),     scales(Nutritional.id("protein"),   0.25f), false);
        builder.add(itemKey(Items.SPIDER_EYE),       scales(Nutritional.id("protein"),   0.25f), false);
        builder.add(itemKey(Items.POISONOUS_POTATO), scales(Nutritional.id("vegetable"), 0.25f), false);
    }

    @Override
    public String getName() {
        return "Nutritional Item DataMaps";
    }

    private static ResourceKey<Item> itemKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }

    private static NutrientScales scales(ResourceLocation nutrient, float scale) {
        return new NutrientScales(Map.of(nutrient, scale));
    }
}
