package com.breakinblocks.nutritional.command;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import com.breakinblocks.nutritional.userpack.recipe.FoodSetWriter;
import com.breakinblocks.nutritional.userpack.recipe.UpdateFoodsRunner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
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
                .requires(src -> src.hasPermission(2));

        root.then(Commands.literal("get")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .executes(NutritionalCommand::executeGet))));

        root.then(Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 100.0f))
                                        .executes(NutritionalCommand::executeSet)))));

        root.then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(ctx -> executeAdjust(ctx, v -> v + FloatArgumentType.getFloat(ctx, "amount")))))));

        root.then(Commands.literal("subtract")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(ctx -> executeAdjust(ctx, v -> v - FloatArgumentType.getFloat(ctx, "amount")))))));

        root.then(Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(NutritionalCommand::executeResetAll)
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .executes(NutritionalCommand::executeResetOne))));

        root.then(Commands.literal("reload")
                .executes(NutritionalCommand::executeReload));

        root.then(Commands.literal("food")
                .executes(NutritionalCommand::executeFoodInfo)
                .then(Commands.literal("get")
                        .executes(NutritionalCommand::executeFoodInfo))
                .then(Commands.literal("set")
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .executes(ctx -> FoodSetWriter.setOnHeld(ctx, ResourceLocationArgument.getId(ctx, "nutrient"), 1.0f))
                                .then(Commands.argument("scale", FloatArgumentType.floatArg(0.0f))
                                        .executes(ctx -> FoodSetWriter.setOnHeld(ctx,
                                                ResourceLocationArgument.getId(ctx, "nutrient"),
                                                FloatArgumentType.getFloat(ctx, "scale"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("nutrient", ResourceLocationArgument.id())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .executes(ctx -> FoodSetWriter.removeFromHeld(ctx,
                                        ResourceLocationArgument.getId(ctx, "nutrient"))))));

        root.then(Commands.literal("update-foods")
                .executes(UpdateFoodsRunner::run));

        dispatcher.register(root);
    }

    private static int executeFoodInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Hold an item in your main hand."));
            return 0;
        }

        List<ResourceLocation> nutrients = InvertedNutrientIndex.nutrientsFor(held.getItem());
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        String displayName = held.getHoverName().getString();

        if (nutrients.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(displayName + " (" + itemId + ") has no nutrient mapping.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        Map<ResourceLocation, Float> yields = NutritionalLogic.calculateNutrition(held, player);
        NutrientScales scales = BuiltInRegistries.ITEM.wrapAsHolder(held.getItem()).getData(NutritionalDataMaps.NUTRIENT_SCALES);

        ctx.getSource().sendSuccess(() -> Component.literal(displayName + " (" + itemId + "):")
                .withStyle(ChatFormatting.GREEN), false);
        for (ResourceLocation nutrientId : nutrients) {
            float scale = scales != null ? scales.scaleFor(nutrientId) : 1.0f;
            float yield = yields.getOrDefault(nutrientId, 0.0f);
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "  " + nutrientId + "  scale " + String.format("%.2f", scale)
                            + "  yield " + String.format("%.2f", yield)), false);
        }
        return nutrients.size();
    }

    private static int executeGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation nutrientId = ResourceLocationArgument.getId(ctx, "nutrient");
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        NutrientDefinition def = registry.get(nutrientId);
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
        ResourceLocation nutrientId = ResourceLocationArgument.getId(ctx, "nutrient");
        float value = FloatArgumentType.getFloat(ctx, "value");
        return apply(ctx, player, nutrientId, v -> value, "set");
    }

    private static int executeAdjust(CommandContext<CommandSourceStack> ctx, UnaryOperator<Float> op) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation nutrientId = ResourceLocationArgument.getId(ctx, "nutrient");
        return apply(ctx, player, nutrientId, op, "adjusted");
    }

    private static int apply(CommandContext<CommandSourceStack> ctx,
                             ServerPlayer player,
                             ResourceLocation nutrientId,
                             UnaryOperator<Float> op,
                             String verb) {
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        NutrientDefinition def = registry.get(nutrientId);
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
        ResourceLocation nutrientId = ResourceLocationArgument.getId(ctx, "nutrient");
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(ctx.getSource().registryAccess());
        NutrientDefinition def = registry.get(nutrientId);
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
        Map<ResourceLocation, Float> reset = new HashMap<>();
        for (Holder.Reference<NutrientDefinition> ref : registry.holders().toList()) {
            reset.put(ref.key().location(), ref.value().defaultValue());
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
