package com.synkra.fawemixinmod.tacz.event;

import com.synkra.fawemixinmod.FaweMixinMod;
import com.synkra.fawemixinmod.tacz.network.message.KillFeedMessage;
import com.tacz.guns.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FaweMixinMod.MODID)
public class KillFeedEvent {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查死亡实体是否为生物实体
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity killedEntity = event.getEntity();
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof ServerPlayer killerPlayer) {
            // 获取击杀者主手物品
            ItemStack weapon = killerPlayer.getMainHandItem();
            // 发送击杀播报包给所有玩家
            String killerName = killerPlayer.getDisplayName().getString();
            String victimName = killedEntity.getDisplayName().getString();
            KillFeedMessage packet = new KillFeedMessage(killerName, victimName, weapon, true);
            NetworkHandler.sendToAllPlayers(packet);
        } else if (event.getSource().getEntity() instanceof LivingEntity killerEntity) {
            // 非玩家实体击杀玩家的情况
            if (killedEntity instanceof ServerPlayer victimPlayer) {
                ItemStack weapon = killerEntity.getMainHandItem();
                String killerName = killerEntity.getDisplayName().getString();
                String victimName = victimPlayer.getDisplayName().getString();
                KillFeedMessage packet = new KillFeedMessage(killerName, victimName, weapon, false);
                NetworkHandler.sendToAllPlayers(packet);
            }
        }
    }
}
