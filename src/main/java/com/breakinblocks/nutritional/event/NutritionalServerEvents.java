package com.breakinblocks.nutritional.event;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.data.codec.NutritionEffectDef;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class NutritionalServerEvents {

    private NutritionalServerEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        rebuildAll(event.getServer());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            rebuildAll(event.getPlayerList().getServer());
        }
    }

    private static void rebuildAll(MinecraftServer server) {
        RegistryAccess access = server.registryAccess();
        validate(access);
        InvertedNutrientIndex.rebuild(access);
        Nutritional.LOGGER.info("Loaded {} nutrients, {} effects, {} food hints, {} diet tiers, {} sustained rewards.",
                NutritionalDatapack.nutrients(access).size(),
                NutritionalDatapack.effects(access).size(),
                NutritionalDatapack.foodHints(access).size(),
                NutritionalDatapack.tiers(access).size(),
                NutritionalDatapack.rewards(access).size());
        NeoForge.EVENT_BUS.post(new NutritionalDataReloadedEvent(access));
    }

    private static void validate(RegistryAccess access) {
        Registry<NutritionEffectDef> effects = NutritionalDatapack.effects(access);
        for (NutritionEffectDef def : effects) {
            for (ResourceKey<?> ref : def.nutrients()) {
                if (NutritionalDatapack.nutrients(access).get(ref.identifier()) == null) {
                    Nutritional.LOGGER.warn("Effect references unknown nutrient: {}", ref.identifier());
                }
            }
        }
    }
}
