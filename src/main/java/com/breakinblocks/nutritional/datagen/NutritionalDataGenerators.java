package com.breakinblocks.nutritional.datagen;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.registry.NutritionalRegistries;
import com.breakinblocks.nutritional.datagen.builtin.DefaultAdvancements;
import com.breakinblocks.nutritional.datagen.builtin.DefaultDimensionModifiers;
import com.breakinblocks.nutritional.datagen.builtin.DefaultEffects;
import com.breakinblocks.nutritional.datagen.builtin.DefaultFoodHints;
import com.breakinblocks.nutritional.datagen.builtin.DefaultNutrientScales;
import com.breakinblocks.nutritional.datagen.builtin.DefaultNutrients;
import com.breakinblocks.nutritional.datagen.builtin.DefaultRewards;
import com.breakinblocks.nutritional.datagen.builtin.DefaultTiers;
import com.breakinblocks.nutritional.datagen.lang.EnUsLangProvider;
import com.breakinblocks.nutritional.datagen.tags.NutritionalItemTagsProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class NutritionalDataGenerators {

    private NutritionalDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(NutritionalRegistries.NUTRIENT, DefaultNutrients::bootstrap)
                .add(NutritionalRegistries.EFFECT, DefaultEffects::bootstrap)
                .add(NutritionalRegistries.FOOD_HINT, DefaultFoodHints::bootstrap)
                .add(NutritionalRegistries.DIET_TIER, DefaultTiers::bootstrap)
                .add(NutritionalRegistries.SUSTAINED_REWARD, DefaultRewards::bootstrap)
                .add(NutritionalRegistries.DIMENSION_MODIFIER, DefaultDimensionModifiers::bootstrap);

        generator.addProvider(true,
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), builder, Set.of(Nutritional.MOD_ID)));

        generator.addProvider(true,
                new NutritionalItemTagsProvider(output, event.getLookupProvider()));

        generator.addProvider(true,
                new DefaultNutrientScales(output, event.getLookupProvider()));

        generator.addProvider(true,
                new AdvancementProvider(output, event.getLookupProvider(), List.of(new DefaultAdvancements())));

        generator.addProvider(true, new EnUsLangProvider(output));
    }
}
