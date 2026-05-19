package com.breakinblocks.nutritional.userpack.recipe;

import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.common.NutritionalLogic;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class EdibleItemScanner {

    private EdibleItemScanner() {}

    public static List<Item> scanForUnmapped(MinecraftServer server) {
        RegistryAccess access = server.registryAccess();
        List<Item> result = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!InvertedNutrientIndex.nutrientsFor(item).isEmpty()) continue;
            if (!isEdible(item, access)) continue;
            result.add(item);
        }
        return result;
    }

    public static boolean isEdible(Item item, RegistryAccess access) {
        ItemStack probe = new ItemStack(item);
        if (probe.has(DataComponents.FOOD)) return true;
        return NutritionalLogic.findFoodHint(probe, access).isPresent();
    }
}
