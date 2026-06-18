package com.breakinblocks.nutritional.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record ScaledItem(Holder<Item> item,
                         float scale,
                         Optional<DataComponentExactPredicate> components) {

    public static final Codec<ScaledItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ScaledItem::item),
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(ScaledItem::scale),
            DataComponentExactPredicate.CODEC.optionalFieldOf("components").forGetter(ScaledItem::components)
    ).apply(instance, ScaledItem::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScaledItem> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ScaledItem::item,
            ByteBufCodecs.FLOAT, ScaledItem::scale,
            ByteBufCodecs.optional(DataComponentExactPredicate.STREAM_CODEC), ScaledItem::components,
            ScaledItem::new
    );

    public boolean matches(ItemStack stack) {
        if (!stack.is(item)) return false;
        return components.map(p -> p.test(stack)).orElse(true);
    }
}
