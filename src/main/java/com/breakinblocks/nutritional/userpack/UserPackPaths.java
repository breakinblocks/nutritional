package com.breakinblocks.nutritional.userpack;

import com.breakinblocks.nutritional.Nutritional;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class UserPackPaths {

    public static final String PACK_ID = "nutritional/config";

    private UserPackPaths() {}

    public static Path root() {
        return FMLPaths.CONFIGDIR.get().resolve(Nutritional.MOD_ID);
    }

    public static Path packMcmeta() {
        return root().resolve("pack.mcmeta");
    }

    public static Path readme() {
        return root().resolve("README.md");
    }

    public static Path dataRoot() {
        return root().resolve("data");
    }

    public static Path namespaceRoot() {
        return dataRoot().resolve(Nutritional.MOD_ID);
    }

    public static Path tagFile(ResourceLocation nutrientId) {
        return dataRoot().resolve(Nutritional.MOD_ID)
                .resolve("tags").resolve("items").resolve("nutrient")
                .resolve(nutrientId.getPath() + ".json");
    }

    public static Path scalesFile() {
        return dataRoot().resolve(Nutritional.MOD_ID)
                .resolve("data_maps").resolve("item")
                .resolve("nutrient_scales.json");
    }
}
