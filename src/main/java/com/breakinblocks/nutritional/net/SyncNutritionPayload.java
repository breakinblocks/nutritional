package com.breakinblocks.nutritional.net;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.client.ClientNutritionCache;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record SyncNutritionPayload(PlayerNutritionData data) implements CustomPacketPayload {

    public static final Type<SyncNutritionPayload> TYPE = new Type<>(Nutritional.id("sync_nutrition"));

    private static final StreamCodec<ByteBuf, Map<Identifier, Float>> VALUES_CODEC =
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.FLOAT);

    private static final StreamCodec<ByteBuf, Set<Identifier>> REWARDS_CODEC =
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncNutritionPayload> STREAM_CODEC =
            StreamCodec.of(SyncNutritionPayload::encode, SyncNutritionPayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(SyncNutritionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientNutritionCache.set(payload.data));
    }

    private static void encode(RegistryFriendlyByteBuf buf, SyncNutritionPayload payload) {
        PlayerNutritionData data = payload.data;
        VALUES_CODEC.encode(buf, data.values());
        buf.writeVarInt(data.consecutiveBalancedDays());
        buf.writeLong(data.lastDayEvaluated());
        REWARDS_CODEC.encode(buf, data.earnedRewards());
        buf.writeBoolean(data.currentTier().isPresent());
        data.currentTier().ifPresent(rl -> Identifier.STREAM_CODEC.encode(buf, rl));
    }

    private static SyncNutritionPayload decode(RegistryFriendlyByteBuf buf) {
        Map<Identifier, Float> values = VALUES_CODEC.decode(buf);
        int days = buf.readVarInt();
        long lastDay = buf.readLong();
        Set<Identifier> rewards = REWARDS_CODEC.decode(buf);
        Optional<Identifier> tier = buf.readBoolean() ? Optional.of(Identifier.STREAM_CODEC.decode(buf)) : Optional.empty();
        return new SyncNutritionPayload(new PlayerNutritionData(values, days, lastDay, rewards, tier));
    }
}
