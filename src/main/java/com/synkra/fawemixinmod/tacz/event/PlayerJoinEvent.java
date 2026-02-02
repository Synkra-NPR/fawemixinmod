package com.synkra.fawemixinmod.tacz.event;

import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.network.message.GunRankingMessage;
import com.tacz.guns.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerJoinEvent {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // 确保这是服务端且实体是玩家
        if (event.getEntity() instanceof ServerPlayer player) {
            // 确保稀有度数据已经初始化
            if (!GunRankingManager.isInitialized()) {
                GunRankingManager.init();
            }

            // 发送稀有度数据包给玩家
            GunRankingMessage packet = new GunRankingMessage(GunRankingManager.getGunRarityMap());
            NetworkHandler.sendToClientPlayer(packet, player);
        }
    }
}