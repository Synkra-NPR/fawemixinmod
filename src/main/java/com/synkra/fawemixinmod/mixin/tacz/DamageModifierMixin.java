package com.synkra.fawemixinmod.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.DamageModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(DamageModifier.class)
public class DamageModifierMixin {
    @Unique
    private static final HashMap<Integer, Float> DAMAGE_MODIFIER_CACHE = new HashMap<>();
    @Inject(
            method = "getPropertyDiagramsData",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IGun;getFireMode(Lnet/minecraft/world/item/ItemStack;)Lcom/tacz/guns/api/item/gun/FireMode;"),
            remap = false)
    public void putInaccuracyModifierCoefficientMap(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty, CallbackInfoReturnable<List<IAttachmentModifier.DiagramsData>> cir, @Local(name="iGun")IGun iGun) {
        DAMAGE_MODIFIER_CACHE.put(gunItem.hashCode(), GunRankingManager.getDamageMultiplier(GunRankingManager.getGunRarity(iGun.getGunId(gunItem))));
    }
    @Inject(
            method = "getPropertyDiagramsData",
            at = @At("RETURN"),
            remap = false)
    public void removeInaccuracyModifierCoefficientMap(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty, CallbackInfoReturnable<List<IAttachmentModifier.DiagramsData>> cir) {
        DAMAGE_MODIFIER_CACHE.remove(gunItem.hashCode());
    }
    @ModifyVariable(
            method = "getPropertyDiagramsData",
            at=@At("STORE"),
            name="modifiedValue",
            remap = false)
    public float modifyDamageByRarity(float value,@Local(name="gunItem") ItemStack gunItem) {
        return value*DAMAGE_MODIFIER_CACHE.get(gunItem.hashCode());
    }
    @ModifyVariable(
            method = "getPropertyDiagramsData",
            at= @At(value = "STORE",
                    ordinal = 1),
            name="finalBase",
            remap = false)
    public float modifyDamageByRarity2(float value,@Local(name="gunItem")ItemStack gunItem) {
        return value*DAMAGE_MODIFIER_CACHE.get(gunItem.hashCode());
    }
}
