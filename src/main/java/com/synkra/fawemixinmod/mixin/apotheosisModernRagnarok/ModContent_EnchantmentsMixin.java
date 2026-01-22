package com.synkra.fawemixinmod.mixin.apotheosisModernRagnarok;

import com.synkra.fawemixinmod.config.CommonConfig;
import mod.chloeprime.apotheosismodernragnarok.common.ModContent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(ModContent.Enchantments.class)
public class ModContent_EnchantmentsMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/DeferredRegister;register(Ljava/lang/String;Ljava/util/function/Supplier;)Lnet/minecraftforge/registries/RegistryObject;"
            ),
            remap = false
    )
    private static RegistryObject<Enchantment> redirectRegister(
            DeferredRegister<Enchantment> instance,
            String name,
            Supplier<? extends Enchantment> supplier
    ) {
        // 要移除的附魔列表
        Set<String> toRemove = Set.of("survival_instinct","projection_magic","perfect_block");

        if (toRemove.contains(name)) {
            // 直接返回null
            return null;
        }

        // 正常注册其他附魔
        return instance.register(name, supplier);
    }
}
