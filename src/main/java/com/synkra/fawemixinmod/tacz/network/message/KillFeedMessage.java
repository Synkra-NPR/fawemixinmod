package com.synkra.fawemixinmod.tacz.network.message;

import com.synkra.fawemixinmod.tacz.client.gui.overlay.KillFeedOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record KillFeedMessage(String killerName, String victimName, ItemStack weapon, boolean isKillerPlayer) {
    public KillFeedMessage(String killerName, String victimName, ItemStack weapon, boolean isKillerPlayer) {
        this.killerName = killerName;
        this.victimName = victimName;
        this.weapon = weapon.copy();
        this.isKillerPlayer = isKillerPlayer;
    }

    public static void encode(KillFeedMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.killerName);
        buf.writeUtf(message.victimName);
        buf.writeItem(message.weapon);
        buf.writeBoolean(message.isKillerPlayer);
    }

    public static KillFeedMessage decode(FriendlyByteBuf buf) {
        String killerName = buf.readUtf();
        String victimName = buf.readUtf();
        ItemStack weapon = buf.readItem();
        boolean isKillerPlayer = buf.readBoolean();
        return new KillFeedMessage(killerName, victimName, weapon, isKillerPlayer);
    }

    public static void handle(KillFeedMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在客户端处理消息
            KillFeedOverlay.getInstance().addKillEntry(
                    message.killerName,
                    message.victimName,
                    message.weapon,
                    message.isKillerPlayer
            );
        });
        context.setPacketHandled(true);
    }
}