package com.breakinblocks.nutritional.event;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class DecayTicker {

    private static final Map<UUID, Integer> lastFoodLevel = new WeakHashMap<>();

    private DecayTicker() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player rawPlayer = event.getEntity();
        if (rawPlayer.level().isClientSide()) return;
        if (!(rawPlayer instanceof ServerPlayer player)) return;

        int currentFood = player.getFoodData().getFoodLevel();
        Integer previous = lastFoodLevel.put(player.getUUID(), currentFood);
        if (previous == null || previous <= currentFood) return;

        int foodDrop = previous - currentFood;
        applyDecay(player, foodDrop);
    }

    private static void applyDecay(ServerPlayer player, int foodDrop) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("nutritional:decay");
        try {
            Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(player.level().registryAccess());
            PlayerNutritionData current = player.getData(NutritionalAttachments.PLAYER_NUTRITION);

            Map<Identifier, Float> updates = new HashMap<>();
            for (Holder.Reference<NutrientDefinition> ref : registry.listElements().toList()) {
                Identifier id = ref.key().identifier();
                NutrientDefinition def = ref.value();
                float now = current.get(id, def.defaultValue());
                float next = NutritionalLogic.applyDecay(now, foodDrop, def, player);
                if (next != now) updates.put(id, next);
            }

            if (!updates.isEmpty()) {
                player.setData(NutritionalAttachments.PLAYER_NUTRITION, current.withValues(updates));
                NutritionalNetwork.sendDelta(player, updates);
            }
        } finally {
            profiler.pop();
        }
    }
}
