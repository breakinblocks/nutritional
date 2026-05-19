package com.breakinblocks.nutritional.userpack.recipe;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.userpack.UserPackWriter;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class UpdateFoodsRunner {

    private UpdateFoodsRunner() {}

    public static int run(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        List<Item> candidates = EdibleItemScanner.scanForUnmapped(server);

        int derived = 0;
        List<ResourceLocation> missing = new ArrayList<>();
        for (Item item : candidates) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            var maybe = NutrientGenerator.derive(item, server);
            if (maybe.isEmpty()) {
                missing.add(itemId);
                Nutritional.LOGGER.warn("update-foods: no baseline values for {}", itemId);
                continue;
            }
            if (!writeEntries(itemId, maybe.get())) continue;
            derived++;
        }

        if (derived > 0) {
            server.reloadResources(server.getPackRepository().getSelectedIds());
        }

        int derivedCount = derived;
        int missingCount = missing.size();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Derived " + derivedCount + " entries, " + missingCount + " missing (see server log)."), true);
        return derived;
    }

    private static boolean writeEntries(ResourceLocation itemId, Map<ResourceLocation, Float> scales) {
        try {
            for (Map.Entry<ResourceLocation, Float> entry : scales.entrySet()) {
                ResourceLocation nutrientId = entry.getKey();
                float scale = entry.getValue();
                UserPackWriter.addItemToNutrientTag(nutrientId, itemId);
                if (Math.abs(scale - 1.0f) > 0.001f) {
                    UserPackWriter.setNutrientScale(itemId, nutrientId, scale);
                }
            }
            return true;
        } catch (IOException e) {
            Nutritional.LOGGER.error("update-foods: failed to write entries for {}", itemId, e);
            return false;
        }
    }
}
