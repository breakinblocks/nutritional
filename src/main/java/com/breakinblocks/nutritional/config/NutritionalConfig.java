package com.breakinblocks.nutritional.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class NutritionalConfig {

    public static final ModConfigSpec STARTUP_SPEC;
    public static final Startup STARTUP;

    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Startup, ModConfigSpec> startupPair = new ModConfigSpec.Builder().configure(Startup::new);
        STARTUP = startupPair.getLeft();
        STARTUP_SPEC = startupPair.getRight();

        Pair<Server, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();

        Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private NutritionalConfig() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, STARTUP_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class Startup {
        public final ModConfigSpec.BooleanValue userpackEnabled;

        Startup(ModConfigSpec.Builder b) {
            b.push("userpack");
            userpackEnabled = b.comment("Master toggle for the config/nutritional/ datapack source.",
                    "Disable to skip loading anything from that folder on server start.")
                    .define("enabled", true);
            b.pop();
        }
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

        public final ModConfigSpec.DoubleValue diminishingExponent;

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
            logMissingFood = b.comment("Log items referenced by nutrient/food_hint JSON that are not present in the item registry.")
                    .define("missing_food", false);
            logMissingNutrients = b.comment("Log food items that have no nutrient mapping.")
                    .define("missing_nutrients", false);
            b.pop();

            b.push("userpack");
            diminishingExponent = b.comment("Exponent for the diminishing-returns curve used by /nutritional update-foods.",
                    "Output scale = sum(input scales) / (input count ^ exponent) / output count.",
                    "Lower values (0.5) make recipes closer to additive; higher values (1.0) heavily damp them.")
                    .defineInRange("diminishing_exponent", 0.75, 0.1, 2.0);
            b.pop();
        }
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue guiEnabled;
        public final ModConfigSpec.BooleanValue guiButtonEnabled;
        public final ModConfigSpec.BooleanValue tooltipEnabled;
        public final ModConfigSpec.BooleanValue hudEnabled;

        Client(ModConfigSpec.Builder b) {
            b.push("ui");
            guiEnabled = b.define("gui_enabled", true);
            guiButtonEnabled = b.define("gui_button_enabled", true);
            tooltipEnabled = b.define("tooltip_enabled", true);
            hudEnabled = b.comment("Render the diet-tier HUD widget in the top-right corner.")
                    .define("hud_enabled", true);
            b.pop();
        }
    }
}
