package com.breakinblocks.nutritional.datagen.builtin;

import static com.breakinblocks.nutritional.datagen.builtin.ModCompatFoods.food;
import static com.breakinblocks.nutritional.datagen.builtin.ModCompatFoods.s;

final class CompatFarmersDelight {

    private CompatFarmersDelight() {}

    static void register() {
        farmersdelight();
        delightfulcreators();
        farmers_delight_christmas_editio();
    }

    private static void farmersdelight() {
        food("farmersdelight:apple_cider", s("fruit", 1.0f));
        food("farmersdelight:apple_pie", s("fruit", 0.5f), s("grain", 0.5f));
        food("farmersdelight:apple_pie_slice", s("fruit", 0.5f), s("grain", 0.5f));
        food("farmersdelight:bacon", s("protein", 1.0f));
        food("farmersdelight:bacon_and_eggs", s("protein", 1.0f));
        food("farmersdelight:bacon_sandwich", s("grain", 0.3173f), s("protein", 0.3889f), s("vegetable", 0.2938f));
        food("farmersdelight:baked_cod_stew", s("protein", 0.5647f), s("vegetable", 0.4353f));
        food("farmersdelight:beef_patty", s("protein", 1.0f));
        food("farmersdelight:beef_stew", s("protein", 0.4529f), s("vegetable", 0.5471f));
        food("farmersdelight:bone_broth", s("protein", 1.0f));
        food("farmersdelight:cabbage", s("vegetable", 1.0f));
        food("farmersdelight:cabbage_leaf", s("vegetable", 1.0f));
        food("farmersdelight:cabbage_rolls", s("grain", 0.1667f), s("vegetable", 0.8333f));
        food("farmersdelight:cake_slice", s("grain", 1.0f));
        food("farmersdelight:chicken_cuts", s("protein", 1.0f));
        food("farmersdelight:chicken_sandwich", s("grain", 0.3119f), s("protein", 0.3723f), s("vegetable", 0.3158f));
        food("farmersdelight:chicken_soup", s("protein", 0.4248f), s("vegetable", 0.5752f));
        food("farmersdelight:chocolate_pie", s("fruit", 0.3333f), s("grain", 0.6667f));
        food("farmersdelight:chocolate_pie_slice", s("fruit", 0.3333f), s("grain", 0.6667f));
        food("farmersdelight:cod_roll", s("grain", 0.5337f), s("protein", 0.4663f));
        food("farmersdelight:cod_slice", s("protein", 1.0f));
        food("farmersdelight:cooked_bacon", s("protein", 1.0f));
        food("farmersdelight:cooked_chicken_cuts", s("protein", 1.0f));
        food("farmersdelight:cooked_cod_slice", s("protein", 1.0f));
        food("farmersdelight:cooked_mutton_chops", s("protein", 1.0f));
        food("farmersdelight:cooked_rice", s("grain", 1.0f));
        food("farmersdelight:cooked_salmon_slice", s("protein", 1.0f));
        food("farmersdelight:dog_food", s("grain", 0.1932f), s("protein", 0.8068f));
        food("farmersdelight:dumplings", s("grain", 0.4753f), s("protein", 0.2571f), s("vegetable", 0.2677f));
        food("farmersdelight:egg_sandwich", s("grain", 0.3461f), s("protein", 0.6539f));
        food("farmersdelight:fish_stew", s("protein", 0.4123f), s("vegetable", 0.5877f));
        food("farmersdelight:fried_egg", s("protein", 1.0f));
        food("farmersdelight:fried_rice", s("grain", 0.5401f), s("vegetable", 0.4599f));
        food("farmersdelight:fruit_salad", s("fruit", 0.7103f), s("vegetable", 0.2897f));
        food("farmersdelight:gleaming_salad", s("vegetable", 1.0f));
        food("farmersdelight:glow_berry_custard", s("dairy", 0.2684f), s("fruit", 0.4269f), s("protein", 0.3047f));
        food("farmersdelight:grilled_salmon", s("fruit", 0.1549f), s("protein", 0.5345f), s("vegetable", 0.3105f));
        food("farmersdelight:ham", s("protein", 1.0f));
        food("farmersdelight:hamburger", s("grain", 0.235f), s("protein", 0.4056f), s("vegetable", 0.3594f));
        food("farmersdelight:honey_cookie", s("fruit", 0.4205f), s("grain", 0.5795f));
        food("farmersdelight:honey_glazed_ham", s("fruit", 0.3333f), s("protein", 0.6667f));
        food("farmersdelight:hot_cocoa", s("dairy", 0.1477f), s("fruit", 0.8523f));
        food("farmersdelight:kelp_roll", s("grain", 0.5221f), s("vegetable", 0.4779f));
        food("farmersdelight:kelp_roll_slice", s("grain", 0.5f), s("vegetable", 0.5f));
        food("farmersdelight:melon_juice", s("fruit", 1.0f));
        food("farmersdelight:melon_popsicle", s("fruit", 1.0f));
        food("farmersdelight:milk_bottle", s("dairy", 1.0f));
        food("farmersdelight:minced_beef", s("protein", 1.0f));
        food("farmersdelight:mixed_salad", s("vegetable", 1.0f));
        food("farmersdelight:mushroom_rice", s("grain", 0.4428f), s("protein", 0.2603f), s("vegetable", 0.2968f));
        food("farmersdelight:mutton_chops", s("protein", 1.0f));
        food("farmersdelight:mutton_wrap", s("grain", 0.3212f), s("protein", 0.3854f), s("vegetable", 0.2934f));
        food("farmersdelight:nether_salad", s("vegetable", 1.0f));
        food("farmersdelight:noodle_soup", s("grain", 0.2647f), s("protein", 0.5234f), s("vegetable", 0.2118f));
        food("farmersdelight:onion", s("vegetable", 1.0f));
        food("farmersdelight:onion_soup", s("dairy", 0.1051f), s("grain", 0.1957f), s("vegetable", 0.6991f));
        food("farmersdelight:pasta_with_meatballs", s("grain", 0.2487f), s("protein", 0.4844f), s("vegetable", 0.2669f));
        food("farmersdelight:pasta_with_mutton_chop", s("grain", 0.2508f), s("protein", 0.4755f), s("vegetable", 0.2737f));
        food("farmersdelight:pie_crust", s("dairy", 0.0929f), s("grain", 0.9071f));
        food("farmersdelight:pumpkin_pie_slice", s("grain", 0.5f), s("vegetable", 0.5f));
        food("farmersdelight:pumpkin_slice", s("vegetable", 1.0f));
        food("farmersdelight:pumpkin_soup", s("dairy", 0.0873f), s("protein", 0.2679f), s("vegetable", 0.6447f));
        food("farmersdelight:ratatouille", s("vegetable", 1.0f));
        food("farmersdelight:raw_pasta", s("grain", 0.7778f), s("protein", 0.2222f));
        food("farmersdelight:rice", s("grain", 1.0f));
        food("farmersdelight:rice_panicle", s("grain", 1.0f));
        food("farmersdelight:roast_chicken", s("protein", 1.0f));
        food("farmersdelight:roasted_mutton_chops", s("grain", 0.2476f), s("protein", 0.5084f), s("vegetable", 0.244f));
        food("farmersdelight:rotten_tomato", s("vegetable", 1.0f));
        food("farmersdelight:salmon_roll", s("grain", 0.5337f), s("protein", 0.4663f));
        food("farmersdelight:salmon_slice", s("protein", 1.0f));
        food("farmersdelight:shepherds_pie", s("grain", 1.0f));
        food("farmersdelight:shepherds_pie_block", s("grain", 0.3538f), s("protein", 0.2878f), s("vegetable", 0.3584f));
        food("farmersdelight:smoked_ham", s("protein", 1.0f));
        food("farmersdelight:squid_ink_pasta", s("grain", 0.2608f), s("protein", 0.5198f), s("vegetable", 0.2194f));
        food("farmersdelight:steak_and_potatoes", s("grain", 0.1844f), s("protein", 0.3796f), s("vegetable", 0.436f));
        food("farmersdelight:stuffed_potato", s("protein", 0.1203f), s("vegetable", 0.8797f));
        food("farmersdelight:stuffed_pumpkin", s("vegetable", 1.0f));
        food("farmersdelight:sweet_berry_cheesecake", s("dairy", 0.4f), s("fruit", 0.4f), s("grain", 0.2f));
        food("farmersdelight:sweet_berry_cheesecake_slice", s("dairy", 0.4f), s("fruit", 0.4f), s("grain", 0.2f));
        food("farmersdelight:sweet_berry_cookie", s("fruit", 0.3889f), s("grain", 0.6111f));
        food("farmersdelight:tomato", s("vegetable", 1.0f));
        food("farmersdelight:tomato_sauce", s("vegetable", 1.0f));
        food("farmersdelight:vegetable_noodles", s("grain", 0.235f), s("protein", 0.1228f), s("vegetable", 0.6422f));
        food("farmersdelight:vegetable_soup", s("vegetable", 1.0f));
        food("farmersdelight:wheat_dough", s("grain", 0.8224f), s("protein", 0.1776f));
    }

    private static void delightfulcreators() {
        food("delightfulcreators:baked_cod_stew_bucket", s("protein", 0.6667f), s("vegetable", 0.3333f));
        food("delightfulcreators:beef_stew_bucket", s("protein", 0.6667f), s("vegetable", 0.3333f));
        food("delightfulcreators:beetroot_soup_bucket", s("protein", 0.2f), s("vegetable", 0.8f));
        food("delightfulcreators:chicken_soup_bucket", s("protein", 0.6667f), s("vegetable", 0.3333f));
        food("delightfulcreators:fish_stew_bucket", s("protein", 0.6667f), s("vegetable", 0.3333f));
        food("delightfulcreators:mushroom_stew_bucket", s("protein", 0.3333f), s("vegetable", 0.6667f));
        food("delightfulcreators:noodle_soup_bucket", s("grain", 0.5714f), s("protein", 0.1429f), s("vegetable", 0.2857f));
        food("delightfulcreators:pumpkin_soup_bucket", s("protein", 0.2f), s("vegetable", 0.8f));
        food("delightfulcreators:rabbit_stew_bucket", s("protein", 0.6667f), s("vegetable", 0.3333f));
        food("delightfulcreators:vegetable_soup_bucket", s("protein", 0.2f), s("vegetable", 0.8f));
    }

    private static void farmers_delight_christmas_editio() {
        food("farmers_delight_christmas_editio:baked_ginger_bread_ma", s("grain", 0.5f), s("vegetable", 0.5f));
        food("farmers_delight_christmas_editio:black_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:blue_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:brown_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:candy_cane", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:candy_cane_and_chocolate_chip_ice_cream", s("dairy", 0.4122f), s("fruit", 0.5878f));
        food("farmers_delight_christmas_editio:candy_cane_bit_ice_cream", s("dairy", 0.6667f), s("fruit", 0.3333f));
        food("farmers_delight_christmas_editio:chocolate_bar", s("dairy", 0.2365f), s("fruit", 0.6243f), s("protein", 0.1391f));
        food("farmers_delight_christmas_editio:chocolate_chip_ice_cream", s("dairy", 0.4122f), s("fruit", 0.5878f));
        food("farmers_delight_christmas_editio:chocolate_coin", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:cinnamon", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:cinnamon_bun", s("fruit", 0.2714f), s("grain", 0.7286f));
        food("farmers_delight_christmas_editio:cinnamon_eggnog", s("dairy", 0.2963f), s("fruit", 0.3333f), s("protein", 0.3704f));
        food("farmers_delight_christmas_editio:crushed_candy_cane", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:cyan_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:eggnog", s("dairy", 0.2222f), s("protein", 0.7778f));
        food("farmers_delight_christmas_editio:gray_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:green_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:hot_cocoa_with_marshmallows", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:hot_cocoa_with_marshmallows_and_candy_cane", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:hot_cocoa_with_marshmallows_and_cinnamon", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:hot_cocoa_with_marshmallows_and_peppermint", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:light_blue_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:light_gray_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:lime_royal_frosting", s("fruit", 0.3333f), s("protein", 0.6667f));
        food("farmers_delight_christmas_editio:lime_sugar_cookie", s("fruit", 0.2778f), s("grain", 0.5f), s("protein", 0.2222f));
        food("farmers_delight_christmas_editio:magenta_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:marshmallow", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:orange_royal_frosting", s("fruit", 0.3333f), s("protein", 0.6667f));
        food("farmers_delight_christmas_editio:orange_sugar_cookie", s("fruit", 0.2778f), s("grain", 0.5f), s("protein", 0.2222f));
        food("farmers_delight_christmas_editio:pink_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:purplesugarcookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:red_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:vannila_ice_cream", s("dairy", 0.7531f), s("protein", 0.2469f));
        food("farmers_delight_christmas_editio:white_sugar_cookie", s("grain", 1.0f));
        food("farmers_delight_christmas_editio:wrapped_chocolate_coin", s("fruit", 1.0f));
        food("farmers_delight_christmas_editio:yellow_sugar_cookie", s("grain", 1.0f));
    }
}
