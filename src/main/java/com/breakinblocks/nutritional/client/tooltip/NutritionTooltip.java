package com.breakinblocks.nutritional.client.tooltip;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.common.InvertedNutrientIndex;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.datamap.NutrientScales;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

@EventBusSubscriber(modid = Nutritional.MOD_ID, value = Dist.CLIENT)
public final class NutritionTooltip {

    private NutritionTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!NutritionalConfig.CLIENT.tooltipEnabled.get()) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Identifier> applicable = InvertedNutrientIndex.nutrientsFor(stack.getItem());
        if (applicable.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        RegistryAccess access = mc.getConnection().registryAccess();
        Registry<NutrientDefinition> nutrients = NutritionalDatapack.nutrients(access);

        Optional<NutrientScales> scales = Optional.ofNullable(
                BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).getData(NutritionalDataMaps.NUTRIENT_SCALES));

        TreeMap<String, List<String>> byScale = new TreeMap<>();
        for (Identifier id : applicable) {
            NutrientDefinition def = nutrients.getValue(id);
            if (def == null || !def.visible()) continue;
            float scale = scales.map(s -> s.scaleFor(id)).orElse(1.0f);
            String scaleKey = String.format("%.2f", scale);
            String displayName = Component.translatable(translationKey(id)).getString();
            byScale.computeIfAbsent(scaleKey, k -> new ArrayList<>()).add(displayName);
        }
        if (byScale.isEmpty()) return;

        for (var entry : byScale.entrySet()) {
            String names = String.join(", ", entry.getValue());
            MutableComponent line = Component.literal("Nutrients: ").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal(names).withStyle(ChatFormatting.DARK_GREEN))
                    .append(Component.literal(" (" + entry.getKey() + "x)").withStyle(ChatFormatting.DARK_AQUA));
            event.getToolTip().add(line);
        }
    }

    private static String translationKey(Identifier id) {
        return "nutrient." + id.getNamespace() + "." + id.getPath();
    }
}
