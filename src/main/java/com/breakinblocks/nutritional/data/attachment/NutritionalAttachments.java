package com.breakinblocks.nutritional.data.attachment;

import com.breakinblocks.nutritional.Nutritional;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class NutritionalAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nutritional.MOD_ID);

    public static final Supplier<AttachmentType<PlayerNutritionData>> PLAYER_NUTRITION = ATTACHMENT_TYPES.register(
            "player_nutrition",
            () -> AttachmentType.builder(() -> PlayerNutritionData.EMPTY)
                    .serialize(PlayerNutritionData.CODEC.fieldOf("nutrition"))
                    .copyOnDeath()
                    .build()
    );

    private NutritionalAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
