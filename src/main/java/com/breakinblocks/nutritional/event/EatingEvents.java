package com.breakinblocks.nutritional.event;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.NutritionEvaluator;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.ApplicationPhase;
import com.breakinblocks.nutritional.data.codec.FoodHintDef;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.NutritionalNetwork;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class EatingEvents {

    private EatingEvents() {}

    @SubscribeEvent
    public static void onStartUsing(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ApplicationPhase phase = phaseFor(event.getItem(), player);
        if (phase == ApplicationPhase.ON_RIGHT_CLICK) applyNutrients(player, event.getItem());
    }

    @SubscribeEvent
    public static void onFinishUsing(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        if (event.getItem().getItem() == Items.MILK_BUCKET) {
            NutritionEvaluator.evaluate(player);
            return;
        }
        ApplicationPhase phase = phaseFor(event.getItem(), player);
        if (phase == ApplicationPhase.FINISH_USING) applyNutrients(player, event.getItem());
    }

    private static void applyNutrients(ServerPlayer player, ItemStack stack) {
        Map<Identifier, Float> deltas = NutritionalLogic.calculateNutrition(stack, player);
        if (deltas.isEmpty()) return;

        Registry<NutrientDefinition> registry = NutritionalDatapack.nutrients(player.level().registryAccess());
        PlayerNutritionData current = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        PlayerNutritionData updated = current.adjust(deltas, id -> {
            NutrientDefinition def = registry.getValue(id);
            return def != null ? def.defaultValue() : 50.0f;
        });
        player.setData(NutritionalAttachments.PLAYER_NUTRITION, updated);
        NutritionalNetwork.sendDelta(player, deltaSnapshot(updated, current, deltas.keySet()));
        NutritionEvaluator.evaluate(player);
    }

    private static Map<Identifier, Float> deltaSnapshot(PlayerNutritionData updated,
                                                              PlayerNutritionData previous,
                                                              Set<Identifier> changedKeys) {
        Map<Identifier, Float> out = new HashMap<>();
        for (Identifier id : changedKeys) {
            float now = updated.get(id, 0.0f);
            if (Float.compare(now, previous.get(id, 0.0f)) != 0) out.put(id, now);
        }
        return out;
    }

    private static ApplicationPhase phaseFor(ItemStack stack, ServerPlayer player) {
        Optional<FoodHintDef> hint = NutritionalLogic.findFoodHint(stack, player.level().registryAccess());
        return hint.map(FoodHintDef::applicationPhase).orElse(ApplicationPhase.FINISH_USING);
    }
}
