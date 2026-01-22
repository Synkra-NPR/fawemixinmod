package com.synkra.fawemixinmod.mixin.attributeslib;

import dev.shadowsoffire.attributeslib.client.AttributesGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AttributesGui.class)
public class AttributesGuiMixin {
    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/network/chat/Component.literal (Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0)
            )
    private MutableComponent modifyTextByI18N_1(String p_237114_) {
        return Component.translatable("fawemixinmod.attributeslib.gui.hide_unchanged");
    }

    @Redirect(method = "renderTooltip",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/network/chat/Component.literal (Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0),
            remap = false
            )
    private MutableComponent modifyTextByI18N_2(String p_237114_) {
        return Component.translatable("fawemixinmod.attributeslib.value.dynamic_paren");
    }
    @Redirect(method = "renderTooltip",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/network/chat/Component.literal (Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 3),
            remap = false
            )
    private MutableComponent modifyTextByI18N_3(String p_237114_) {
        return Component.translatable("fawemixinmod.attributeslib.gui.modifier_formula");
    }
    @Redirect(method = "renderEntry",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/network/chat/Component.literal (Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0),
            remap = false
            )
    private MutableComponent modifyTextByI18N_4(String p_237114_) {
        return Component.translatable("fawemixinmod.attributeslib.value.dynamic");
    }
}
