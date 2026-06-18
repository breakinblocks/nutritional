package com.breakinblocks.nutritional.client.hud;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.client.ClientNutritionCache;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.codec.DietTierDefinition;
import com.breakinblocks.nutritional.data.codec.TierDisplay;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Optional;

@EventBusSubscriber(modid = Nutritional.MOD_ID, value = Dist.CLIENT)
public final class TierHudOverlay {

    public static final Identifier LAYER_ID = Nutritional.id("tier_hud");

    private TierHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, TierHudOverlay::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!NutritionalConfig.CLIENT.hudEnabled.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.getConnection() == null) return;

        Optional<Identifier> tierId = ClientNutritionCache.get().currentTier();
        if (tierId.isEmpty()) return;

        RegistryAccess access = mc.getConnection().registryAccess();
        Registry<DietTierDefinition> tiers = NutritionalDatapack.tiers(access);
        DietTierDefinition tier = tiers.getValue(tierId.get());
        if (tier == null) return;

        TierDisplay display = tier.display();
        if (!display.hudVisible()) return;

        Font font = mc.font;
        Component label = Component.translatable(display.name());
        int textWidth = font.width(label);
        int padding = 4;
        int height = 14;
        int iconSize = 10;
        int width = textWidth + padding * 2 + (display.icon().isPresent() ? iconSize + 4 : 0);

        int screenWidth = graphics.guiWidth();
        int x = screenWidth - width - 6;
        int y = 6;

        graphics.fill(x, y, x + width, y + height, 0xA0000000);
        graphics.outline(x, y, width, height, display.color() | 0xFF000000);

        int textX = x + padding;
        if (display.icon().isPresent()) {
            ItemStack iconStack = new ItemStack(display.icon().get());
            graphics.pose().pushMatrix();
            graphics.pose().scale(0.625f, 0.625f);
            graphics.item(iconStack, (int) ((x + padding) / 0.625f), (int) ((y + 2) / 0.625f));
            graphics.pose().popMatrix();
            textX += iconSize + 4;
        }
        graphics.text(font, label, textX, y + 3, display.color() | 0xFF000000, false);
    }
}
