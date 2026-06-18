package com.breakinblocks.nutritional.datagen.tags;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.datagen.builtin.ModCompatFoods;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public final class NutritionalItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {

    public static final TagKey<Item> NUTRIENT_FRUIT     = tag("fruit");
    public static final TagKey<Item> NUTRIENT_GRAIN     = tag("grain");
    public static final TagKey<Item> NUTRIENT_PROTEIN   = tag("protein");
    public static final TagKey<Item> NUTRIENT_VEGETABLE = tag("vegetable");
    public static final TagKey<Item> NUTRIENT_DAIRY     = tag("dairy");

    public NutritionalItemTagsProvider(PackOutput output,
                                       CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Registries.ITEM, lookup,
                item -> BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(),
                Nutritional.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(NUTRIENT_FRUIT).add(
                Items.APPLE, Items.MELON_SLICE, Items.GLISTERING_MELON_SLICE,
                Items.SWEET_BERRIES, Items.GLOW_BERRIES,
                Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
                Items.CHORUS_FRUIT, Items.PUMPKIN_PIE);

        tag(NUTRIENT_GRAIN).add(
                Items.BREAD, Items.COOKIE, Items.CAKE, Items.PUMPKIN_PIE);

        tag(NUTRIENT_PROTEIN).add(
                Items.BEEF, Items.COOKED_BEEF,
                Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.CHICKEN, Items.COOKED_CHICKEN,
                Items.MUTTON, Items.COOKED_MUTTON,
                Items.RABBIT, Items.COOKED_RABBIT,
                Items.COD, Items.COOKED_COD,
                Items.SALMON, Items.COOKED_SALMON,
                Items.TROPICAL_FISH, Items.PUFFERFISH,
                Items.EGG, Items.RABBIT_STEW, Items.MUSHROOM_STEW,
                Items.SUSPICIOUS_STEW);

        tag(NUTRIENT_VEGETABLE).add(
                Items.CARROT, Items.GOLDEN_CARROT,
                Items.POTATO, Items.BAKED_POTATO,
                Items.BEETROOT, Items.BEETROOT_SOUP,
                Items.DRIED_KELP, Items.PUMPKIN_PIE);

        tag(NUTRIENT_DAIRY).add(Items.MILK_BUCKET);

        for (ModCompatFoods.Entry entry : ModCompatFoods.entries()) {
            for (Identifier nutrient : entry.scales().keySet()) {
                getOrCreateRawBuilder(tagFor(nutrient)).addOptionalElement(entry.item());
            }
        }
    }

    private static TagKey<Item> tagFor(Identifier nutrient) {
        return tag(nutrient.getPath());
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Nutritional.MOD_ID, "nutrient/" + name));
    }
}
