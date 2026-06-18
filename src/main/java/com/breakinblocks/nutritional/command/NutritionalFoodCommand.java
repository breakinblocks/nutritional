package com.breakinblocks.nutritional.command;

import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NutritionalFoodCommand {

    private NutritionalFoodCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nutritional-food")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(NutritionalFoodCommand::executeInfo));
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Hold an item in your main hand."));
            return 0;
        }
        List<Identifier> nutrients = InvertedNutrientIndex.nutrientsFor(held.getItem());
        Map<Identifier, Float> yield = NutritionalLogic.calculateNutrition(held, player);
        String namePart = held.getHoverName().getString();
        if (nutrients.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(namePart + " has no nutrient mapping."), false);
            return 0;
        }
        String summary = yield.entrySet().stream()
                .map(e -> e.getKey() + "=" + String.format("%.2f", e.getValue()))
                .collect(Collectors.joining(", "));
        ctx.getSource().sendSuccess(() -> Component.literal(namePart + " -> " + summary), false);
        return nutrients.size();
    }
}
