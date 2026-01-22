package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.util.OtherUtil;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.LocalPlayerShoot;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getDurability;

@Mixin(LocalPlayerShoot.class)
public abstract class LocalPlayerShootMixin {
    @Inject(method = "shoot",
            at = @At(value = "HEAD"),
            cancellable = true,
            remap = false
    )
    private void injectDurabilityCheck(CallbackInfoReturnable<ShootResult> cir) {
        LocalPlayerShoot self = (LocalPlayerShoot) (Object) this;
        LocalPlayer player = ((LocalPlayerShootAccessorMixin) self).getPlayer();

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun)) {
            return;
        }

        // 获取display实例用于播放声音
        GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHandItem).orElse(null);
        if (display == null) {
            return;
        }

        // 检查耐久度
        if (getDurability(mainHandItem) <= 0) {
            SoundPlayManager.playDryFireSound(player, display);
            cir.setReturnValue(ShootResult.NO_AMMO);
            cir.cancel();
        }
    }
    @Inject(method = "doShoot",
            at = @At(value = "RETURN"),
            remap = false
    )
    private void updateAmmoCountColor(GunDisplayInstance display, IGun iGun, ItemStack mainHandItem, GunData gunData, long delay, CallbackInfo ci) {
        OtherUtil.lastFireTime=System.currentTimeMillis();
    }
}
