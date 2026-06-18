package com.breakinblocks.nutritional.client;

import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ClientNutritionCache {

    private static volatile PlayerNutritionData data = PlayerNutritionData.EMPTY;

    private ClientNutritionCache() {}

    public static PlayerNutritionData get() {
        return data;
    }

    public static void set(PlayerNutritionData incoming) {
        data = incoming;
    }

    public static void applyDelta(Map<Identifier, Float> updates) {
        Map<Identifier, Float> merged = new HashMap<>(data.values());
        merged.putAll(updates);
        data = new PlayerNutritionData(merged, data.consecutiveBalancedDays(), data.lastDayEvaluated(), data.earnedRewards(), data.currentTier());
    }

    public static void clear() {
        data = PlayerNutritionData.EMPTY;
    }
}
