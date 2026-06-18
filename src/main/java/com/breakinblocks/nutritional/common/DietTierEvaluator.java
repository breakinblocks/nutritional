package com.breakinblocks.nutritional.common;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.advancement.NutritionalCriteria;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.AttributeModifierEntry;
import com.breakinblocks.nutritional.data.codec.CombineMode;
import com.breakinblocks.nutritional.data.codec.DietTierDefinition;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.codec.RangeSpec;
import com.breakinblocks.nutritional.data.codec.TierCondition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DietTierEvaluator {

    private DietTierEvaluator() {}

    public static void evaluate(ServerPlayer player) {
        PlayerNutritionData data = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        Registry<DietTierDefinition> tiers = NutritionalDatapack.tiers(player.level().registryAccess());
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(player.level().registryAccess());
        if (tiers.size() == 0) return;

        Holder.Reference<DietTierDefinition> active = pickActiveTier(tiers, data, nutrients);
        Optional<Identifier> activeId = active != null
                ? Optional.of(active.key().identifier())
                : Optional.empty();

        Map<Identifier, PlayerModifierTracker.ModifierSpec> tierModifiers = new HashMap<>();
        if (active != null) {
            Identifier tierId = active.key().identifier();
            for (AttributeModifierEntry mod : active.value().attributeModifiers()) {
                Identifier modId = Nutritional.id("tier/" + tierId.getNamespace() + "/" + tierId.getPath() + "/" + mod.attribute().getRegisteredName().replace(':', '/'));
                tierModifiers.put(modId, new PlayerModifierTracker.ModifierSpec(mod.attribute(), mod.amount(), mod.operation()));
            }
        }
        PlayerModifierScopes.TIER.apply(player, tierModifiers);

        if (!activeId.equals(data.currentTier())) {
            player.setData(NutritionalAttachments.PLAYER_NUTRITION, data.withTier(activeId));
            NutritionalNetwork.sendFullSync(player);
            activeId.ifPresent(id -> NutritionalCriteria.TIER_REACHED.get().trigger(player, id));
        }
    }

    public static Holder.Reference<DietTierDefinition> pickActiveTier(Registry<DietTierDefinition> tiers,
                                                                     PlayerNutritionData data,
                                                                     Registry<NutrientDefinition> nutrients) {
        return tiers.listElements()
                .sorted((a, b) -> Integer.compare(b.value().priority(), a.value().priority()))
                .filter(ref -> matches(ref.value().condition(), data, nutrients))
                .findFirst()
                .orElse(null);
    }

    public static boolean matches(TierCondition condition, PlayerNutritionData data, Registry<NutrientDefinition> nutrients) {
        boolean combineAll = condition.combine() == CombineMode.ALL;
        boolean sawAny = false;
        boolean anyPassed = false;

        if (condition.average().isPresent()) {
            sawAny = true;
            boolean pass = condition.average().get().test(averageValue(data, nutrients));
            if (combineAll && !pass) return false;
            if (pass) anyPassed = true;
        }
        if (condition.minimum().isPresent()) {
            sawAny = true;
            boolean pass = condition.minimum().get().test(minValue(data, nutrients));
            if (combineAll && !pass) return false;
            if (pass) anyPassed = true;
        }
        if (condition.maximum().isPresent()) {
            sawAny = true;
            boolean pass = condition.maximum().get().test(maxValue(data, nutrients));
            if (combineAll && !pass) return false;
            if (pass) anyPassed = true;
        }
        for (Map.Entry<Identifier, RangeSpec> entry : condition.perNutrient().entrySet()) {
            sawAny = true;
            NutrientDefinition def = nutrients.getValue(entry.getKey());
            if (def == null) {
                if (combineAll) return false;
                continue;
            }
            boolean pass = entry.getValue().test(data.get(entry.getKey(), def.defaultValue()));
            if (combineAll && !pass) return false;
            if (pass) anyPassed = true;
        }
        if (!sawAny) return true;
        return combineAll || anyPassed;
    }

    private static float averageValue(PlayerNutritionData data, Registry<NutrientDefinition> nutrients) {
        if (nutrients.size() == 0) return 0f;
        float total = 0f;
        int count = 0;
        for (Holder.Reference<NutrientDefinition> ref : nutrients.listElements().toList()) {
            total += data.get(ref.key().identifier(), ref.value().defaultValue());
            count++;
        }
        return count == 0 ? 0f : total / count;
    }

    private static float minValue(PlayerNutritionData data, Registry<NutrientDefinition> nutrients) {
        float min = Float.POSITIVE_INFINITY;
        for (Holder.Reference<NutrientDefinition> ref : nutrients.listElements().toList()) {
            float v = data.get(ref.key().identifier(), ref.value().defaultValue());
            if (v < min) min = v;
        }
        return min == Float.POSITIVE_INFINITY ? 0f : min;
    }

    private static float maxValue(PlayerNutritionData data, Registry<NutrientDefinition> nutrients) {
        float max = Float.NEGATIVE_INFINITY;
        for (Holder.Reference<NutrientDefinition> ref : nutrients.listElements().toList()) {
            float v = data.get(ref.key().identifier(), ref.value().defaultValue());
            if (v > max) max = v;
        }
        return max == Float.NEGATIVE_INFINITY ? 0f : max;
    }
}
