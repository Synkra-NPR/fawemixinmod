package com.synkra.fawemixinmod.mixin.tacz;

import com.tacz.guns.compat.kubejs.util.TimelessItemNbtFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

// 首先为父类 TimelessItemNbtFactory 创建 Mixin
@Mixin(TimelessItemNbtFactory.class)
public abstract class TimelessItemNbtFactoryMixin<T extends Item, S extends TimelessItemNbtFactory<T, S>> {
    @Shadow(remap = false)
    protected ResourceLocation id;
}