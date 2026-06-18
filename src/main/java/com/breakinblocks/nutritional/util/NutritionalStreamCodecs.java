package com.breakinblocks.nutritional.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public final class NutritionalStreamCodecs {

    private NutritionalStreamCodecs() {}

    public static <T> StreamCodec<ByteBuf, TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> registry) {
        return Identifier.STREAM_CODEC.map(
                rl -> TagKey.create(registry, rl),
                TagKey::location
        );
    }

    public static <T> StreamCodec<ByteBuf, ResourceKey<T>> resourceKey(ResourceKey<? extends Registry<T>> registry) {
        return Identifier.STREAM_CODEC.map(
                rl -> ResourceKey.create(registry, rl),
                ResourceKey::identifier
        );
    }
}
