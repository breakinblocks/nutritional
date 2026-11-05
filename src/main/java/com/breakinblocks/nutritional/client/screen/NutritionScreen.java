package com.breakinblocks.nutritional.client.screen;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.client.ClientNutritionCache;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import com.breakinblocks.nutritional.data.codec.DietTierDefinition;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.breakinblocks.nutritional.net.RequestNutritionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Optional;

public final class NutritionScreen extends Screen {

    private static final int PANEL_WIDTH = 176;

    private static final Identifier PANEL_SPRITE = Nutritional.id("panel");
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");

    private static final int MIN_PANEL_HEIGHT = 166;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 20;
    private static final int ROW_HEIGHT = 22;
    private static final int SLOT_SIZE = 18;
    private static final int BAR_X = PADDING + SLOT_SIZE + 4;
    private static final int BAR_WIDTH = PANEL_WIDTH - PADDING - BAR_X;
    private static final int BAR_HEIGHT = 6;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 2;
    private static final int FOOTER_HEIGHT = BUTTON_HEIGHT + PADDING * 2;
    private static final int TIER_HEIGHT = 12;
    private static final int TIER_PADDING = 3;

    private static final int TEXT_COLOR = 0xFF404040;
    private static final int INSET_SHADOW = 0xFF373737;
    private static final int INSET_HIGHLIGHT = 0xFFFFFFFF;
    private static final int BAR_TRACK = 0xFF555555;
    private static final int DARK_TIER_LUMA = 100;

    private final Screen parent;

    private List<Holder.Reference<NutrientDefinition>> rows = List.of();
    private int panelHeight;
    private int left;
    private int top;
    private Button hudToggle;
    private Button hudMove;

    public NutritionScreen(Screen parent) {
        super(Component.translatable("screen.nutritional.title"));
        this.parent = parent;
    }

    public static void openFromMenu(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundContainerClosePacket(player.containerMenu.containerId));
            player.containerMenu = player.inventoryMenu;
        }
        mc.setScreen(new NutritionScreen(parent));
    }

    private static int panelHeight() {
        return Math.max(MIN_PANEL_HEIGHT, HEADER_HEIGHT + visibleNutrients().size() * ROW_HEIGHT + FOOTER_HEIGHT);
    }

    private static List<Holder.Reference<NutrientDefinition>> visibleNutrients() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return List.of();
        return NutritionalDatapack.nutrients(connection.registryAccess()).listElements()
                .filter(ref -> ref.value().visible())
                .toList();
    }

    @Override
    protected void init() {
        super.init();
        ClientPacketDistributor.sendToServer(new RequestNutritionPayload());

        rows = visibleNutrients();
        panelHeight = panelHeight();
        left = (width - PANEL_WIDTH) / 2;
        top = (height - panelHeight) / 2;

        int buttonWidth = (PANEL_WIDTH - PADDING * 2 - BUTTON_GAP * 2) / 3;
        int buttonY = top + panelHeight - PADDING - BUTTON_HEIGHT;

        hudToggle = addRenderableWidget(Button.builder(hudButtonText(), b -> toggleHud())
                .pos(left + PADDING, buttonY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        hudMove = addRenderableWidget(Button.builder(Component.translatable("screen.nutritional.hud.move"),
                        b -> Minecraft.getInstance().setScreen(new HudPlacementScreen(this)))
                .pos(left + PADDING + buttonWidth + BUTTON_GAP, buttonY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
        hudMove.active = NutritionalConfig.CLIENT.hudEnabled.get();

        addRenderableWidget(Button.builder(Component.translatable("screen.nutritional.close"), b -> onClose())
                .pos(left + PADDING + (buttonWidth + BUTTON_GAP) * 2, buttonY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
    }

    private Component hudButtonText() {
        boolean enabled = NutritionalConfig.CLIENT.hudEnabled.get();
        return Component.translatable(enabled ? "screen.nutritional.hud.on" : "screen.nutritional.hud.off");
    }

    private void toggleHud() {
        boolean enabled = NutritionalConfig.CLIENT.hudEnabled.get();
        NutritionalConfig.CLIENT.hudEnabled.set(!enabled);
        NutritionalConfig.CLIENT_SPEC.save();
        hudToggle.setMessage(hudButtonText());
        hudMove.active = !enabled;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        extractTransparentBackground(graphics);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL_SPRITE, left, top, PANEL_WIDTH, panelHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        graphics.text(font, title, left + PADDING, top + 6, TEXT_COLOR, false);

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerNutritionData data = ClientNutritionCache.get();
            renderActiveTier(graphics, data, mc.getConnection().registryAccess());

            if (rows.isEmpty()) {
                Component empty = Component.literal("No nutrients defined.");
                graphics.text(font, empty, left + (PANEL_WIDTH - font.width(empty)) / 2, top + HEADER_HEIGHT + 6, TEXT_COLOR, false);
            }

            int rowY = top + HEADER_HEIGHT;
            for (Holder.Reference<NutrientDefinition> row : rows) {
                renderRow(graphics, row, data, rowY);
                rowY += ROW_HEIGHT;
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, partial);
    }

    private void renderActiveTier(GuiGraphicsExtractor graphics, PlayerNutritionData data, RegistryAccess access) {
        Optional<Identifier> tierId = data.currentTier();
        if (tierId.isEmpty()) return;
        DietTierDefinition def = NutritionalDatapack.tiers(access).getValue(tierId.get());
        if (def == null) return;

        Component name = Component.translatable(def.display().name());
        int color = def.display().color();
        int chipWidth = font.width(name) + TIER_PADDING * 2;
        int chipX = left + PANEL_WIDTH - PADDING - chipWidth;
        int chipY = top + 4;
        if (luma(color) >= DARK_TIER_LUMA) {
            renderInset(graphics, chipX, chipY, chipWidth, TIER_HEIGHT, INSET_SHADOW);
        }
        graphics.text(font, name, chipX + TIER_PADDING, chipY + 2, color | 0xFF000000, false);
    }

    private static int luma(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    private void renderRow(GuiGraphicsExtractor graphics, Holder.Reference<NutrientDefinition> row, PlayerNutritionData data, int y) {
        NutrientDefinition def = row.value();
        Identifier id = row.key().identifier();
        float value = data.get(id, def.defaultValue());

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, left + PADDING, y, SLOT_SIZE, SLOT_SIZE);
        ItemStack icon = new ItemStack(def.icon());
        if (!icon.isEmpty()) graphics.item(icon, left + PADDING + 1, y + 1);

        int barX = left + BAR_X;
        Component name = Component.translatable("nutrient." + id.getNamespace() + "." + id.getPath());
        graphics.text(font, name, barX, y + 1, TEXT_COLOR, false);

        String percent = String.format("%.0f%%", value);
        graphics.text(font, percent, barX + BAR_WIDTH - font.width(percent), y + 1, TEXT_COLOR, false);

        int barY = y + SLOT_SIZE - BAR_HEIGHT;
        renderInset(graphics, barX, barY, BAR_WIDTH, BAR_HEIGHT, BAR_TRACK);
        int filled = (int) ((BAR_WIDTH - 2) * Math.min(1.0f, Math.max(0.0f, value / 100.0f)));
        graphics.fill(barX + 1, barY + 1, barX + 1 + filled, barY + BAR_HEIGHT - 1, def.color() | 0xFF000000);
    }

    private static void renderInset(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int fill) {
        graphics.fill(x, y, x + w, y + h, INSET_HIGHLIGHT);
        graphics.fill(x, y, x + w - 1, y + h - 1, INSET_SHADOW);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
