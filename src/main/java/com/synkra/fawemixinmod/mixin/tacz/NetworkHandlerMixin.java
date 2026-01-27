package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.network.message.ConfigSyncMessage;
import com.synkra.fawemixinmod.tacz.network.message.GunRankingMessage;
import com.synkra.fawemixinmod.tacz.network.message.KillFeedMessage;
import com.tacz.guns.network.NetworkHandler;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(NetworkHandler.class)
public class NetworkHandlerMixin {
@Final
    @Shadow(remap = false)
    public static SimpleChannel CHANNEL;
    @Final
    @Shadow(remap = false)
    private static AtomicInteger ID_COUNT;
    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/network/NetworkHandler;registerAcknowledge()V"
            ),
            remap = false
    )
    private static void injectCustomMessages(CallbackInfo ci) {
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), GunRankingMessage.class,
                GunRankingMessage::encode, GunRankingMessage::decode, GunRankingMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), KillFeedMessage.class,
                KillFeedMessage::encode, KillFeedMessage::decode, KillFeedMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ConfigSyncMessage.class,
                ConfigSyncMessage::encode, ConfigSyncMessage::decode, ConfigSyncMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
