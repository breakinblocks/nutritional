package com.breakinblocks.nutritional.client.screen;

import com.breakinblocks.nutritional.client.hud.HudPlacement;
import com.breakinblocks.nutritional.client.hud.TierHudOverlay;
import com.breakinblocks.nutritional.config.HudAnchor;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudPlacementScreen extends Screen {

    private static final int SNAP_DISTANCE = 4;
    private static final int FINE_STEP = 1;
    private static final int COARSE_STEP = 10;
    private static final int PANEL_HEIGHT = 60;

    private final Screen parent;

    private int widgetWidth;
    private int widgetX;
    private int widgetY;
    private boolean dragging;
    private int grabX;
    private int grabY;

    public HudPlacementScreen(Screen parent) {
        super(Component.translatable("screen.nutritional.hud.place.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        widgetWidth = measureWidget();
        widgetX = TierHudOverlay.resolveX(width, widgetWidth);
        widgetY = TierHudOverlay.resolveY(height, TierHudOverlay.HEIGHT);
        HudPlacement.begin(widgetX, widgetY);

        addRenderableWidget(
                Button.builder(Component.translatable("screen.nutritional.hud.place.reset"), b -> reset())
                        .pos(width / 2 - 105, height - 28)
                        .size(100, 20)
                        .build());

        addRenderableWidget(
                Button.builder(Component.translatable("screen.nutritional.hud.place.done"), b -> onClose())
                        .pos(width / 2 + 5, height - 28)
                        .size(100, 20)
                        .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        int measured = measureWidget();
        if (measured != widgetWidth) {
            widgetWidth = measured;
            moveTo(widgetX, widgetY, false);
        }

        graphics.renderOutline(widgetX - 1, widgetY - 1, widgetWidth + 2, TierHudOverlay.HEIGHT + 2,
                dragging ? 0xFFFFFFFF : 0xFF909090);

        HudAnchor anchor = nearestAnchor();
        Component dragHint = Component.translatable("screen.nutritional.hud.place.hint.drag");
        Component keyHint = Component.translatable("screen.nutritional.hud.place.hint.keys");
        Component readout = Component.translatable("screen.nutritional.hud.place.readout",
                Component.translatable(anchor.translationKey()),
                widgetX - anchor.baseX(width, widgetWidth),
                widgetY - anchor.baseY(height, TierHudOverlay.HEIGHT));

        int panelWidth = Math.max(Math.max(font.width(title), font.width(dragHint)),
                Math.max(font.width(keyHint), font.width(readout))) + 16;
        int panelX = (width - panelWidth) / 2;
        int panelY = height - 28 - 6 - PANEL_HEIGHT;

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + PANEL_HEIGHT, 0xC0000000);
        graphics.renderOutline(panelX, panelY, panelWidth, PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.drawCenteredString(font, title, width / 2, panelY + 6, 0xFFFFFFFF);
        graphics.drawCenteredString(font, dragHint, width / 2, panelY + 20, 0xFFAAAAAA);
        graphics.drawCenteredString(font, keyHint, width / 2, panelY + 31, 0xFFAAAAAA);
        graphics.drawCenteredString(font, readout, width / 2, panelY + 45, 0xFFFFDD55);

        super.render(graphics, mouseX, mouseY, partial);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != InputConstants.MOUSE_BUTTON_LEFT || !overWidget(mouseX, mouseY)) return false;

        dragging = true;
        grabX = (int) mouseX - widgetX;
        grabY = (int) mouseY - widgetY;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        moveTo((int) mouseX - grabX, (int) mouseY - grabY, !hasShiftDown());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dragging) return super.mouseReleased(mouseX, mouseY, button);
        dragging = false;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int step = hasShiftDown() ? COARSE_STEP : FINE_STEP;
        return switch (keyCode) {
            case InputConstants.KEY_LEFT -> nudge(-step, 0);
            case InputConstants.KEY_RIGHT -> nudge(step, 0);
            case InputConstants.KEY_UP -> nudge(0, -step);
            case InputConstants.KEY_DOWN -> nudge(0, step);
            default -> super.keyPressed(keyCode, scanCode, modifiers);
        };
    }

    @Override
    public void onClose() {
        commit();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        HudPlacement.end();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean overWidget(double mouseX, double mouseY) {
        return mouseX >= widgetX && mouseX < widgetX + widgetWidth
                && mouseY >= widgetY && mouseY < widgetY + TierHudOverlay.HEIGHT;
    }

    private int measureWidget() {
        return TierHudOverlay.width(font, TierHudOverlay.contentForPlacement(Minecraft.getInstance()));
    }

    private boolean nudge(int dx, int dy) {
        moveTo(widgetX + dx, widgetY + dy, false);
        return true;
    }

    private void moveTo(int x, int y, boolean snap) {
        int placedX = TierHudOverlay.clamp(x, width, widgetWidth);
        int placedY = TierHudOverlay.clamp(y, height, TierHudOverlay.HEIGHT);

        if (snap) {
            for (HudAnchor anchor : HudAnchor.values()) {
                int baseX = anchor.baseX(width, widgetWidth);
                int baseY = anchor.baseY(height, TierHudOverlay.HEIGHT);
                if (Math.abs(placedX - baseX) <= SNAP_DISTANCE) placedX = baseX;
                if (Math.abs(placedY - baseY) <= SNAP_DISTANCE) placedY = baseY;
            }
        }

        widgetX = placedX;
        widgetY = placedY;
        HudPlacement.moveTo(widgetX, widgetY);
    }

    private void reset() {
        NutritionalConfig.CLIENT.hudAnchor.set(NutritionalConfig.CLIENT.hudAnchor.getDefault());
        NutritionalConfig.CLIENT.hudOffsetX.set(NutritionalConfig.CLIENT.hudOffsetX.getDefault());
        NutritionalConfig.CLIENT.hudOffsetY.set(NutritionalConfig.CLIENT.hudOffsetY.getDefault());
        NutritionalConfig.CLIENT_SPEC.save();

        widgetX = TierHudOverlay.resolveX(width, widgetWidth);
        widgetY = TierHudOverlay.resolveY(height, TierHudOverlay.HEIGHT);
        HudPlacement.moveTo(widgetX, widgetY);
    }

    private void commit() {
        HudAnchor anchor = nearestAnchor();
        NutritionalConfig.CLIENT.hudAnchor.set(anchor);
        NutritionalConfig.CLIENT.hudOffsetX.set(widgetX - anchor.baseX(width, widgetWidth));
        NutritionalConfig.CLIENT.hudOffsetY.set(widgetY - anchor.baseY(height, TierHudOverlay.HEIGHT));
        NutritionalConfig.CLIENT_SPEC.save();
    }

    private HudAnchor nearestAnchor() {
        return HudAnchor.nearest(widgetX, widgetY, widgetWidth, TierHudOverlay.HEIGHT, width, height);
    }
}
