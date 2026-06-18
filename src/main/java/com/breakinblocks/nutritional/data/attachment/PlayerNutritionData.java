package com.breakinblocks.nutritional.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record PlayerNutritionData(Map<Identifier, Float> values,
                                  int consecutiveBalancedDays,
                                  long lastDayEvaluated,
                                  Set<Identifier> earnedRewards,
                                  Optional<Identifier> currentTier) {

    public static final PlayerNutritionData EMPTY =
            new PlayerNutritionData(Map.of(), 0, Long.MIN_VALUE, Set.of(), Optional.empty());

    public static final Codec<PlayerNutritionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT)
                    .optionalFieldOf("values", Map.of()).forGetter(PlayerNutritionData::values),
            Codec.INT.optionalFieldOf("consecutive_balanced_days", 0).forGetter(PlayerNutritionData::consecutiveBalancedDays),
            Codec.LONG.optionalFieldOf("last_day_evaluated", Long.MIN_VALUE).forGetter(PlayerNutritionData::lastDayEvaluated),
            Identifier.CODEC.listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("earned_rewards", new HashSet<>()).forGetter(d -> new HashSet<>(d.earnedRewards)),
            Identifier.CODEC.optionalFieldOf("current_tier").forGetter(PlayerNutritionData::currentTier)
    ).apply(instance, PlayerNutritionData::new));

    public float get(Identifier nutrientId, float fallback) {
        Float v = values.get(nutrientId);
        return v != null ? v : fallback;
    }

    public PlayerNutritionData withValue(Identifier id, float value) {
        Map<Identifier, Float> copy = new HashMap<>(values);
        copy.put(id, clamp(value));
        return new PlayerNutritionData(copy, consecutiveBalancedDays, lastDayEvaluated, earnedRewards, currentTier);
    }

    public PlayerNutritionData withValues(Map<Identifier, Float> updates) {
        if (updates.isEmpty()) return this;
        Map<Identifier, Float> copy = new HashMap<>(values);
        updates.forEach((id, v) -> copy.put(id, clamp(v)));
        return new PlayerNutritionData(copy, consecutiveBalancedDays, lastDayEvaluated, earnedRewards, currentTier);
    }

    public PlayerNutritionData adjust(Map<Identifier, Float> deltas, Function<Identifier, Float> fallbackProvider) {
        if (deltas.isEmpty()) return this;
        Map<Identifier, Float> copy = new HashMap<>(values);
        deltas.forEach((id, delta) -> {
            float current = copy.containsKey(id) ? copy.get(id) : fallbackProvider.apply(id);
            copy.put(id, clamp(current + delta));
        });
        return new PlayerNutritionData(copy, consecutiveBalancedDays, lastDayEvaluated, earnedRewards, currentTier);
    }

    public PlayerNutritionData withDayProgress(long day, int streak) {
        return new PlayerNutritionData(values, streak, day, earnedRewards, currentTier);
    }

    public PlayerNutritionData withReward(Identifier rewardId) {
        if (earnedRewards.contains(rewardId)) return this;
        Set<Identifier> copy = new HashSet<>(earnedRewards);
        copy.add(rewardId);
        return new PlayerNutritionData(values, consecutiveBalancedDays, lastDayEvaluated, copy, currentTier);
    }

    public PlayerNutritionData withoutReward(Identifier rewardId) {
        if (!earnedRewards.contains(rewardId)) return this;
        Set<Identifier> copy = new HashSet<>(earnedRewards);
        copy.remove(rewardId);
        return new PlayerNutritionData(values, consecutiveBalancedDays, lastDayEvaluated, copy, currentTier);
    }

    public PlayerNutritionData withTier(Optional<Identifier> tier) {
        return new PlayerNutritionData(values, consecutiveBalancedDays, lastDayEvaluated, earnedRewards, tier);
    }

    public static float clamp(float v) {
        if (v < 0.0f) return 0.0f;
        if (v > 100.0f) return 100.0f;
        return v;
    }
}
