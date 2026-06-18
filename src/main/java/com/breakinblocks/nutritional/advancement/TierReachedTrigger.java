package com.breakinblocks.nutritional.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class TierReachedTrigger extends SimpleCriterionTrigger<TierReachedTrigger.Instance> {

    public static final Codec<Instance> CODEC = RecordCodecBuilder.create(b -> b.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
            Identifier.CODEC.optionalFieldOf("tier").forGetter(Instance::tier)
    ).apply(b, Instance::new));

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public void trigger(ServerPlayer player, Identifier tierId) {
        this.trigger(player, instance -> instance.matches(tierId));
    }

    public static Criterion<Instance> any() {
        return NutritionalCriteria.TIER_REACHED.get().createCriterion(new Instance(Optional.empty(), Optional.empty()));
    }

    public static Criterion<Instance> tier(Identifier tierId) {
        return NutritionalCriteria.TIER_REACHED.get().createCriterion(new Instance(Optional.empty(), Optional.of(tierId)));
    }

    public record Instance(Optional<ContextAwarePredicate> player,
                           Optional<Identifier> tier) implements SimpleCriterionTrigger.SimpleInstance {

        public boolean matches(Identifier tierId) {
            return tier.isEmpty() || tier.get().equals(tierId);
        }
    }
}
