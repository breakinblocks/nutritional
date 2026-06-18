package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.data.codec.DimensionModifier;
import com.breakinblocks.nutritional.data.registry.NutritionalRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public final class DefaultDimensionModifiers {

    private DefaultDimensionModifiers() {}

    public static void bootstrap(BootstrapContext<DimensionModifier> ctx) {
        ctx.register(key(Level.NETHER.identifier()),    new DimensionModifier(0.9f, 1.5f));
        ctx.register(key(Level.END.identifier()),       new DimensionModifier(0.8f, 1.25f));
    }

    private static ResourceKey<DimensionModifier> key(Identifier dim) {
        return ResourceKey.create(NutritionalRegistries.DIMENSION_MODIFIER, dim);
    }
}
