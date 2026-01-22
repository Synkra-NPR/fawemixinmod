package com.synkra.fawemixinmod.mixin.tacz;

import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getDurability;
import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.reduceDurability;

@Mixin(LivingEntityShoot.class)
public class LivingEntityShootMixin {
    @Final
    @Shadow(remap = false)
    private ShooterDataHolder data;
    @Inject(method = "shoot",
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/api/entity/IGunOperator;fromLivingEntity(Lnet/minecraft/world/entity/LivingEntity;)Lcom/tacz/guns/api/entity/IGunOperator;"
            ),
            remap = false,
            cancellable = true)
    public void checkDurability(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, CallbackInfoReturnable<ShootResult> cir) {
        if (data.currentGunItem != null && getDurability(data.currentGunItem.get()) <= 0) {
            cir.setReturnValue(ShootResult.NO_AMMO);
        }
    }
    @Inject(method = "shoot",
            at = @At(value="TAIL"),
            remap = false
    )
    public void reduceGunDurability(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, CallbackInfoReturnable<ShootResult> cir) {
        if (data.currentGunItem != null) {
            reduceDurability(data.currentGunItem.get(),1);
        }
    }
}