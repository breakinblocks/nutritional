package com.breakinblocks.nutritional.client.hud;

public final class HudPlacement {

    private static boolean active;
    private static int x;
    private static int y;

    private HudPlacement() {}

    public static void begin(int startX, int startY) {
        active = true;
        x = startX;
        y = startY;
    }

    public static void moveTo(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public static void end() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static int x() {
        return x;
    }

    public static int y() {
        return y;
    }
}
