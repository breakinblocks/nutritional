package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class DefaultNutrients {

    public static final ResourceKey<NutrientDefinition> FRUIT     = key("fruit");
    public static final ResourceKey<NutrientDefinition> GRAIN     = key("grain");
    public static final ResourceKey<NutrientDefinition> PROTEIN   = key("protein");
    public static final ResourceKey<NutrientDefinition> VEGETABLE = key("vegetable");
    public static final ResourceKey<NutrientDefinition> DAIRY     = key("dairy");

    private DefaultNutrients() {}

    public static void bootstrap(BootstrapContext<NutrientDefinition> ctx) {
        ctx.register(FRUIT, new NutrientDefinition(
                Items.APPLE.builtInRegistryHolder(), 0xCF3531, 1.0f, true, 50.0f, Optional.empty()));
        ctx.register(GRAIN, new NutrientDefinition(
                Items.BREAD.builtInRegistryHolder(), 0xF4D92E, 1.0f, true, 50.0f, Optional.empty()));
        ctx.register(PROTEIN, new NutrientDefinition(
                Items.COOKED_BEEF.builtInRegistryHolder(), 0xA0522D, 1.0f, true, 50.0f, Optional.empty()));
        ctx.register(VEGETABLE, new NutrientDefinition(
                Items.CARROT.builtInRegistryHolder(), 0x72DD5A, 1.0f, true, 50.0f, Optional.empty()));
        ctx.register(DAIRY, new NutrientDefinition(
                Items.MILK_BUCKET.builtInRegistryHolder(), 0xFFF8E7, 1.0f, true, 50.0f, Optional.empty()));
    }

    private static ResourceKey<NutrientDefinition> key(String name) {
        return ResourceKey.create(NutritionalRegistries.NUTRIENT, Nutritional.id(name));
    }
}
