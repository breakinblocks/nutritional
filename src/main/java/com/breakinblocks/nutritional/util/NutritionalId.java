package com.breakinblocks.nutritional.util;

import com.breakinblocks.nutritional.Nutritional;
import net.minecraft.resources.Identifier;

public final class NutritionalId {

    private NutritionalId() {}

    public static Identifier of(String path) {
        return Identifier.fromNamespaceAndPath(Nutritional.MOD_ID, path);
    }
}
