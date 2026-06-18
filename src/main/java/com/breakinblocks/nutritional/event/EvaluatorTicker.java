package com.breakinblocks.nutritional.event;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.DietTierEvaluator;
import com.breakinblocks.nutritional.common.NutritionEvaluator;
import com.breakinblocks.nutritional.common.PlayerModifierScopes;
import com.breakinblocks.nutritional.common.SustainedRewardEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class EvaluatorTicker {

    private static final int RE_EVAL_INTERVAL = 110;

    private EvaluatorTicker() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player raw = event.getEntity();
        if (raw.level().isClientSide()) return;
        if (!(raw instanceof ServerPlayer player)) return;
        if (player.tickCount % RE_EVAL_INTERVAL == 0) {
            ProfilerFiller profiler = Profiler.get();
            profiler.push("nutritional:evaluate");
            try {
                profiler.push("effects");
                NutritionEvaluator.evaluate(player);
                profiler.popPush("tier");
                DietTierEvaluator.evaluate(player);
                profiler.popPush("reward");
                SustainedRewardEvaluator.evaluate(player);
                profiler.pop();
            } finally {
                profiler.pop();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerModifierScopes.EFFECTS.clear(player);
            PlayerModifierScopes.TIER.clear(player);
        }
    }
}
