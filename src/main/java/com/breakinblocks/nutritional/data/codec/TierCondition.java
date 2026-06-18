package com.breakinblocks.nutritional.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public record TierCondition(CombineMode combine,
                            Optional<RangeSpec> average,
                            Optional<RangeSpec> minimum,
                            Optional<RangeSpec> maximum,
                            Map<Identifier, RangeSpec> perNutrient) {

    public static final Codec<TierCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CombineMode.CODEC.optionalFieldOf("combine", CombineMode.ALL).forGetter(TierCondition::combine),
            RangeSpec.CODEC.optionalFieldOf("average").forGetter(TierCondition::average),
            RangeSpec.CODEC.optionalFieldOf("minimum").forGetter(TierCondition::minimum),
            RangeSpec.CODEC.optionalFieldOf("maximum").forGetter(TierCondition::maximum),
            Codec.unboundedMap(Identifier.CODEC, RangeSpec.CODEC).optionalFieldOf("per_nutrient", Map.of()).forGetter(TierCondition::perNutrient)
    ).apply(instance, TierCondition::new));
}
