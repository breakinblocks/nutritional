package com.breakinblocks.nutritional.common;

import com.breakinblocks.nutritional.attribute.NutritionalAttributes;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.DimensionModifier;
import com.breakinblocks.nutritional.data.codec.FoodHintDef;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NutritionalLogic {

    private NutritionalLogic() {}

    public static Map<Identifier, Float> calculateNutrition(ItemStack stack, ServerPlayer player) {
        List<Identifier> applicable = InvertedNutrientIndex.nutrientsFor(stack.getItem());
        if (applicable.isEmpty()) return Map.of();

        RegistryAccess access = player.level().registryAccess();
        float baseFood = getBaseFoodValue(stack, access);
        if (baseFood <= 0.0f) return Map.of();

        double nutritionMul = NutritionalConfig.SERVER.nutritionMultiplier.get();
        double absorption = player.getAttributeValue(NutritionalAttributes.NUTRIENT_ABSORPTION);
        double lossPerExtra = NutritionalConfig.SERVER.lossPerExtraNutrient.get();
        double lossRatio = Math.min(1.0, lossPerExtra / 100.0 * Math.max(0, applicable.size() - 1));
        double dimYield = dimensionModifier(player, access).yieldMultiplier();
        Optional<NutrientScales> scales = Optional.ofNullable(
                BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).getData(NutritionalDataMaps.NUTRIENT_SCALES));

        Map<Identifier, Float> result = new HashMap<>();
        for (Identifier nutrientId : applicable) {
            float scale = scales.map(s -> s.scaleFor(nutrientId)).orElse(1.0f);
            float yield = (float) (baseFood * 0.5 * nutritionMul * absorption * scale * dimYield * (1.0 - lossRatio));
            result.put(nutrientId, yield);
        }
        return result;
    }

    public static DimensionModifier dimensionModifier(ServerPlayer player, RegistryAccess access) {
        Registry<DimensionModifier> registry = NutritionalDatapack.dimensionModifiers(access);
        DimensionModifier mod = registry.getValue(player.level().dimension().identifier());
        return mod != null ? mod : DimensionModifier.IDENTITY;
    }

    public static Optional<FoodHintDef> findFoodHint(ItemStack stack, RegistryAccess access) {
        Registry<FoodHintDef> hints = NutritionalDatapack.foodHints(access);
        for (FoodHintDef hint : hints) {
            if (hint.match().matches(stack)) return Optional.of(hint);
        }
        return Optional.empty();
    }

    public static boolean isEdible(ItemStack stack, RegistryAccess access) {
        Optional<FoodHintDef> hint = findFoodHint(stack, access);
        if (hint.isPresent()) return hint.get().isValidFood();
        return stack.has(DataComponents.FOOD);
    }

    public static float applyDecay(float current, int foodDrop, NutrientDefinition def, ServerPlayer player) {
        if (!NutritionalConfig.SERVER.decayEnabled.get()) return current;
        double globalMul = NutritionalConfig.SERVER.decayMultiplier.get();
        double decayRateAttr = player.getAttributeValue(NutritionalAttributes.NUTRIENT_DECAY_RATE);
        double dimDecay = dimensionModifier(player, player.level().registryAccess()).decayMultiplier();
        float decay = (float) (foodDrop * 0.075 * def.decay() * globalMul * decayRateAttr * dimDecay);
        return PlayerNutritionData.clamp(current - decay);
    }

    public static float applyDeathPenalty(float current) {
        double floor = NutritionalConfig.SERVER.deathPenaltyFloor.get();
        double amount = NutritionalConfig.SERVER.deathPenaltyAmount.get();
        boolean resetBelow = NutritionalConfig.SERVER.deathPenaltyResetBelowFloor.get();
        if (current <= floor) return resetBelow ? (float) floor : current;
        return (float) Math.max(floor, current - amount);
    }

    private static float getBaseFoodValue(ItemStack stack, RegistryAccess access) {
        Optional<FoodHintDef> hint = findFoodHint(stack, access);
        if (hint.isPresent()) return hint.get().healAmount();
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) return food.nutrition();
        return 0.0f;
    }
}
