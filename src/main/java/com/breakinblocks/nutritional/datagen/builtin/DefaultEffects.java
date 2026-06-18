package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.codec.DetectionMode;
import com.breakinblocks.nutritional.data.codec.NutritionEffectDef;
import com.breakinblocks.nutritional.data.codec.ParticleVisibility;
import com.breakinblocks.nutritional.data.registry.NutritionalRegistries;
import com.breakinblocks.nutritional.effect.NutritionalMobEffects;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public final class DefaultEffects {

    public static final ResourceKey<NutritionEffectDef> NOURISHED    = key("nourished");
    public static final ResourceKey<NutritionEffectDef> MALNOURISHED = key("malnourished");
    public static final ResourceKey<NutritionEffectDef> STRENGTH     = key("strength");
    public static final ResourceKey<NutritionEffectDef> WEAKNESS     = key("weakness");

    private DefaultEffects() {}

    public static void bootstrap(BootstrapContext<NutritionEffectDef> ctx) {
        ctx.register(NOURISHED, new NutritionEffectDef(
                NutritionalMobEffects.NOURISHED,
                0,
                75.0f, 100.0f,
                DetectionMode.AVERAGE,
                1,
                List.of(DefaultNutrients.FRUIT, DefaultNutrients.GRAIN,
                        DefaultNutrients.PROTEIN, DefaultNutrients.VEGETABLE,
                        DefaultNutrients.DAIRY),
                ParticleVisibility.TRANSPARENT,
                619,
                List.of()
        ));

        ctx.register(MALNOURISHED, new NutritionEffectDef(
                NutritionalMobEffects.MALNOURISHED,
                0,
                0.0f, 25.0f,
                DetectionMode.AVERAGE,
                1,
                List.of(DefaultNutrients.FRUIT, DefaultNutrients.GRAIN,
                        DefaultNutrients.PROTEIN, DefaultNutrients.VEGETABLE,
                        DefaultNutrients.DAIRY),
                ParticleVisibility.TRANSPARENT,
                619,
                List.of()
        ));

        ctx.register(STRENGTH, new NutritionEffectDef(
                MobEffects.STRENGTH,
                0,
                85.0f, 100.0f,
                DetectionMode.ANY,
                1,
                List.of(DefaultNutrients.PROTEIN),
                ParticleVisibility.TRANSPARENT,
                619,
                List.of()
        ));

        ctx.register(WEAKNESS, new NutritionEffectDef(
                MobEffects.WEAKNESS,
                0,
                0.0f, 15.0f,
                DetectionMode.ANY,
                1,
                List.of(DefaultNutrients.PROTEIN),
                ParticleVisibility.TRANSPARENT,
                619,
                List.of()
        ));
    }

    private static ResourceKey<NutritionEffectDef> key(String name) {
        return ResourceKey.create(NutritionalRegistries.EFFECT, Nutritional.id(name));
    }
}
