package com.synkra.fawemixinmod.mixin.tacz;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.modifier.custom.RpmModifier;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(com.tacz.guns.util.AttachmentDataUtils.class)
public class AttachmentDataUtilsMixin {
    @Shadow(remap = false)
    private static List<Modifier> getModifiers(ItemStack gunItem, GunData gunData, String id) {
        throw new AbstractMethodError();
    }
    @Unique
    private static int faweMixin$getRPM(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 300;
        }
        FireMode fireMode = iGun.getFireMode(gunItem);
        int roundsPerMinute = gunData.getRoundsPerMinute(fireMode);

        List<Modifier> modifiers = getModifiers(gunItem, gunData, RpmModifier.ID);
        double eval = AttachmentPropertyManager.eval(modifiers, roundsPerMinute);
        return (int) Math.round(eval);
    }
}
