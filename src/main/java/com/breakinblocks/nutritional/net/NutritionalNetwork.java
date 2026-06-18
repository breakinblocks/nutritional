package com.breakinblocks.nutritional.net;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.attachment.NutritionalAttachments;
import com.breakinblocks.nutritional.data.attachment.PlayerNutritionData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;

@EventBusSubscriber(modid = Nutritional.MOD_ID)
public final class NutritionalNetwork {

    private NutritionalNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Nutritional.MOD_ID).versioned("1");
        registrar.playToClient(SyncNutritionPayload.TYPE, SyncNutritionPayload.STREAM_CODEC, SyncNutritionPayload::handleOnClient);
        registrar.playToClient(DeltaNutritionPayload.TYPE, DeltaNutritionPayload.STREAM_CODEC, DeltaNutritionPayload::handleOnClient);
        registrar.playToServer(RequestNutritionPayload.TYPE, RequestNutritionPayload.STREAM_CODEC, RequestNutritionPayload::handleOnServer);
    }

    public static void sendFullSync(ServerPlayer player) {
        PlayerNutritionData data = player.getData(NutritionalAttachments.PLAYER_NUTRITION);
        PacketDistributor.sendToPlayer(player, new SyncNutritionPayload(data));
    }

    public static void sendDelta(ServerPlayer player, Map<Identifier, Float> updates) {
        if (updates.isEmpty()) return;
        PacketDistributor.sendToPlayer(player, new DeltaNutritionPayload(updates));
    }
}
