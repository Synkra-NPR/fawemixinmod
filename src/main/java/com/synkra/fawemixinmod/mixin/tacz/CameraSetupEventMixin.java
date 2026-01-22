package com.synkra.fawemixinmod.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.synkra.fawemixinmod.tacz.util.DurabilityUtil;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.event.CameraSetupEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CameraSetupEvent.class)
public class CameraSetupEventMixin {
    @ModifyVariable(
            method = "initialCameraRecoil",
            at = @At("STORE"),
            remap = false, name = "aimingRecoilModifier")
    private static float modifyAimingRecoilModifier(float value) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player==null){
            return value;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun)) {
            return value;
        }
        return value * DurabilityUtil.getRecoilMultiplier(mainHandItem);
    }
}
