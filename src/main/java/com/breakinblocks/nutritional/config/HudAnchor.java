package com.breakinblocks.nutritional.config;

import java.util.Locale;

public enum HudAnchor {
    TOP_LEFT(0.0f, 0.0f),
    TOP_CENTER(0.5f, 0.0f),
    TOP_RIGHT(1.0f, 0.0f),
    MIDDLE_LEFT(0.0f, 0.5f),
    MIDDLE_CENTER(0.5f, 0.5f),
    MIDDLE_RIGHT(1.0f, 0.5f),
    BOTTOM_LEFT(0.0f, 1.0f),
    BOTTOM_CENTER(0.5f, 1.0f),
    BOTTOM_RIGHT(1.0f, 1.0f);

    private final float horizontal;
    private final float vertical;

    HudAnchor(float horizontal, float vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public int baseX(int screenWidth, int elementWidth) {
        return Math.round((screenWidth - elementWidth) * horizontal);
    }

    public int baseY(int screenHeight, int elementHeight) {
        return Math.round((screenHeight - elementHeight) * vertical);
    }

    public String translationKey() {
        return "hud_anchor.nutritional." + name().toLowerCase(Locale.ROOT);
    }

    public static HudAnchor nearest(int x, int y, int elementWidth, int elementHeight, int screenWidth, int screenHeight) {
        HudAnchor closest = TOP_LEFT;
        long closestDistance = Long.MAX_VALUE;
        for (HudAnchor anchor : values()) {
            long dx = x - anchor.baseX(screenWidth, elementWidth);
            long dy = y - anchor.baseY(screenHeight, elementHeight);
            long distance = dx * dx + dy * dy;
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = anchor;
            }
        }
        return closest;
    }
}
