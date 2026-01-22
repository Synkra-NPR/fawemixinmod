package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.compat.kubejs.util.GunNbtFactory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.synkra.fawemixinmod.tacz.GunRankingManager.setGunRanking;

@Mixin(GunNbtFactory.class)
public class GunNbtFactoryMixin  extends TimelessItemNbtFactoryMixin<AbstractGunItem, GunNbtFactory> {
    @Inject(method = "build",
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/api/item/IGun;setBulletInBarrel(Lnet/minecraft/world/item/ItemStack;Z)V",
                    shift = At.Shift.AFTER),
            remap = false
    )
    private void setThisGunRanking(CallbackInfoReturnable<ItemStack> cir){
        ItemStack stack = cir.getReturnValue();
        if (stack.getItem() instanceof IGun iGun) {
            // 设置枪械分级
            if (iGun instanceof GunItemDataAccessor) {
                GunRarity rarity = GunRankingManager.getGunRarity(this.id);
                int ranking = GunRankingManager.getGunRankingValue(rarity);
                setGunRanking(stack, ranking);
            }
        }
    }
}
