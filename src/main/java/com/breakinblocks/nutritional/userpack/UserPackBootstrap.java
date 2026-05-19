package com.breakinblocks.nutritional.userpack;

import com.breakinblocks.nutritional.Nutritional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UserPackBootstrap {

    private static final String PACK_MCMETA =
            "{\n" +
            "  \"pack\": {\n" +
            "    \"description\": \"Nutritional user data\",\n" +
            "    \"pack_format\": 48\n" +
            "  }\n" +
            "}\n";

    private static final String README =
            "# Nutritional Config Datapack\n\n" +
            "This folder is loaded as a server datapack by the Nutritional mod. Files dropped here merge with the built-in defaults on every `/reload` or server start.\n\n" +
            "Supported content under `data/<namespace>/nutritional/`:\n\n" +
            "- `nutrient/<id>.json`            — define a new nutrient\n" +
            "- `effect/<id>.json`              — threshold-driven mob effect mapping\n" +
            "- `food_hint/<id>.json`           — overrides for non-vanilla edible items\n" +
            "- `diet_tier/<id>.json`           — diet tier with attribute modifiers\n" +
            "- `sustained_reward/<id>.json`    — long-streak rewards\n" +
            "- `dimension_modifier/<dim>.json` — per-dimension yield/decay multipliers\n" +
            "- `tags/items/nutrient/<id>.json` — items that belong to a nutrient\n" +
            "- `data_maps/item/nutrient_scales.json` — per-item scale overrides\n\n" +
            "Most users don't need to touch this folder directly. Use:\n\n" +
            "- `/nutritional food set <nutrient> [<scale>]` while holding an item — assigns nutrient(s) to that item.\n" +
            "- `/nutritional food remove <nutrient>`        — undoes a `set`.\n" +
            "- `/nutritional update-foods`                  — scans every edible item, derives nutrient scales from crafting recipes, writes results here, and logs anything it couldn't derive.\n\n" +
            "Entries written by those commands for items from optional mods (e.g. `farmersdelight:tomato`) are wrapped in a `neoforge:mod_loaded` condition so they silently disappear if that mod is uninstalled.\n";

    private UserPackBootstrap() {}

    public static void ensureDirectoryStructure() {
        Path root = UserPackPaths.root();
        try {
            Files.createDirectories(UserPackPaths.namespaceRoot());
            createIfMissing(UserPackPaths.packMcmeta(), PACK_MCMETA);
            createIfMissing(UserPackPaths.readme(), README);
        } catch (IOException e) {
            Nutritional.LOGGER.error("Failed to initialize {} config pack", root, e);
        }
    }

    private static void createIfMissing(Path target, String content) throws IOException {
        if (Files.exists(target)) return;
        Files.writeString(target, content);
    }
}
