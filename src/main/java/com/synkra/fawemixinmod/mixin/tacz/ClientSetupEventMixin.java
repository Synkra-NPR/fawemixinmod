package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.client.gui.overlay.KillFeedOverlay;
import com.tacz.guns.client.init.ClientSetupEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ClientSetupEvent.class)
public class ClientSetupEventMixin {
    @Inject(
            method = "onRegisterGuiOverlays",
            at = @At("RETURN"),
            remap = false
    )
    private static void onRegisterGuiOverlaysTail(RegisterGuiOverlaysEvent event, CallbackInfo ci) {
        event.registerAboveAll("fawemixinmod_kill_feed_overlay", new KillFeedOverlay());
    }
}
