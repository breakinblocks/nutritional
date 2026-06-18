package com.breakinblocks.nutritional.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class RewardEarnedTrigger extends SimpleCriterionTrigger<RewardEarnedTrigger.Instance> {

    public static final Codec<Instance> CODEC = RecordCodecBuilder.create(b -> b.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
            Identifier.CODEC.optionalFieldOf("reward").forGetter(Instance::reward)
    ).apply(b, Instance::new));

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public void trigger(ServerPlayer player, Identifier rewardId) {
        this.trigger(player, instance -> instance.matches(rewardId));
    }

    public static Criterion<Instance> any() {
        return NutritionalCriteria.REWARD_EARNED.get().createCriterion(new Instance(Optional.empty(), Optional.empty()));
    }

    public static Criterion<Instance> reward(Identifier rewardId) {
        return NutritionalCriteria.REWARD_EARNED.get().createCriterion(new Instance(Optional.empty(), Optional.of(rewardId)));
    }

    public record Instance(Optional<ContextAwarePredicate> player,
                           Optional<Identifier> reward) implements SimpleCriterionTrigger.SimpleInstance {

        public boolean matches(Identifier rewardId) {
            return reward.isEmpty() || reward.get().equals(rewardId);
        }
    }
}
