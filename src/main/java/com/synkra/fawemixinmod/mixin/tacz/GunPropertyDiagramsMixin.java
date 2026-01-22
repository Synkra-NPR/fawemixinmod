package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.config.ClientConfig;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.components.refit.GunPropertyDiagrams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.synkra.fawemixinmod.tacz.GunRankingManager.GUN_RANKING_TAG;
import static com.synkra.fawemixinmod.tacz.util.OtherUtil.calculateCustomPulseColor;

@Mixin(GunPropertyDiagrams.class)
public class GunPropertyDiagramsMixin {
    @Redirect(method = "draw",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/client/gui/GuiGraphics.fill (IIIII)V"))
    private static void drawBackground(GuiGraphics instance, int p_282988_, int p_282861_, int p_281278_, int p_281710_, int p_281470_,GuiGraphics graphics) {
        if(ClientConfig.GUN_REFIT_SCREEN_ENABLED_MIXIN.get()){
            LocalPlayer player = Minecraft.getInstance().player;
            Inventory inventory = null;
            int color = 0xAF222222;
            if (player != null) {
                inventory = player.getInventory();
                ItemStack gunItem = inventory.getSelected();
                if(gunItem.getItem() instanceof IGun) {
                    if (gunItem.getTag() != null) {
                        color=calculateCustomPulseColor(0, GunRarity.getColor(gunItem.getTag().getInt(GUN_RANKING_TAG)));
                    }
                }
            }
            graphics.fill(p_282988_, p_282861_, p_282988_ + 288, p_282861_ + com.tacz.guns.client.gui.components.refit.GunPropertyDiagrams.getHidePropertyButtonYOffset() - 11 + 7, color);// 原版的这个按钮y位置似乎有问题，这里修复一下,+个7
        }else{
            graphics.fill(p_282988_, p_282861_, p_282988_ + 288, p_282861_ + com.tacz.guns.client.gui.components.refit.GunPropertyDiagrams.getHidePropertyButtonYOffset() - 11 + 7, 0xAF222222);
        }
    }
}
