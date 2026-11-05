package com.breakinblocks.nutritional.integration.modtabs;

import com.breakinblocks.nutritional.client.screen.NutritionScreen;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

public final class NutritionTab extends SimpleItemTab {

    private NutritionTab() {
        super(new ItemStack(Items.APPLE));
    }

    public static void register() {
        TabsMenu.register(new NutritionTab());
    }

    @Override
    public void openTargetScreen(Player player) {
        NutritionScreen.openFromMenu(null);
    }

    @Override
    public boolean isEnabled(Player player) {
        return NutritionalConfig.CLIENT.guiEnabled.get() && NutritionalConfig.CLIENT.modTabsEnabled.get();
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
                .withDimensions(player -> NutritionScreen.PANEL_WIDTH, player -> NutritionScreen.panelHeight())
                .registerAllTabs(NutritionScreen.class);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen instanceof NutritionScreen;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("screen.nutritional.title");
    }
}
