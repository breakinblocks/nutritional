package com.breakinblocks.nutritional.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class NutritionalConfig {

    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Server, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();

        Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private NutritionalConfig() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class Server {
        public final ModConfigSpec.DoubleValue nutritionMultiplier;
        public final ModConfigSpec.DoubleValue lossPerExtraNutrient;
        public final ModConfigSpec.BooleanValue allowOverEating;
        public final ModConfigSpec.DoubleValue startingDefault;

        public final ModConfigSpec.BooleanValue decayEnabled;
        public final ModConfigSpec.DoubleValue decayMultiplier;

        public final ModConfigSpec.DoubleValue deathPenaltyFloor;
        public final ModConfigSpec.DoubleValue deathPenaltyAmount;
        public final ModConfigSpec.BooleanValue deathPenaltyResetBelowFloor;

        public final ModConfigSpec.BooleanValue logMissingFood;
        public final ModConfigSpec.BooleanValue logMissingNutrients;

        Server(ModConfigSpec.Builder b) {
            b.push("nutrition");
            nutritionMultiplier = b.comment("Global multiplier for nutrient yield from food.")
                    .defineInRange("multiplier", 1.0, 0.0, 100.0);
            lossPerExtraNutrient = b.comment("Percentage of nutrient value lost per additional nutrient on the same food. 15 = 15% per extra.")
                    .defineInRange("loss_per_extra_nutrient", 15.0, 0.0, 100.0);
            allowOverEating = b.comment("Allow eating when hunger is full. Required for survival in peaceful mode.")
                    .define("allow_overeating", false);
            startingDefault = b.comment("Fallback starting value for a nutrient when its definition does not set one.")
                    .defineInRange("starting_default", 50.0, 0.0, 100.0);
            b.pop();

            b.push("decay");
            decayEnabled = b.comment("Master switch for nutrient decay.")
                    .define("enabled", true);
            decayMultiplier = b.comment("Global multiplier for decay rate. Stacks with per-nutrient decay and the nutrient_decay_rate attribute.")
                    .defineInRange("multiplier", 1.0, -100.0, 100.0);
            b.pop();

            b.push("death");
            deathPenaltyFloor = b.comment("Floor value below which the death penalty stops removing more.")
                    .defineInRange("penalty_floor", 30.0, 0.0, 100.0);
            deathPenaltyAmount = b.comment("Amount subtracted from each nutrient on death.")
                    .defineInRange("penalty_amount", 15.0, 0.0, 100.0);
            deathPenaltyResetBelowFloor = b.comment("If true, nutrients below the floor at death are raised to the floor.")
                    .define("penalty_reset_below_floor", true);
            b.pop();

            b.push("logging");
            logMissingFood = b.comment("Log items listed in nutrient or food_hint tags whose mod is installed but which do not exist, such as renamed or removed foods.")
                    .define("missing_food", false);
            logMissingNutrients = b.comment("Log food items that have no nutrient mapping.")
                    .define("missing_nutrients", false);
            b.pop();
        }
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue guiEnabled;
        public final ModConfigSpec.BooleanValue guiButtonEnabled;
        public final ModConfigSpec.IntValue guiButtonX;
        public final ModConfigSpec.IntValue guiButtonY;
        public final ModConfigSpec.BooleanValue modTabsEnabled;
        public final ModConfigSpec.BooleanValue tooltipEnabled;
        public final ModConfigSpec.BooleanValue tooltipShowMultipliers;
        public final ModConfigSpec.BooleanValue hudEnabled;
        public final ModConfigSpec.BooleanValue hudAlwaysShow;
        public final ModConfigSpec.EnumValue<HudAnchor> hudAnchor;
        public final ModConfigSpec.IntValue hudOffsetX;
        public final ModConfigSpec.IntValue hudOffsetY;

        Client(ModConfigSpec.Builder b) {
            b.push("ui");
            guiEnabled = b.comment("Allow the nutrition screen to open. When false, the hotkey, the inventory button and the Mod Tabs tab are all turned off.")
                    .define("gui_enabled", true);
            guiButtonEnabled = b.comment("Show the nutrition button next to the recipe book in the inventory.")
                    .define("gui_button_enabled", true);
            guiButtonX = b.comment("Horizontal position of the inventory button, in pixels from the left edge of the inventory.")
                    .defineInRange("gui_button_x", 126, -4096, 4096);
            guiButtonY = b.comment("Vertical position of the inventory button, in pixels from the middle of the screen. The recipe book button sits at -22.")
                    .defineInRange("gui_button_y", -22, -4096, 4096);
            modTabsEnabled = b.comment("Add a nutrition tab to the tab bar when Mod Tabs is installed.")
                    .define("mod_tabs_enabled", true);
            tooltipEnabled = b.comment("Show the food groups a food belongs to in its tooltip.")
                    .define("tooltip_enabled", true);
            tooltipShowMultipliers = b.comment("Also show each food group's multiplier in the tooltip, such as Grain (1.00x).")
                    .define("tooltip_show_multipliers", false);
            hudEnabled = b.comment("Render the diet-tier HUD widget.")
                    .define("hud_enabled", true);
            hudAlwaysShow = b.comment("Keep the diet-tier HUD widget on screen. When false it shows for 10 seconds after the tier changes, then fades out.")
                    .define("hud_always_show", false);
            hudAnchor = b.comment("Screen edge the diet-tier HUD widget is positioned relative to.")
                    .defineEnum("hud_anchor", HudAnchor.TOP_RIGHT);
            hudOffsetX = b.comment("Horizontal pixel offset from the anchor. Set in-game with the Move HUD button on the nutrition screen.")
                    .defineInRange("hud_offset_x", -6, -4096, 4096);
            hudOffsetY = b.comment("Vertical pixel offset from the anchor.")
                    .defineInRange("hud_offset_y", 6, -4096, 4096);
            b.pop();
        }
    }
}
