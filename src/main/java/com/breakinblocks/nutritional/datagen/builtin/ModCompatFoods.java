package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.Nutritional;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModCompatFoods {

    public record Entry(String modId, ResourceLocation item, Map<ResourceLocation, Float> scales) {}

    record Scale(ResourceLocation nutrient, float value) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private ModCompatFoods() {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    static Scale s(String nutrient, float value) {
        return new Scale(Nutritional.id(nutrient), value);
    }

    static void food(String itemId, Scale... scales) {
        ResourceLocation item = ResourceLocation.parse(itemId);
        Map<ResourceLocation, Float> map = new LinkedHashMap<>();
        for (Scale scale : scales) {
            map.put(scale.nutrient(), scale.value());
        }
        ENTRIES.add(new Entry(item.getNamespace(), item, map));
    }

    static {
        food("create:builders_tea", s("dairy", 0.25f));
        food("create:chocolate_glazed_berries", s("fruit", 1.0f));
        food("create:honeyed_apple", s("fruit", 1.0f));
        food("create:sweet_roll", s("grain", 1.0f));

        food("enderio:enderios", s("dairy", 0.59460354f), s("grain", 0.59460354f));

        food("pneumaticcraft:chips", s("vegetable", 1.25f));
        food("pneumaticcraft:salmon_tempura", s("vegetable", 0.5f), s("grain", 0.7f), s("protein", 1.2f));
        food("pneumaticcraft:sourdough_bread", s("fruit", 0.015625f));
        food("pneumaticcraft:cod_n_chips", s("protein", 1.0f));

        food("excessive_utilities:magical_apple", s("fruit", 2.0f));

        food("pickletweaks:diamond_apple", s("fruit", 1.0f));
        food("pickletweaks:emerald_apple", s("fruit", 1.0f));

        food("integrateddynamics:menril_berries", s("fruit", 1.0f));

        CompatFarmersDelight.register();
        CompatDelightAddons.register();
        CompatCroptopia.register();
        CompatHarvestCraft.register();
        CompatWorldMods.register();
        CompatPantryForBlockheads.register();
    }
}
