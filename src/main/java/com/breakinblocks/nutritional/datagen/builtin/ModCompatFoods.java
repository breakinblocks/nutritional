package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.Nutritional;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModCompatFoods {

    public record Entry(String modId, ResourceLocation item, Map<ResourceLocation, Float> scales) {}

    private record Scale(ResourceLocation nutrient, float value) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private ModCompatFoods() {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static Scale s(String nutrient, float value) {
        return new Scale(Nutritional.id(nutrient), value);
    }

    private static void food(String itemId, Scale... scales) {
        ResourceLocation item = ResourceLocation.parse(itemId);
        Map<ResourceLocation, Float> map = new LinkedHashMap<>();
        for (Scale scale : scales) {
            map.put(scale.nutrient(), scale.value());
        }
        ENTRIES.add(new Entry(item.getNamespace(), item, map));
    }

    static {
        food("farmersdelight:apple_cider", s("fruit", 1.316074f));
        food("farmersdelight:wheat_dough", s("protein", 0.11785113f), s("grain", 0.35355338f));
        food("farmersdelight:raw_pasta", s("protein", 0.11785113f), s("grain", 0.35355338f));
        food("farmersdelight:minced_beef", s("protein", 0.5f));
        food("farmersdelight:beef_patty", s("protein", 0.5f));
        food("farmersdelight:chicken_cuts", s("protein", 0.5f));
        food("farmersdelight:cooked_chicken_cuts", s("protein", 0.5f));
        food("farmersdelight:bacon", s("protein", 0.5f));
        food("farmersdelight:cooked_bacon", s("protein", 0.5f));
        food("farmersdelight:cod_slice", s("protein", 0.5f));
        food("farmersdelight:cooked_cod_slice", s("protein", 0.5f));
        food("farmersdelight:salmon_slice", s("protein", 0.5f));
        food("farmersdelight:cooked_salmon_slice", s("protein", 0.5f));
        food("farmersdelight:mutton_chops", s("protein", 0.5f));
        food("farmersdelight:cooked_mutton_chops", s("protein", 0.5f));
        food("farmersdelight:pie_crust", s("grain", 1.316074f));
        food("farmersdelight:cake_slice", s("grain", 0.14285715f));
        food("farmersdelight:pumpkin_pie_slice", s("fruit", 0.25f), s("vegetable", 0.25f), s("grain", 0.25f));
        food("farmersdelight:sweet_berry_cookie", s("fruit", 0.05483642f), s("grain", 0.10967284f));
        food("farmersdelight:honey_cookie", s("fruit", 0.05483642f), s("grain", 0.10967284f));
        food("farmersdelight:melon_popsicle", s("fruit", 1.4142135f));
        food("farmersdelight:glow_berry_custard", s("dairy", 0.35355338f), s("fruit", 0.70710677f), s("protein", 0.35355338f));
        food("farmersdelight:fruit_salad", s("fruit", 1.4953488f));
        food("farmersdelight:egg_sandwich", s("protein", 0.8773827f), s("grain", 0.43869135f));
        food("farmersdelight:chicken_sandwich", s("protein", 0.43869135f), s("vegetable", 0.43869135f), s("grain", 0.43869135f));
        food("farmersdelight:hamburger", s("protein", 0.29730177f), s("grain", 0.59460354f));
        food("farmersdelight:bacon_sandwich", s("protein", 0.29730177f), s("grain", 0.59460354f));
        food("farmersdelight:mutton_wrap", s("protein", 0.59460354f), s("grain", 0.59460354f));
        food("farmersdelight:dumplings", s("protein", 0.33233914f), s("grain", 0.10511205f));
        food("farmersdelight:stuffed_potato", s("dairy", 0.43869135f), s("protein", 0.43869135f), s("vegetable", 0.43869135f));
        food("farmersdelight:salmon_roll", s("protein", 0.29730177f));
        food("farmersdelight:cod_roll", s("protein", 0.29730177f));
        food("farmersdelight:kelp_roll", s("vegetable", 1.4142135f));
        food("farmersdelight:kelp_roll_slice", s("vegetable", 0.47140452f));
        food("farmersdelight:beef_stew", s("protein", 0.43869135f), s("vegetable", 0.8773827f));
        food("farmersdelight:chicken_soup", s("protein", 0.43869135f), s("vegetable", 0.8773827f));
        food("farmersdelight:vegetable_soup", s("vegetable", 1.316074f));
        food("farmersdelight:fried_rice", s("protein", 0.59460354f), s("vegetable", 0.59460354f));
        food("farmersdelight:pumpkin_soup", s("dairy", 0.59460354f), s("protein", 0.59460354f));
        food("farmersdelight:baked_cod_stew", s("protein", 0.8773827f), s("vegetable", 0.43869135f));
        food("farmersdelight:noodle_soup", s("protein", 0.7487735f), s("vegetable", 0.35355338f), s("grain", 0.125f));
        food("farmersdelight:onion_soup", s("dairy", 0.59460354f), s("grain", 0.59460354f));
        food("farmersdelight:bacon_and_eggs", s("protein", 1.0606601f));
        food("farmersdelight:pasta_with_meatballs", s("protein", 0.36737648f), s("grain", 0.2102241f));
        food("farmersdelight:pasta_with_mutton_chop", s("protein", 0.6646783f), s("grain", 0.2102241f));
        food("farmersdelight:roasted_mutton_chops", s("protein", 0.29730177f), s("vegetable", 0.59460354f));
        food("farmersdelight:vegetable_noodles", s("protein", 0.05170027f), s("vegetable", 0.8773827f), s("grain", 0.15510081f));
        food("farmersdelight:steak_and_potatoes", s("protein", 0.59460354f), s("vegetable", 0.59460354f));
        food("farmersdelight:ratatouille", s("vegetable", 1.1892071f));
        food("farmersdelight:squid_ink_pasta", s("protein", 0.6646783f), s("grain", 0.2102241f));
        food("farmersdelight:grilled_salmon", s("fruit", 0.59460354f), s("protein", 0.59460354f));
        food("farmersdelight:dog_food", s("protein", 0.7432544f));
        food("farmersdelight:tomato_sauce", s("vegetable", 1.2f));
        food("farmersdelight:cabbage_leaf", s("vegetable", 0.4f));
        food("farmersdelight:smoked_ham", s("protein", 1.2f));
        food("farmersdelight:apple_pie_slice", s("fruit", 0.6f), s("grain", 0.5f), s("dairy", 0.3f));
        food("farmersdelight:sweet_berry_cheesecake_slice", s("fruit", 0.6f), s("grain", 0.4f), s("dairy", 0.7f));
        food("farmersdelight:bone_broth", s("fruit", 1.0f));
        food("farmersdelight:ham", s("protein", 1.0f));
        food("farmersdelight:fried_egg", s("protein", 1.0f));
        food("farmersdelight:barbecue_stick", s("protein", 1.0f));
        food("farmersdelight:cabbage_rolls", s("protein", 1.0f));
        food("farmersdelight:fish_stew", s("protein", 1.0f));
        food("farmersdelight:mixed_salad", s("vegetable", 1.0f));
        food("farmersdelight:mushroom_rice", s("vegetable", 1.0f));
        food("farmersdelight:cabbage", s("vegetable", 1.0f));
        food("farmersdelight:tomato", s("vegetable", 1.0f));
        food("farmersdelight:onion", s("vegetable", 1.0f));
        food("farmersdelight:pumpkin_slice", s("vegetable", 1.0f));

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
    }
}
