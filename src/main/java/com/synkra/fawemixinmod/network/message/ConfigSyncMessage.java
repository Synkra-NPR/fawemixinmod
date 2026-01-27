package com.synkra.fawemixinmod.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConfigSyncMessage {
    private final boolean enabledMixinGunRecipe;
    private final boolean enabledGunRandom;
    private final double gunDurabilityFactor;
    private final List<Integer> gunRarityColor;
    private final List<Double> gunRarityDistribution;
    private final List<Double> gunDamageModifier;
    private final List<String> gunBoxBlackList;

    public ConfigSyncMessage(boolean enabledMixinGunRecipe, boolean enabledGunRandom, 
                            double gunDurabilityFactor, List<Integer> gunRarityColor,
                            List<Double> gunRarityDistribution, List<Double> gunDamageModifier,
                            List<String> gunBoxBlackList) {
        this.enabledMixinGunRecipe = enabledMixinGunRecipe;
        this.enabledGunRandom = enabledGunRandom;
        this.gunDurabilityFactor = gunDurabilityFactor;
        this.gunRarityColor = gunRarityColor;
        this.gunRarityDistribution = gunRarityDistribution;
        this.gunDamageModifier = gunDamageModifier;
        this.gunBoxBlackList = gunBoxBlackList;
    }

    public static void encode(ConfigSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabledMixinGunRecipe);
        buf.writeBoolean(msg.enabledGunRandom);
        buf.writeDouble(msg.gunDurabilityFactor);

        buf.writeInt(msg.gunRarityColor.size());
        for (Integer color : msg.gunRarityColor) {
            buf.writeInt(color);
        }

        buf.writeInt(msg.gunRarityDistribution.size());
        for (Double distribution : msg.gunRarityDistribution) {
            buf.writeDouble(distribution);
        }

        buf.writeInt(msg.gunDamageModifier.size());
        for (Double modifier : msg.gunDamageModifier) {
            buf.writeDouble(modifier);
        }

        buf.writeInt(msg.gunBoxBlackList.size());
        for (String blackListEntry : msg.gunBoxBlackList) {
            buf.writeUtf(blackListEntry);
        }
    }

    public static ConfigSyncMessage decode(FriendlyByteBuf buf) {
        boolean enabledMixinGunRecipe = buf.readBoolean();
        boolean enabledGunRandom = buf.readBoolean();
        double gunDurabilityFactor = buf.readDouble();

        int colorSize = buf.readInt();
        List<Integer> gunRarityColor = new ArrayList<>();
        for (int i = 0; i < colorSize; i++) {
            gunRarityColor.add(buf.readInt());
        }

        int distributionSize = buf.readInt();
        List<Double> gunRarityDistribution = new ArrayList<>();
        for (int i = 0; i < distributionSize; i++) {
            gunRarityDistribution.add(buf.readDouble());
        }

        int modifierSize = buf.readInt();
        List<Double> gunDamageModifier = new ArrayList<>();
        for (int i = 0; i < modifierSize; i++) {
            gunDamageModifier.add(buf.readDouble());
        }

        int blackListSize = buf.readInt();
        List<String> gunBoxBlackList = new ArrayList<>();
        for (int i = 0; i < blackListSize; i++) {
            gunBoxBlackList.add(buf.readUtf());
        }

        return new ConfigSyncMessage(enabledMixinGunRecipe, enabledGunRandom, 
                                   gunDurabilityFactor, gunRarityColor, 
                                   gunRarityDistribution, gunDamageModifier, 
                                   gunBoxBlackList);
    }

    public static void handle(ConfigSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                // 更新客户端的配置
                com.synkra.fawemixinmod.config.CommonConfig.syncFromServer(
                    msg.enabledMixinGunRecipe,
                    msg.enabledGunRandom,
                    msg.gunDurabilityFactor,
                    msg.gunRarityColor,
                    msg.gunRarityDistribution,
                    msg.gunDamageModifier,
                    msg.gunBoxBlackList
                );
            });
            ctx.get().setPacketHandled(true);
        }
    }
}