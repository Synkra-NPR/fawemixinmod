package com.synkra.fawemixinmod.mixin.tacz;


import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.synkra.fawemixinmod.tacz.util.DurabilityUtil;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.modifier.custom.RecoilModifier;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import com.tacz.guns.resource.pojo.data.gun.GunRecoilKeyFrame;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecoilModifier.class)
public class RecoilModifierMixin {
    @ModifyVariable(
            method = "getPropertyDiagramsData",
            at = @At("STORE"),
            remap = false, name = "pitch")
    @OnlyIn(Dist.CLIENT)
    private double modifyPitch(double value,ItemStack gunItem) {
        float multiplier = DurabilityUtil.getRecoilMultiplier(gunItem);
        return value * multiplier;
    }
    @ModifyVariable(
            method = "getPropertyDiagramsData",
            at = @At("STORE"),
            remap = false, name = "yaw")
    @OnlyIn(Dist.CLIENT)
    private double modifyYaw(double value,ItemStack gunItem) {
        float multiplier = DurabilityUtil.getRecoilMultiplier(gunItem);
        return value * multiplier;
    }
    @ModifyVariable(
            method = "getPropertyDiagramsData",
            at = @At("STORE"),
            remap = false, name = "modifiedPitch")
    @OnlyIn(Dist.CLIENT)
    private double modifyModifiedPitch(double value,ItemStack gunItem) {
        float multiplier = DurabilityUtil.getRecoilMultiplier(gunItem);
        return value * multiplier;
    }
    @ModifyVariable(
            method = "getPropertyDiagramsData",
            at = @At("STORE"),
            remap = false, name = "modifiedYaw")
    @OnlyIn(Dist.CLIENT)
    private double modifyModifiedYaw(double value,ItemStack gunItem) {
        float multiplier = DurabilityUtil.getRecoilMultiplier(gunItem);
        return value * multiplier;
    }
}
