package com.breakinblocks.nutritional.command;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class NutritionalCommand {

    private NutritionalCommand() {}

    private static final SuggestionProvider<CommandSourceStack> NUTRIENT_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    NutritionalDatapack.nutrients(ctx.getSource().registryAccess()).keySet().stream(),
                    builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("nutritional")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));

        root.then(Commands.literal("get")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", IdentifierArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .executes(NutritionalCommand::executeGet))));

        root.then(Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", IdentifierArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 100.0f))
                                        .executes(NutritionalCommand::executeSet)))));

        root.then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", IdentifierArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(ctx -> executeAdjust(ctx, v -> v + FloatArgumentType.getFloat(ctx, "amount")))))));

        root.then(Commands.literal("subtract")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", IdentifierArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(ctx -> executeAdjust(ctx, v -> v - FloatArgumentType.getFloat(ctx, "amount")))))));

        root.then(Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(NutritionalCommand::executeResetAll)
                        .then(Commands.argument("nutrient", IdentifierArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .executes(NutritionalCommand::executeResetOne))));

        root.then(Commands.literal("reload")
                .executes(NutritionalCommand::executeReload));

        dispatcher.register(root);
    }

    private static int executeGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Identifier nutrientId = IdentifierArgument.getId(ctx, "nutrient");
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        NutrientDefinition def = registry.getValue(nutrientId);
        if (def == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown nutrient: " + nutrientId));
            return 0;
        }
        PlayerNutritionData data = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        float value = data.get(nutrientId, def.defaultValue());
        ctx.getSource().sendSuccess(() -> Component.literal(
                player.getName().getString() + " " + nutrientId + " = " + String.format("%.2f", value)), false);
        return Math.round(value);
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Identifier nutrientId = IdentifierArgument.getId(ctx, "nutrient");
        float value = FloatArgumentType.getFloat(ctx, "value");
        return apply(ctx, player, nutrientId, v -> value, "set");
    }

    private static int executeAdjust(CommandContext<CommandSourceStack> ctx, UnaryOperator<Float> op) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Identifier nutrientId = IdentifierArgument.getId(ctx, "nutrient");
        return apply(ctx, player, nutrientId, op, "adjusted");
    }

    private static int apply(CommandContext<CommandSourceStack> ctx,
                             ServerPlayer player,
                             Identifier nutrientId,
                             UnaryOperator<Float> op,
                             String verb) {
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        NutrientDefinition def = registry.getValue(nutrientId);
        if (def == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown nutrient: " + nutrientId));
            return 0;
        }
        PlayerNutritionData current = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        float currentValue = current.get(nutrientId, def.defaultValue());
        float newValue = PlayerNutritionData.clamp(op.apply(currentValue));
        PlayerNutritionData updated = current.withValue(nutrientId, newValue);
        player.setData(NutritionalAttachments.PLAYER_NUTRITION, updated);
        NutritionalNetwork.sendDelta(player, Map.of(nutrientId, newValue));
        ctx.getSource().sendSuccess(() -> Component.literal(
                verb + " " + nutrientId + " on " + player.getName().getString() + " -> " + String.format("%.2f", newValue)), true);
        return Math.round(newValue);
    }

    private static int executeResetOne(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Identifier nutrientId = IdentifierArgument.getId(ctx, "nutrient");
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        NutrientDefinition def = registry.getValue(nutrientId);
        if (def == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown nutrient: " + nutrientId));
            return 0;
        }
        float def0 = def.defaultValue();
        PlayerNutritionData updated = player.getData(NutritionalAttachments.PLAYER_NUTRITION).withValue(nutrientId, def0);
        player.setData(NutritionalAttachments.PLAYER_NUTRITION, updated);
        NutritionalNetwork.sendDelta(player, Map.of(nutrientId, def0));
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset " + nutrientId + " on " + player.getName().getString()), true);
        return 1;
    }

    private static int executeResetAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        Map<Identifier, Float> reset = new HashMap<>();
        for (Holder.Reference<NutrientDefinition> ref : registry.listElements().toList()) {
            reset.put(ref.key().identifier(), ref.value().defaultValue());
        }
        PlayerNutritionData updated = player.getData(NutritionalAttachments.PLAYER_NUTRITION).withValues(reset);
        player.setData(NutritionalAttachments.PLAYER_NUTRITION, updated);
        NutritionalNetwork.sendFullSync(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset all nutrients on " + player.getName().getString()), true);
        return reset.size();
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((v, err) -> {
            if (err != null) {
                Nutritional.LOGGER.error("Reload failed", err);
                ctx.getSource().sendFailure(Component.literal("Reload failed: " + err.getMessage()));
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("Datapacks reloaded."), true);
            }
        });
        return 1;
    }
}
