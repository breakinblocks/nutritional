package com.breakinblocks.nutritional.userpack.recipe;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.userpack.UserPackWriter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;

public final class FoodSetWriter {

    private FoodSetWriter() {}

    public static int setOnHeld(CommandContext<CommandSourceStack> ctx,
                                ResourceLocation nutrientId,
                                float scale) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Hold an item in your main hand."));
            return 0;
        }

        MinecraftServer server = ctx.getSource().getServer();
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(server.registryAccess());
        if (nutrients.get(nutrientId) == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown nutrient: " + nutrientId));
            return 0;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        try {
            UserPackWriter.addItemToNutrientTag(nutrientId, itemId);
            if (Math.abs(scale - 1.0f) > 0.001f) {
                UserPackWriter.setNutrientScale(itemId, nutrientId, scale);
            } else {
                UserPackWriter.removeNutrientScale(itemId, nutrientId);
            }
        } catch (IOException e) {
            Nutritional.LOGGER.error("food set: failed for {} -> {}", itemId, nutrientId, e);
            ctx.getSource().sendFailure(Component.literal("Failed to write: " + e.getMessage()));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Wrote " + itemId + " -> " + nutrientId + " (scale " + String.format("%.2f", scale) + "). Run /reload to apply.")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    public static int removeFromHeld(CommandContext<CommandSourceStack> ctx,
                                     ResourceLocation nutrientId) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Hold an item in your main hand."));
            return 0;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        try {
            UserPackWriter.removeItemFromNutrientTag(nutrientId, itemId);
            UserPackWriter.removeNutrientScale(itemId, nutrientId);
        } catch (IOException e) {
            Nutritional.LOGGER.error("food remove: failed for {} -> {}", itemId, nutrientId, e);
            ctx.getSource().sendFailure(Component.literal("Failed to write: " + e.getMessage()));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Removed " + itemId + " -> " + nutrientId + ". Run /reload to apply.")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
}
