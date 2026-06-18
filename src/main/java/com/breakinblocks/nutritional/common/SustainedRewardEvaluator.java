package com.breakinblocks.nutritional.common;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.advancement.NutritionalCriteria;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.AttributeModifierEntry;
import com.breakinblocks.nutritional.data.codec.DietTierDefinition;
import com.breakinblocks.nutritional.data.codec.SustainedRewardDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.data.registry.NutritionalRegistries;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SustainedRewardEvaluator {

    private static final long TICKS_PER_DAY = 24000L;

    private SustainedRewardEvaluator() {}

    public static void evaluate(ServerPlayer player) {
        PlayerNutritionData data = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        long currentDay = player.level().getOverworldClockTime() / TICKS_PER_DAY;
        boolean dayChanged = currentDay != data.lastDayEvaluated();

        Registry<DietTierDefinition> tiers = NutritionalDatapack.tiers(player.level().registryAccess());
        Registry<SustainedRewardDefinition> rewards = NutritionalDatapack.rewards(player.level().registryAccess());
        if (rewards.size() == 0) return;

        int streak = data.consecutiveBalancedDays();
        if (dayChanged && data.lastDayEvaluated() != Long.MIN_VALUE) {
            streak = data.currentTier().isPresent() ? streak + 1 : 0;
        } else if (dayChanged) {
            streak = data.currentTier().isPresent() ? 1 : 0;
        }

        Set<Identifier> earned = new HashSet<>(data.earnedRewards());
        Map<Identifier, PlayerModifierTracker.ModifierSpec> activeModifiers = new HashMap<>();

        for (Holder.Reference<SustainedRewardDefinition> ref : rewards.listElements().toList()) {
            Identifier rewardId = ref.key().identifier();
            SustainedRewardDefinition def = ref.value();
            boolean qualifies = qualifies(def, data.currentTier(), tiers);
            boolean longEnough = streak >= def.consecutiveDaysRequired();
            boolean shouldRemove = !qualifies && belowFloor(def, data.currentTier(), tiers);

            if (longEnough && qualifies) {
                if (!earned.contains(rewardId)) {
                    earned.add(rewardId);
                    NutritionalCriteria.REWARD_EARNED.get().trigger(player, rewardId);
                }
                addModifiers(activeModifiers, rewardId, def);
            } else if (earned.contains(rewardId)) {
                if (shouldRemove) earned.remove(rewardId);
                else addModifiers(activeModifiers, rewardId, def);
            }
        }

        PlayerModifierScopes.SUSTAINED.apply(player, activeModifiers);

        boolean stateChanged = dayChanged
                || streak != data.consecutiveBalancedDays()
                || !earned.equals(data.earnedRewards());
        if (stateChanged) {
            PlayerNutritionData updated = new PlayerNutritionData(
                    data.values(), streak, currentDay, earned, data.currentTier());
            player.setData(NutritionalAttachments.PLAYER_NUTRITION, updated);
            NutritionalNetwork.sendFullSync(player);
        }
    }

    private static void addModifiers(Map<Identifier, PlayerModifierTracker.ModifierSpec> sink,
                                     Identifier rewardId,
                                     SustainedRewardDefinition def) {
        for (AttributeModifierEntry mod : def.attributeModifiers()) {
            Identifier modId = Nutritional.id("sustained/" + rewardId.getNamespace() + "/" + rewardId.getPath() + "/" + mod.attribute().getRegisteredName().replace(':', '/'));
            sink.put(modId, new PlayerModifierTracker.ModifierSpec(mod.attribute(), mod.amount(), mod.operation()));
        }
    }

    private static boolean qualifies(SustainedRewardDefinition def,
                                     Optional<Identifier> activeTier,
                                     Registry<DietTierDefinition> tiers) {
        if (activeTier.isEmpty()) return false;
        DietTierDefinition currentDef = tiers.getValue(activeTier.get());
        DietTierDefinition requiredDef = tiers.getValue(def.qualifyingTier().identifier());
        if (currentDef == null || requiredDef == null) return false;
        return currentDef.priority() >= requiredDef.priority();
    }

    private static boolean belowFloor(SustainedRewardDefinition def,
                                      Optional<Identifier> activeTier,
                                      Registry<DietTierDefinition> tiers) {
        Optional<ResourceKey<DietTierDefinition>> floor = def.loseOnTierBelow();
        if (floor.isEmpty()) return false;
        DietTierDefinition floorDef = tiers.getValue(floor.get().identifier());
        if (floorDef == null) return false;
        if (activeTier.isEmpty()) return true;
        DietTierDefinition currentDef = tiers.getValue(activeTier.get());
        return currentDef == null || currentDef.priority() < floorDef.priority();
    }

    public static void stripOnDeath(ServerPlayer player) {
        PlayerNutritionData data = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        Registry<SustainedRewardDefinition> rewards = NutritionalDatapack.rewards(player.level().registryAccess());
        Set<Identifier> retained = new HashSet<>();
        for (Identifier rewardId : data.earnedRewards()) {
            SustainedRewardDefinition def = rewards.getValue(rewardId);
            if (def != null && !def.loseOnDeath()) retained.add(rewardId);
        }
        if (!retained.equals(data.earnedRewards())) {
            PlayerNutritionData updated = new PlayerNutritionData(
                    data.values(), data.consecutiveBalancedDays(), data.lastDayEvaluated(), retained, data.currentTier());
            player.setData(NutritionalAttachments.PLAYER_NUTRITION, updated);
        }
    }
}
