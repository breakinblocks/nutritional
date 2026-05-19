package com.breakinblocks.nutritional.command;

import com.breakinblocks.nutritional.Nutritional;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class NutritionalCommands {

    private NutritionalCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        NutritionalCommand.register(event.getDispatcher());
    }
}
