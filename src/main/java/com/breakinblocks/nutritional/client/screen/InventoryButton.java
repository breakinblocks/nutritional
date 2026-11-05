package com.breakinblocks.nutritional.client.screen;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Nutritional.MOD_ID, value = Dist.CLIENT)
public final class InventoryButton extends ImageButton {

    private static final WidgetSprites SPRITES = new WidgetSprites(
            Nutritional.id("inventory_button"), Nutritional.id("inventory_button_highlighted"));
    private static final int WIDTH = 20;
    private static final int HEIGHT = 18;

    private final InventoryScreen screen;

    private InventoryButton(InventoryScreen screen, Component label) {
        super(buttonX(screen), screen.height / 2 + NutritionalConfig.CLIENT.guiButtonY.get(), WIDTH, HEIGHT, SPRITES,
                b -> NutritionScreen.openFromMenu(new InventoryScreen(Minecraft.getInstance().player)), label);
        this.screen = screen;
        setTooltip(Tooltip.create(label));
    }

    private static int buttonX(InventoryScreen screen) {
        return screen.getGuiLeft() + NutritionalConfig.CLIENT.guiButtonX.get();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        setX(buttonX(screen));
        super.extractContents(graphics, mouseX, mouseY, partial);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        if (!NutritionalConfig.CLIENT.guiEnabled.get() || !NutritionalConfig.CLIENT.guiButtonEnabled.get()) return;
        event.addListener(new InventoryButton(screen, Component.translatable("screen.nutritional.title")));
    }
}
