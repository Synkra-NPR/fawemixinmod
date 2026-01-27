package com.synkra.fawemixinmod.event;

import com.synkra.fawemixinmod.config.CommonConfig;
import com.synkra.fawemixinmod.network.message.ConfigSyncMessage;
import com.tacz.guns.network.NetworkHandler;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class ConfigSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 当玩家登录时，向客户端发送配置同步消息
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            sendConfigToClient(serverPlayer);
        }
    }

    private static void sendConfigToClient(ServerPlayer player) {
        // 获取当前配置值
        boolean enabledMixinGunRecipe = CommonConfig.ENABLED_MIXIN_GUN_RECIPE.get();
        boolean enabledGunRandom = CommonConfig.ENABLED_GUN_RANDOM.get();
        double gunDurabilityFactor = CommonConfig.GUN_DURABILITY_FACTOR.get();
        
        // 复制列表值以避免并发问题
        List<Integer> gunRarityColor = new ArrayList<>(CommonConfig.GUN_RARITY_COLOR.get());
        List<Double> gunRarityDistribution = new ArrayList<>(CommonConfig.GUN_RARITY_DISTRIBUTION.get());
        List<Double> gunDamageModifier = new ArrayList<>(CommonConfig.GUN_DAMAGE_MODIFIER.get());
        List<String> gunBoxBlackList = new ArrayList<>(CommonConfig.GUN_BOX_BLACK_LIST.get());

        // 发送配置同步消息到客户端
        NetworkHandler.sendToClientPlayer(new ConfigSyncMessage(
            enabledMixinGunRecipe,
            enabledGunRandom,
            gunDurabilityFactor,
            gunRarityColor,
            gunRarityDistribution,
            gunDamageModifier,
            gunBoxBlackList
        ),player);
    }
}