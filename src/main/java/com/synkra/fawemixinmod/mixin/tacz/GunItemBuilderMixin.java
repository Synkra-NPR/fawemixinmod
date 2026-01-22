package com.synkra.fawemixinmod.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.synkra.fawemixinmod.tacz.GunDurabilityManager;
import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.setDurability;
import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.setMaxDurability;
import static com.synkra.fawemixinmod.tacz.GunRankingManager.setGunRanking;

@Mixin(GunItemBuilder.class)
public abstract class GunItemBuilderMixin {
    @Shadow(remap = false)
    private ResourceLocation gunId;
    @Inject(method = "build",
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/api/item/IGun;setBulletInBarrel(Lnet/minecraft/world/item/ItemStack;Z)V",
                    shift = At.Shift.AFTER),
            remap = false
    )
    private void setThisGunRankingAndDurability(CallbackInfoReturnable<ItemStack> cir, @Local(name="gun") ItemStack gun){
        if (gun.getItem() instanceof IGun iGun) {
            // 设置默认耐久度和最大耐久度
            int initialDurability = GunDurabilityManager.calculateInitialDurability(this.gunId);
            setDurability(gun, initialDurability);
            setMaxDurability(gun, initialDurability);
            // 设置枪械分级
            if (iGun instanceof GunItemDataAccessor dataAccessor) {
                GunRarity rarity = GunRankingManager.getGunRarity(this.gunId);
                int ranking = GunRankingManager.getGunRankingValue(rarity);
                setGunRanking(gun, ranking);
            }
        }
    }
    @Inject(method = "forceBuild",
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/api/item/IGun;setBulletInBarrel(Lnet/minecraft/world/item/ItemStack;Z)V",
                    shift = At.Shift.AFTER),
            remap = false
    )
    private void setThisGunRankingAndDurability_2(CallbackInfoReturnable<ItemStack> cir, @Local(name="gun") ItemStack gun){
        if (gun.getItem() instanceof IGun iGun) {
            // 设置默认耐久度和最大耐久度
            int initialDurability = GunDurabilityManager.calculateInitialDurability(this.gunId);
            setDurability(gun, initialDurability);
            setMaxDurability(gun, initialDurability);
            // 设置枪械分级
            if (iGun instanceof GunItemDataAccessor) {
                GunRarity rarity = GunRankingManager.getGunRarity(this.gunId);
                int ranking = GunRankingManager.getGunRankingValue(rarity);
                setGunRanking(gun, ranking);
            }
        }
    }
}