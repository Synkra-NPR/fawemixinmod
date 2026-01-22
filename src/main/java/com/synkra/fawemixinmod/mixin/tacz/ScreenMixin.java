package com.synkra.fawemixinmod.mixin.tacz;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.client.gui.screens.Screen.class)
public class ScreenMixin {
    @Shadow(remap = false)
    public Minecraft getMinecraft() {
        throw new AbstractMethodError();
    }
}
