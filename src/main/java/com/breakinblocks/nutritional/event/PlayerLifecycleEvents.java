package com.breakinblocks.nutritional.event;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import com.breakinblocks.nutritional.common.SustainedRewardEvaluator;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class PlayerLifecycleEvents {

    private PlayerLifecycleEvents() {}

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerNutritionData carried = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(player.level().registryAccess());

        Map<Identifier, Float> penalties = new HashMap<>();
        registry.listElements().toList().forEach(ref -> {
            Identifier id = ref.key().identifier();
            float now = carried.get(id, ref.value().defaultValue());
            float next = NutritionalLogic.applyDeathPenalty(now);
            if (next != now) penalties.put(id, next);
        });

        if (!penalties.isEmpty()) {
            player.setData(NutritionalAttachments.PLAYER_NUTRITION, carried.withValues(penalties));
        }
        SustainedRewardEvaluator.stripOnDeath(player);
        NutritionalNetwork.sendFullSync(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NutritionalNetwork.sendFullSync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NutritionalNetwork.sendFullSync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NutritionalNetwork.sendFullSync(player);
        }
    }
}
