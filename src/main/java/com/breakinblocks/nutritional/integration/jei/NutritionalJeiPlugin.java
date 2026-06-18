package com.breakinblocks.nutritional.integration.jei;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

@JeiPlugin
public final class NutritionalJeiPlugin implements IModPlugin {

    private static final Identifier UID = Nutritional.id("jei");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        RegistryAccess access = mc.getConnection().registryAccess();
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(access);

        for (Item item : InvertedNutrientIndex.items()) {
            Component info = describe(item, nutrients);
            if (info == null) continue;
            registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, info);
        }
    }

    private static Component describe(Item item, Registry<NutrientDefinition> nutrients) {
        List<Identifier> ids = InvertedNutrientIndex.nutrientsFor(item);
        if (ids.isEmpty()) return null;

        Optional<NutrientScales> scales = Optional.ofNullable(
                BuiltInRegistries.ITEM.wrapAsHolder(item).getData(NutritionalDataMaps.NUTRIENT_SCALES));

        Component header = Component.translatable("jei.nutritional.info.header").withStyle(ChatFormatting.DARK_GREEN);
        StringBuilder body = new StringBuilder();
        boolean first = true;
        for (Identifier id : ids) {
            NutrientDefinition def = nutrients.getValue(id);
            if (def == null || !def.visible()) continue;
            float scale = scales.map(s -> s.scaleFor(id)).orElse(1.0f);
            String name = Component.translatable("nutrient." + id.getNamespace() + "." + id.getPath()).getString();
            if (!first) body.append(", ");
            body.append(name).append(" (").append(String.format("%.2fx", scale)).append(")");
            first = false;
        }
        if (body.isEmpty()) return null;
        return Component.empty()
                .append(header)
                .append(Component.literal("\n" + body));
    }
}
