package com.breakinblocks.nutritional.net;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.client.ClientNutritionCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record DeltaNutritionPayload(Map<Identifier, Float> updates) implements CustomPacketPayload {

    public static final Type<DeltaNutritionPayload> TYPE = new Type<>(Nutritional.id("delta_nutrition"));

    private static final StreamCodec<ByteBuf, Map<Identifier, Float>> VALUES_CODEC =
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.FLOAT);

    public static final StreamCodec<RegistryFriendlyByteBuf, DeltaNutritionPayload> STREAM_CODEC = StreamCodec.composite(
            VALUES_CODEC, DeltaNutritionPayload::updates,
            DeltaNutritionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(DeltaNutritionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNutritionCache.applyDelta(payload.updates));
    }
}
