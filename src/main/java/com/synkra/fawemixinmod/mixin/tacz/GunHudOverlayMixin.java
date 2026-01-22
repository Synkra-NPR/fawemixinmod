package com.synkra.fawemixinmod.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.synkra.fawemixinmod.config.ClientConfig;
import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.synkra.fawemixinmod.tacz.util.OtherUtil;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getDurability;
import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getMaxDurability;

@Mixin(value = GunHudOverlay.class, priority = 500) // 关键：降低优先级
public abstract class GunHudOverlayMixin {
    @Unique
    private static final Object[] RPM_CACHE = new Object[2];//使用缓存提高性能。index:0.ItemStack,1.Integer
    @Unique
    private static final Object[] RARITY_CACHE = new Object[2];//使用缓存提高性能。index:0.ItemStack,1.Integer
    @Shadow(remap = false)
    private static int cacheMaxAmmoCount;
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            ordinal = 1, shift = At.Shift.AFTER))
    private void addDurabilityNextToAmmo(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height, CallbackInfo ci,
               @Local(name="currentAmmoCountText")String currentAmmoCountText,@Local(name="stack")ItemStack stack,@Local(name="mc")Minecraft mc,@Local(name="poseStack")PoseStack poseStack,@Local(name="inventoryAmmoCountText")String inventoryAmmoCountText) {
        // 获取耐久度
        int durability = getDurability(stack);
        int maxDurability = getMaxDurability(stack);
        if (maxDurability <= 0) return;

        double durabilityPercent = (double) durability / maxDurability;
        String durabilityText = String.format("%.0f%%", durabilityPercent * 100);

        int color = durabilityPercent > 0.25 ? 0xAAAAAA : 0xFF5555;

        Font font = mc.font;
        graphics.pose().pushPose();
        graphics.pose().scale(0.8f, 0.8f, 1); // 和备弹同样大小

        float inventoryAmmoX = (width - 68 + mc.font.width(currentAmmoCountText) * 1.5f) / 0.8f;

        // 绘制耐久度百分比
        if (!durabilityText.isEmpty()) {
            float durabilityX = inventoryAmmoX + (inventoryAmmoCountText.isEmpty() ? 0 : mc.font.width(inventoryAmmoCountText)) + 4;
            graphics.drawString(font, durabilityText, durabilityX, (height - 43) / 0.8f, color, false);
        }
        poseStack.popPose();
    }
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/client/gui/GuiGraphics.fill (IIIII)V")) // 在绘制备弹后执行
    private void modifyVerticalBar(GuiGraphics graphics, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor, @Local(name="stack")ItemStack stack,@Local(name="ammoCount")int ammoCount, @Local(name="overheatLocked")boolean overheatLocked,@Local(name="useDummyAmmo")boolean useDummyAmmo, @Local(name="useDummyAmmo")boolean useInventoryAmmo) {
        if(ClientConfig.GUN_HUD_OVERLAY_ENABLED_MIXIN.get()) {
            graphics.fill(pMinX, pMinY - 2, pMaxX + 100, pMaxY + 2, 0x33000000);
            int verticalBarColor;

            if (ammoCount < (cacheMaxAmmoCount * 0.25) && ammoCount < 10 || overheatLocked) {
                verticalBarColor = 0xFFFF5555;
            } else {
                verticalBarColor = useInventoryAmmo && useDummyAmmo ? 0xFF55FFFF : useInventoryAmmo ? 0xFFFFFF55 : 0xFFFFFFFF;
            }
            graphics.fill(pMinX, pMinY - 2, pMaxX + 1, pMaxY + 2, verticalBarColor);
        }else{
            graphics.fill(pMinX, pMinY, pMaxX, pMaxY, 0xFFFFFFFF);
        }
    }
    @ModifyVariable(
            method = "render",
            at = @At("STORE"),
            remap = false, name = "ammoCountColor")
    private int modifyAmmoCountColor(int value, @Local(name="stack")ItemStack stack, @Local(name="gunData")GunData gunData,@Local(name="ammoCount")int ammoCount, @Local(name="overheatLocked")boolean overheatLocked,@Local(name="useDummyAmmo")boolean useDummyAmmo, @Local(name="useDummyAmmo")boolean useInventoryAmmo) {
        if(ClientConfig.GUN_HUD_OVERLAY_ENABLED_MIXIN.get()) {
            int ammoCountColor;

            if (RPM_CACHE[0] != stack) {
                RPM_CACHE[0] = stack;
                RPM_CACHE[1] = OtherUtil.getRPM(stack, gunData);
            }
            if (RARITY_CACHE[0] != stack) {
                RARITY_CACHE[0] = stack;
                RARITY_CACHE[1] = stack.getTag().getInt(GunRankingManager.GUN_RANKING_TAG);
            }
            int rarity = (int) RARITY_CACHE[1];
            int rpm = (int) RPM_CACHE[1];

            if (ammoCount == 0) {
                ammoCountColor = 0xFFFF5555;
            } else {
                ammoCountColor = 0xFF000000 | GunRarity.getColor(rarity);
            }
            ammoCountColor=OtherUtil.overlayColorFast(ammoCountColor, 0xFFFFFFFF, 0.15f);
            long gap = System.currentTimeMillis() - OtherUtil.lastFireTime;
            long modifiedGap = (long) (gap * (rpm / 600f));
            return OtherUtil.calculateCustomPulseColorFullAlphaDefault((float) Math.min(1, Math.min(1, Mth.clamp((rpm / 1200d), 0.25,0.5) + ((float) modifiedGap / 200))), ammoCountColor);
        }else{
            return value;
        }
    }
    @ModifyVariable(
            method = "render",
            at = @At("STORE"),
            remap = false, name = "inventoryAmmoCountColor")
    private int modifyInventoryAmmoCountColor(int value, @Local(name="stack")ItemStack stack, @Local(name="gunData")GunData gunData,@Local(name="ammoCount")int ammoCount, @Local(name="overheatLocked")boolean overheatLocked,@Local(name="useDummyAmmo")boolean useDummyAmmo, @Local(name="useDummyAmmo")boolean useInventoryAmmo) {
        if(ClientConfig.GUN_HUD_OVERLAY_ENABLED_MIXIN.get()) {
            int inventoryAmmoCountColor;

            if (RARITY_CACHE[0] != stack) {
                RARITY_CACHE[0] = stack;
                RARITY_CACHE[1] = stack.getTag().getInt(GunRankingManager.GUN_RANKING_TAG);
            }
            int rarity = (int) RARITY_CACHE[1];

            inventoryAmmoCountColor = 0xFF000000 | GunRarity.getColor(rarity);

            return OtherUtil.calculateCustomPulseColorFullAlphaDefault((float) Math.min(1.0,(System.currentTimeMillis()-OtherUtil.lastReloadFinishTime)/1000d), inventoryAmmoCountColor);
        }else{
            return value;
        }
    }
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "java/lang/String.format (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
            remap = false)
    private String modifyVersionText(String format, Object... args) {
        if(ClientConfig.GUN_HUD_OVERLAY_ENABLED_VERSION_MIXIN.get()){
            return String.format(format, args[1], "FAWE-Mixin");
        }else{
            return String.format(format, args[0], args[1]);
        }
    }
}