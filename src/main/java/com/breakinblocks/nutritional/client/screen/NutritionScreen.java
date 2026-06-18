package com.breakinblocks.nutritional.client.screen;

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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NutritionScreen extends Screen {

    private Button hudToggle;

    private static final int ICON_COL = 22;
    private static final int NAME_COL = 70;
    private static final int BAR_WIDTH = 150;
    private static final int BAR_HEIGHT = 12;
    private static final int PCT_COL = 36;
    private static final int ROW_HEIGHT = 22;
    private static final int PANEL_PAD = 12;

    public NutritionScreen() {
        super(Component.translatable("screen.nutritional.title"));
    }

    @Override
    protected void init() {
        super.init();
        ClientPacketDistributor.sendToServer(new RequestNutritionPayload());

        hudToggle = Button.builder(hudButtonText(), b -> toggleHud())
                .pos(width / 2 - 105, height - 30)
                .size(120, 20)
                .build();
        addRenderableWidget(hudToggle);

        addRenderableWidget(
                Button.builder(Component.translatable("screen.nutritional.close"), b -> onClose())
                        .pos(width / 2 + 25, height - 30)
                        .size(80, 20)
                        .build());
    }

    private Component hudButtonText() {
        boolean enabled = NutritionalConfig.CLIENT.hudEnabled.get();
        return Component.translatable(enabled ? "screen.nutritional.hud.on" : "screen.nutritional.hud.off");
    }

    private void toggleHud() {
        boolean enabled = NutritionalConfig.CLIENT.hudEnabled.get();
        NutritionalConfig.CLIENT.hudEnabled.set(!enabled);
        hudToggle.setMessage(hudButtonText());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        RegistryAccess access = mc.getConnection().registryAccess();
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(access);

        List<NutrientRow> rows = new ArrayList<>();
        for (Holder.Reference<NutrientDefinition> ref : nutrients.listElements().toList()) {
            NutrientDefinition def = ref.value();
            if (!def.visible()) continue;
            rows.add(new NutrientRow(ref.key().identifier(), def));
        }
        if (rows.isEmpty()) {
            graphics.centeredText(font, Component.literal("No nutrients defined."), width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        PlayerNutritionData data = ClientNutritionCache.get();
        int panelWidth = ICON_COL + NAME_COL + BAR_WIDTH + PCT_COL + PANEL_PAD * 2;
        int panelHeight = rows.size() * ROW_HEIGHT + PANEL_PAD * 2 + 28;
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xC0000000);
        graphics.outline(x, y, panelWidth, panelHeight, 0xFFFFFFFF);

        graphics.centeredText(font, title, x + panelWidth / 2, y + 6, 0xFFFFFFFF);

        renderActiveTier(graphics, data, access, x + PANEL_PAD, y + 20);

        int rowY = y + 40;
        for (NutrientRow row : rows) {
            renderRow(graphics, row, data, x + PANEL_PAD, rowY);
            rowY += ROW_HEIGHT;
        }
    }

    private void renderActiveTier(GuiGraphicsExtractor graphics, PlayerNutritionData data, RegistryAccess access, int x, int y) {
        Optional<Identifier> tierId = data.currentTier();
        if (tierId.isEmpty()) return;
        Registry<DietTierDefinition> tiers = NutritionalDatapack.tiers(access);
        DietTierDefinition def = tiers.getValue(tierId.get());
        if (def == null) return;
        Component name = Component.translatable(def.display().name());
        Component label = Component.literal("Tier: ").append(name);
        graphics.text(font, label, x, y, def.display().color() | 0xFF000000, false);
    }

    private void renderRow(GuiGraphicsExtractor graphics, NutrientRow row, PlayerNutritionData data, int x, int y) {
        float value = data.get(row.id, row.def.defaultValue());

        ItemStack icon = new ItemStack(row.def.icon());
        if (!icon.isEmpty()) graphics.item(icon, x, y - 2);

        Component name = Component.translatable("nutrient." + row.id.getNamespace() + "." + row.id.getPath());
        graphics.text(font, name, x + ICON_COL, y + 2, 0xFFFFFFFF, false);

        int barX = x + ICON_COL + NAME_COL;
        int barY = y + 1;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF333333);
        int filled = (int) (BAR_WIDTH * Math.min(1.0f, Math.max(0.0f, value / 100.0f)));
        graphics.fill(barX, barY, barX + filled, barY + BAR_HEIGHT, row.def.color() | 0xFF000000);
        graphics.outline(barX, barY, BAR_WIDTH, BAR_HEIGHT, 0xFF000000);

        String pct = String.format("%.0f%%", value);
        graphics.text(font, pct, barX + BAR_WIDTH + 6, y + 2, 0xFFFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record NutrientRow(Identifier id, NutrientDefinition def) {}
}
