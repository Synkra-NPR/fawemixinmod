package com.synkra.fawemixinmod.tacz.network.message;

import com.synkra.fawemixinmod.tacz.GunRarity;
import com.synkra.fawemixinmod.tacz.client.GunRankingClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record GunRankingMessage(Map<ResourceLocation, GunRarity> gunRarityMap) {
    public static void encode(GunRankingMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.gunRarityMap.size());
        for (Map.Entry<ResourceLocation, GunRarity> entry : message.gunRarityMap.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeEnum(entry.getValue());
        }
    }

    public static GunRankingMessage decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<ResourceLocation, GunRarity> gunRarityMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation gunId = buffer.readResourceLocation();
            GunRarity rarity = buffer.readEnum(GunRarity.class);
            gunRarityMap.put(gunId, rarity);
        }
        return new GunRankingMessage(gunRarityMap);
    }

    public static void handle(GunRankingMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleOnClient(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(GunRankingMessage message) {
        GunRankingClientCache.setGunRarities(message.gunRarityMap);
    }
}