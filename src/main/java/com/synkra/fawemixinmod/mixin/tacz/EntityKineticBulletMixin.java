package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityKineticBullet.class)
public class EntityKineticBulletMixin{
    @Shadow(remap = false)
    private ResourceLocation gunId;
    @ModifyVariable(
            method = "getDamage",
            at=@At("STORE"),
            remap = false,name="damage")
    public float modifyDamageByRarity(float value,Vec3 hitVec) {
        return value*GunRankingManager.getDamageMultiplier(GunRankingManager.getGunRarity(this.gunId));
    }
}
