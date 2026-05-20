package com.breakinblocks.nutritional.userpack.recipe;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.userpack.UserPackWriter;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
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
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();

        List<Item> candidates = EdibleItemScanner.scanForUnmapped(server);
        int total = candidates.size();
        source.sendSuccess(() -> Component.literal("Scanning " + total + " edible items without nutrient mappings...")
                .withStyle(ChatFormatting.GRAY), true);

        if (total == 0) {
            source.sendSuccess(() -> Component.literal("All edible items are already mapped. Nothing to do.")
                    .withStyle(ChatFormatting.GREEN), true);
            return 0;
        }

        int derived = 0;
        int writeFailed = 0;
        List<ResourceLocation> missing = new ArrayList<>();
        for (Item item : candidates) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            var maybe = NutrientGenerator.derive(item, server);
            if (maybe.isEmpty()) {
                missing.add(itemId);
                Nutritional.LOGGER.warn("update-foods: no baseline values for {}", itemId);
                continue;
            }
            if (!writeEntries(itemId, maybe.get())) {
                writeFailed++;
                continue;
            }
            derived++;
        }

        int derivedCount = derived;
        int missingCount = missing.size();
        int writeFailedCount = writeFailed;

        ChatFormatting summaryColor = derived > 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        source.sendSuccess(() -> Component.literal(
                "update-foods complete: " + derivedCount + " derived, "
                        + missingCount + " unmappable, "
                        + writeFailedCount + " write errors. See server log for details.")
                .withStyle(summaryColor), true);

        if (derived > 0) {
            source.sendSuccess(() -> Component.literal("Reloading datapacks to apply changes...")
                    .withStyle(ChatFormatting.GRAY), true);
            server.reloadResources(server.getPackRepository().getSelectedIds());
        }

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
