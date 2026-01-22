package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.util.DurabilityUtil;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.InaccuracyModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.HashMap;
import java.util.List;

@Mixin(InaccuracyModifier.class)
public abstract class InaccuracyModifierMixin {
    @Unique
    private static final HashMap<Integer,Float> INACCURACY_MODIFIER_CACHE = new HashMap<>();
    @Inject(
            method = "getPropertyDiagramsData",
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/resource/pojo/data/gun/GunData;getFireModeAdjustData(Lcom/tacz/guns/api/item/gun/FireMode;)Lcom/tacz/guns/resource/pojo/data/gun/GunFireModeAdjustData;",
                    shift = At.Shift.AFTER),
            remap = false
    )
    public void putInaccuracyModifierCoefficientMap(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty, CallbackInfoReturnable<List<IAttachmentModifier.DiagramsData>> cir) {
        INACCURACY_MODIFIER_CACHE.put(gunData.hashCode(), DurabilityUtil.getInaccuracyMultiplier(gunItem));
    }
    @ModifyVariable(
            method = "buildNormal",
            at = @At("STORE"),
            remap = false, name = "inaccuracy")
    private float modifyNormalInaccuracy(float value,GunData gunData) {
        return value * INACCURACY_MODIFIER_CACHE.get(gunData.hashCode());
    }
    @ModifyVariable(
            method = "buildNormal",
            at = @At("STORE"),
            remap = false, name = "modifiedValue")
    private float modifyNormalModifiedValue(float value,GunData gunData) {
        return value * INACCURACY_MODIFIER_CACHE.get(gunData.hashCode());
    }
    @Inject(
            method = "getPropertyDiagramsData",
            at = @At("RETURN"),
            remap = false
    )
    public void removeInaccuracyModifierCoefficientMap(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty, CallbackInfoReturnable<List<IAttachmentModifier.DiagramsData>> cir) {
        INACCURACY_MODIFIER_CACHE.remove(gunData.hashCode());
    }
}