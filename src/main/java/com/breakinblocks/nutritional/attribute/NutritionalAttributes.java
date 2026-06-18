package com.breakinblocks.nutritional.attribute;

import com.breakinblocks.nutritional.Nutritional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class NutritionalAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, Nutritional.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> NUTRIENT_ABSORPTION = ATTRIBUTES.register(
            "nutrient_absorption",
            () -> new RangedAttribute("attribute.nutritional.nutrient_absorption", 1.0, 0.0, 10.0).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> NUTRIENT_DECAY_RATE = ATTRIBUTES.register(
            "nutrient_decay_rate",
            () -> new RangedAttribute("attribute.nutritional.nutrient_decay_rate", 1.0, 0.0, 10.0).setSyncable(true)
    );

    private NutritionalAttributes() {}

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    @SubscribeEvent
    public static void onAttachAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, attr(NUTRIENT_ABSORPTION));
        event.add(EntityType.PLAYER, attr(NUTRIENT_DECAY_RATE));
    }

    private static Holder<Attribute> attr(DeferredHolder<Attribute, Attribute> holder) {
        return holder;
    }
}
