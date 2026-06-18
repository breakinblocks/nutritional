package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public final class DefaultNutrientScales extends DataMapProvider {

    public DefaultNutrientScales(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather() {
        Builder<NutrientScales, Item> builder = builder(NutritionalDataMaps.NUTRIENT_SCALES);
        for (ModCompatFoods.Entry entry : ModCompatFoods.entries()) {
            builder.add(entry.item(), new NutrientScales(entry.scales()), false, new ModLoadedCondition(entry.modId()));
        }
    }
}
