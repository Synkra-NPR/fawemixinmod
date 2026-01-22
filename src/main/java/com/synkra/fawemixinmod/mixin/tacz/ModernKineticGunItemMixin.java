package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.util.DurabilityUtil;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModernKineticGunItem.class)
public class ModernKineticGunItemMixin {
    @ModifyVariable(
            method = "doBulletSpread",
            at = @At("HEAD"),
            ordinal = 1,  // inaccuracy
            argsOnly = true,
            remap = false
    )
    private float modifyPitchBasedOnGunData(float value,ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, Projectile projectile,
                                            int bulletCnt, float processedSpeed, float inaccuracy, float pitch, float yaw) {
        return value*DurabilityUtil.getInaccuracyMultiplier(gunItem);
    }
}