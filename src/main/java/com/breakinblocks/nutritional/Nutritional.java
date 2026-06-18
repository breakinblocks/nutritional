package com.breakinblocks.nutritional;

import com.breakinblocks.nutritional.advancement.NutritionalCriteria;
import com.breakinblocks.nutritional.attribute.NutritionalAttributes;
import com.breakinblocks.nutritional.client.NutritionalClient;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.datamap.NutritionalDataMaps;
import com.breakinblocks.nutritional.data.registry.NutritionalRegistries;
import com.breakinblocks.nutritional.effect.NutritionalMobEffects;
import com.breakinblocks.nutritional.util.NutritionalId;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Nutritional.MOD_ID)
public final class Nutritional {

    public static final String MOD_ID = "nutritional";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Nutritional(IEventBus modBus, ModContainer container, Dist dist) {
        NutritionalRegistries.register(modBus);
        NutritionalDataMaps.register(modBus);
        NutritionalAttachments.register(modBus);
        NutritionalAttributes.register(modBus);
        NutritionalMobEffects.register(modBus);
        NutritionalCriteria.register(modBus);
        NutritionalConfig.register(container);

        if (dist.isClient()) {
            NutritionalClient.init(modBus);
        }

        LOGGER.info("{} initialized.", MOD_ID);
    }

    public static Identifier id(String path) {
        return NutritionalId.of(path);
    }
}
