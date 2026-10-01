package com.breakinblocks.nutritional.client.hud;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.client.ClientNutritionCache;
import com.breakinblocks.nutritional.client.key.NutritionalKeybinds;
import com.breakinblocks.nutritional.config.HudAnchor;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.codec.DietTierDefinition;
import com.breakinblocks.nutritional.data.codec.TierDisplay;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = Nutritional.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class TierHudOverlay {

    public static final ResourceLocation LAYER_ID = Nutritional.id("tier_hud");
    public static final int HEIGHT = 14;

    private static final int PADDING = 4;
    private static final int ICON_SIZE = 10;
    private static final int ICON_GAP = 4;
    private static final float ICON_SCALE = 0.625f;
    private static final int HINT_GAP = 2;
    private static final long VISIBLE_MILLIS = 10_000L;
    private static final long FADE_MILLIS = 1_500L;
    private static final int MIN_ALPHA = 5;

    private static Optional<ResourceLocation> lastTier;
    private static long tierChangedAt;

    private TierHudOverlay() {}

    public record Content(Component label, int color, Optional<Holder<Item>> icon) {}

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, TierHudOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            lastTier = null;
            return;
        }

        trackTierChange();

        boolean placing = HudPlacement.isActive();
        if (!placing && (!NutritionalConfig.CLIENT.hudEnabled.get() || mc.options.hideGui)) return;
        if (!placing && mc.screen != null && !(mc.screen instanceof ChatScreen)) return;

        Optional<Content> active = activeContent(mc);
        if (active.isEmpty() && !placing) return;
        Content content = active.orElseGet(TierHudOverlay::sampleContent);

        float alpha = placing ? 1.0f : visibility();
        if (alpha <= 0.0f) return;

        int width = width(mc.font, content);
        int x = placing ? HudPlacement.x() : resolveX(graphics.guiWidth(), width);
        int y = placing ? HudPlacement.y() : resolveY(graphics.guiHeight(), HEIGHT);
        draw(graphics, mc.font, content, x, y, alpha);
        hotkeyHint().ifPresent(hint -> drawHint(graphics, mc.font, hint, x, y, width, alpha));
    }

    private static void trackTierChange() {
        Optional<ResourceLocation> tier = ClientNutritionCache.get().currentTier();
        if (!Objects.equals(tier, lastTier)) {
            lastTier = tier;
            tierChangedAt = Util.getMillis();
        }
    }

    private static float visibility() {
        if (NutritionalConfig.CLIENT.hudAlwaysShow.get()) return 1.0f;
        long elapsed = Util.getMillis() - tierChangedAt;
        if (elapsed >= VISIBLE_MILLIS) return 0.0f;
        long fadeStart = VISIBLE_MILLIS - FADE_MILLIS;
        if (elapsed <= fadeStart) return 1.0f;
        return 1.0f - (float) (elapsed - fadeStart) / FADE_MILLIS;
    }

    private static Optional<Component> hotkeyHint() {
        if (NutritionalConfig.CLIENT.hudAlwaysShow.get() || NutritionalKeybinds.OPEN_NUTRITION.isUnbound()) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable("hud.nutritional.hotkey",
                NutritionalKeybinds.OPEN_NUTRITION.getTranslatedKeyMessage()));
    }

    private static void drawHint(GuiGraphics graphics, Font font, Component hint, int x, int y, int width, float alpha) {
        int hintWidth = font.width(hint);
        int hintX = clamp(x + (width - hintWidth) / 2, graphics.guiWidth(), hintWidth);
        int below = y + HEIGHT + HINT_GAP;
        int hintY = below + font.lineHeight <= graphics.guiHeight() ? below : y - HINT_GAP - font.lineHeight;
        graphics.drawString(font, hint, hintX, hintY, withAlpha(0xAAAAAA, alpha), true);
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = Math.max(MIN_ALPHA, Math.round(alpha * 255.0f));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static Optional<Content> activeContent(Minecraft mc) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return Optional.empty();

        Optional<ResourceLocation> tierId = ClientNutritionCache.get().currentTier();
        if (tierId.isEmpty()) return Optional.empty();

        Registry<DietTierDefinition> tiers = NutritionalDatapack.tiers(connection.registryAccess());
        DietTierDefinition tier = tiers.get(tierId.get());
        if (tier == null) return Optional.empty();

        TierDisplay display = tier.display();
        if (!display.hudVisible()) return Optional.empty();

        return Optional.of(new Content(Component.translatable(display.name()), display.color(), display.icon()));
    }

    private static Content sampleContent() {
        return new Content(Component.translatable("screen.nutritional.hud.place.sample"), 0xFFFFFF, Optional.empty());
    }

    public static Content contentForPlacement(Minecraft mc) {
        return activeContent(mc).orElseGet(TierHudOverlay::sampleContent);
    }

    public static int width(Font font, Content content) {
        return font.width(content.label()) + PADDING * 2 + (content.icon().isPresent() ? ICON_SIZE + ICON_GAP : 0);
    }

    public static int resolveX(int screenWidth, int elementWidth) {
        HudAnchor anchor = NutritionalConfig.CLIENT.hudAnchor.get();
        return clamp(anchor.baseX(screenWidth, elementWidth) + NutritionalConfig.CLIENT.hudOffsetX.get(), screenWidth, elementWidth);
    }

    public static int resolveY(int screenHeight, int elementHeight) {
        HudAnchor anchor = NutritionalConfig.CLIENT.hudAnchor.get();
        return clamp(anchor.baseY(screenHeight, elementHeight) + NutritionalConfig.CLIENT.hudOffsetY.get(), screenHeight, elementHeight);
    }

    public static int clamp(int position, int screenSize, int elementSize) {
        return Math.max(0, Math.min(position, Math.max(0, screenSize - elementSize)));
    }

    public static void draw(GuiGraphics graphics, Font font, Content content, int x, int y, float alpha) {
        int width = width(font, content);
        graphics.fill(x, y, x + width, y + HEIGHT, withAlpha(0x000000, alpha * 0xA0 / 255.0f));
        graphics.renderOutline(x, y, width, HEIGHT, withAlpha(content.color(), alpha));

        int textX = x + PADDING;
        if (content.icon().isPresent()) {
            ItemStack iconStack = new ItemStack(content.icon().get());
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            graphics.pose().pushPose();
            graphics.pose().scale(ICON_SCALE, ICON_SCALE, 1.0f);
            graphics.renderItem(iconStack, (int) ((x + PADDING) / ICON_SCALE), (int) ((y + 2) / ICON_SCALE));
            graphics.pose().popPose();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            textX += ICON_SIZE + ICON_GAP;
        }
        graphics.drawString(font, content.label(), textX, y + 3, withAlpha(content.color(), alpha), false);
    }
}
