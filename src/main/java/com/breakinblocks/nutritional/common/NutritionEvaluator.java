package com.breakinblocks.nutritional.common;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.AttributeModifierEntry;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.codec.NutritionEffectDef;
import com.breakinblocks.nutritional.data.codec.ParticleVisibility;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NutritionEvaluator {

    private NutritionEvaluator() {}

    public static void evaluate(ServerPlayer player) {
        PlayerNutritionData data = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        Registry<NutritionEffectDef> effects = NutritionalDatapack.effects(player.level().registryAccess());
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(player.level().registryAccess());

        Map<Holder<MobEffect>, AppliedEffect> byMobEffect = new HashMap<>();
        Map<Identifier, PlayerModifierTracker.ModifierSpec> desiredModifiers = new HashMap<>();

        for (Holder.Reference<NutritionEffectDef> ref : effects.listElements().toList()) {
            Identifier effectId = ref.key().identifier();
            NutritionEffectDef def = ref.value();
            Decision decision = decide(def, data, nutrients);
            if (!decision.apply) continue;

            AppliedEffect existing = byMobEffect.get(def.mobEffect());
            int amplifier = decision.amplifier;
            if (existing == null || existing.amplifier < amplifier) {
                byMobEffect.put(def.mobEffect(), new AppliedEffect(def, amplifier));
            }

            for (AttributeModifierEntry mod : def.attributeModifiers()) {
                Identifier modId = Nutritional.id("effect_modifier/" + effectId.getNamespace() + "/" + effectId.getPath() + "/" + mod.attribute().getRegisteredName().replace(':', '/'));
                desiredModifiers.put(modId, new PlayerModifierTracker.ModifierSpec(mod.attribute(), mod.amount(), mod.operation()));
            }
        }

        for (AppliedEffect applied : byMobEffect.values()) {
            ParticleVisibility pv = applied.def.particles();
            MobEffectInstance instance = new MobEffectInstance(
                    applied.def.mobEffect(),
                    applied.def.durationTicks(),
                    applied.amplifier,
                    pv.ambient(),
                    pv.showParticles());
            player.addEffect(instance);
        }

        PlayerModifierScopes.EFFECTS.apply(player, desiredModifiers);
    }

    private static Decision decide(NutritionEffectDef def, PlayerNutritionData data, Registry<NutrientDefinition> nutrients) {
        List<Float> values = collectValues(def, data, nutrients);
        if (values.isEmpty()) return Decision.SKIP;

        return switch (def.detect()) {
            case ANY -> anyMatch(values, def) ? Decision.apply(def.amplifier()) : Decision.SKIP;
            case AVERAGE -> {
                float avg = average(values);
                yield inRange(avg, def) ? Decision.apply(def.amplifier()) : Decision.SKIP;
            }
            case ALL -> allMatch(values, def) ? Decision.apply(def.amplifier()) : Decision.SKIP;
            case CUMULATIVE -> {
                int count = countInRange(values, def);
                if (count == 0) yield Decision.SKIP;
                yield Decision.apply(Math.max(0, count * Math.max(1, def.cumulativeStep()) - 1));
            }
        };
    }

    private static List<Float> collectValues(NutritionEffectDef def, PlayerNutritionData data, Registry<NutrientDefinition> nutrients) {
        List<Float> values = new ArrayList<>(def.nutrients().size());
        for (var key : def.nutrients()) {
            NutrientDefinition n = nutrients.getValue(key.identifier());
            if (n == null) continue;
            values.add(data.get(key.identifier(), n.defaultValue()));
        }
        return values;
    }

    private static boolean anyMatch(List<Float> values, NutritionEffectDef def) {
        for (float v : values) if (inRange(v, def)) return true;
        return false;
    }

    private static boolean allMatch(List<Float> values, NutritionEffectDef def) {
        for (float v : values) if (!inRange(v, def)) return false;
        return true;
    }

    private static int countInRange(List<Float> values, NutritionEffectDef def) {
        int c = 0;
        for (float v : values) if (inRange(v, def)) c++;
        return c;
    }

    private static boolean inRange(float v, NutritionEffectDef def) {
        return v >= def.min() && v <= def.max();
    }

    private static float average(List<Float> values) {
        float sum = 0f;
        for (float v : values) sum += v;
        return sum / values.size();
    }

    private record Decision(boolean apply, int amplifier) {
        static final Decision SKIP = new Decision(false, 0);
        static Decision apply(int amp) { return new Decision(true, amp); }
    }

    private record AppliedEffect(NutritionEffectDef def, int amplifier) {}
}
