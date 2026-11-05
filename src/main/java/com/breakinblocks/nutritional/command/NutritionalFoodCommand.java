package com.breakinblocks.nutritional.command;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.FoodMappingAudit;
import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class NutritionalFoodCommand {

    private static final int CHAT_LIST_LIMIT = 50;

    private static final SuggestionProvider<CommandSourceStack> UNMAPPED_MOD_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    FoodMappingAudit.unmappedFoods(ctx.getSource().registryAccess()).stream()
                            .map(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace())
                            .distinct(),
                    builder);

    private NutritionalFoodCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nutritional-food")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(NutritionalFoodCommand::executeInfo)
                .then(Commands.literal("unmapped")
                        .executes(ctx -> executeUnmapped(ctx, Optional.empty()))
                        .then(Commands.argument("mod", StringArgumentType.word())
                                .suggests(UNMAPPED_MOD_SUGGESTIONS)
                                .executes(ctx -> executeUnmapped(ctx, Optional.of(StringArgumentType.getString(ctx, "mod")))))));
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

    private static int executeUnmapped(CommandContext<CommandSourceStack> ctx, Optional<String> mod) {
        List<Item> unmapped = FoodMappingAudit.unmappedFoods(ctx.getSource().registryAccess()).stream()
                .filter(item -> mod.map(m -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(m)).orElse(true))
                .toList();
        String scope = mod.map(m -> " from " + m).orElse("");
        if (unmapped.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No unmapped foods" + scope + "."), false);
            return 0;
        }

        MutableComponent message = Component.literal(unmapped.size() + " foods" + scope + " have no nutrient mapping:");
        for (Item item : unmapped.subList(0, Math.min(CHAT_LIST_LIMIT, unmapped.size()))) {
            message.append(Component.literal("\n" + BuiltInRegistries.ITEM.getKey(item) + " ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("(").append(new ItemStack(item).getHoverName()).append(")")
                            .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (unmapped.size() > CHAT_LIST_LIMIT) {
            message.append(Component.literal("\n...and " + (unmapped.size() - CHAT_LIST_LIMIT) + " more.")
                    .withStyle(ChatFormatting.YELLOW));
        }
        message.append(saveReport(ctx.getSource(), unmapped, mod));
        ctx.getSource().sendSuccess(() -> message, false);
        return unmapped.size();
    }

    private static Component saveReport(CommandSourceStack source, List<Item> unmapped, Optional<String> mod) {
        try {
            Path report = FoodMappingAudit.writeUnmappedReport(unmapped, mod);
            String where = source.getServer().isDedicatedServer() ? "on the server" : "in your game folder";
            return Component.literal("\nFull list saved to " + report + " " + where + ".").withStyle(ChatFormatting.YELLOW);
        } catch (IOException e) {
            Nutritional.LOGGER.error("Could not save the unmapped food list.", e);
            FoodMappingAudit.logUnmappedFoods(unmapped);
            return Component.literal("\nCould not save the full list, so it was written to the server log instead.")
                    .withStyle(ChatFormatting.RED);
        }
    }
}
