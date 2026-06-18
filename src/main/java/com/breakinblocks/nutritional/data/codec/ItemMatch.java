package com.breakinblocks.nutritional.data.codec;

import com.breakinblocks.nutritional.util.NutritionalStreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record ItemMatch(Optional<Holder<Item>> item,
                        Optional<TagKey<Item>> tag,
                        Optional<DataComponentExactPredicate> components) {

    public static final Codec<ItemMatch> CODEC = RecordCodecBuilder.<ItemMatch>create(instance -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().optionalFieldOf("item").forGetter(ItemMatch::item),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(ItemMatch::tag),
            DataComponentExactPredicate.CODEC.optionalFieldOf("components").forGetter(ItemMatch::components)
    ).apply(instance, ItemMatch::new)).validate(ItemMatch::validate);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMatch> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(Registries.ITEM)), ItemMatch::item,
            ByteBufCodecs.optional(NutritionalStreamCodecs.tagKey(Registries.ITEM)), ItemMatch::tag,
            ByteBufCodecs.optional(DataComponentExactPredicate.STREAM_CODEC), ItemMatch::components,
            ItemMatch::new
    );

    public boolean matches(ItemStack stack) {
        if (item.isPresent() && !stack.is(item.get())) return false;
        if (tag.isPresent() && !stack.is(tag.get())) return false;
        return components.map(p -> p.test(stack)).orElse(true);
    }

    private static DataResult<ItemMatch> validate(ItemMatch m) {
        if (m.item.isEmpty() && m.tag.isEmpty()) {
            return DataResult.error(() -> "ItemMatch must specify at least one of: item, tag");
        }
        return DataResult.success(m);
    }
}
