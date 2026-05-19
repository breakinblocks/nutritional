package com.breakinblocks.nutritional.userpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class ModConditional {

    public static final String VANILLA_NAMESPACE = "minecraft";
    public static final String NUTRITIONAL_NAMESPACE = "nutritional";

    private ModConditional() {}

    public static boolean isForeign(ResourceLocation id) {
        String ns = id.getNamespace();
        return !ns.equals(VANILLA_NAMESPACE) && !ns.equals(NUTRITIONAL_NAMESPACE);
    }

    public static JsonArray modLoadedConditions(ResourceLocation foreignItem) {
        JsonArray array = new JsonArray();
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", foreignItem.getNamespace());
        array.add(condition);
        return array;
    }
}
