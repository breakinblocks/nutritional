package com.breakinblocks.nutritional.client.key;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.client.screen.NutritionScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class NutritionalKeybinds {

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Nutritional.id("nutritional"));

    public static final KeyMapping OPEN_NUTRITION = new KeyMapping(
            "key.nutritional.open_nutrition",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_N,
            CATEGORY
    );

    private NutritionalKeybinds() {}

    @EventBusSubscriber(modid = Nutritional.MOD_ID, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {}

        @SubscribeEvent
        public static void onRegister(RegisterKeyMappingsEvent event) {
            event.register(OPEN_NUTRITION);
        }
    }

    @EventBusSubscriber(modid = Nutritional.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {
        private GameBus() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;
            while (OPEN_NUTRITION.consumeClick()) {
                mc.setScreen(new NutritionScreen());
            }
        }
    }
}
