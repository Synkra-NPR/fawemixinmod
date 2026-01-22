package com.synkra.fawemixinmod.mixin.tacz;

import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import com.tacz.guns.client.gameplay.LocalPlayerShoot;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayerShoot.class)
public interface LocalPlayerShootAccessorMixin {
    @Accessor(value = "player",remap = false)
    LocalPlayer getPlayer();

    @Accessor(value = "data",remap = false)
    LocalPlayerDataHolder getData();
}