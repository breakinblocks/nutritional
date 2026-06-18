package com.breakinblocks.nutritional.data.registry;

import com.breakinblocks.nutritional.data.codec.DietTierDefinition;
import com.breakinblocks.nutritional.data.codec.DimensionModifier;
import com.breakinblocks.nutritional.data.codec.FoodHintDef;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.codec.NutritionEffectDef;
import com.breakinblocks.nutritional.data.codec.SustainedRewardDefinition;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

public final class NutritionalDatapack {

    private NutritionalDatapack() {}

    public static Registry<NutrientDefinition> nutrients(RegistryAccess access) {
        return access.lookupOrThrow(NutritionalRegistries.NUTRIENT);
    }

    public static Registry<NutritionEffectDef> effects(RegistryAccess access) {
        return access.lookupOrThrow(NutritionalRegistries.EFFECT);
    }

    public static Registry<FoodHintDef> foodHints(RegistryAccess access) {
        return access.lookupOrThrow(NutritionalRegistries.FOOD_HINT);
    }

    public static Registry<DietTierDefinition> tiers(RegistryAccess access) {
        return access.lookupOrThrow(NutritionalRegistries.DIET_TIER);
    }

    public static Registry<SustainedRewardDefinition> rewards(RegistryAccess access) {
        return access.lookupOrThrow(NutritionalRegistries.SUSTAINED_REWARD);
    }

    public static Registry<DimensionModifier> dimensionModifiers(RegistryAccess access) {
        return access.lookupOrThrow(NutritionalRegistries.DIMENSION_MODIFIER);
    }
}
