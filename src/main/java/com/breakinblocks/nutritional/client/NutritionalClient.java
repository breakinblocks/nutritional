package com.breakinblocks.nutritional.client;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.integration.modtabs.NutritionTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class NutritionalClient {

    private static final String MOD_TABS = "modtabs";

    private NutritionalClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(NutritionalClient::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded(MOD_TABS)) {
            event.enqueueWork(NutritionalClient::registerModTab);
        }
    }

    private static void registerModTab() {
        try {
            NutritionTab.register();
        } catch (LinkageError e) {
            Nutritional.LOGGER.warn("Could not add the nutrition tab: this version of Mod Tabs is not compatible.", e);
        }
    }
}
