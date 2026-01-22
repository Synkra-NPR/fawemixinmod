package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.synkra.fawemixinmod.tacz.client.GunRankingClientCache;
import com.synkra.fawemixinmod.tacz.util.DurabilityUtil;
import com.synkra.fawemixinmod.tacz.util.OtherUtil;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.input.RefitKey;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.client.resource.pojo.display.gun.DamageStyle;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.item.GunTooltipPart;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.*;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getDurability;
import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getMaxDurability;

@Mixin(value=ClientGunTooltip.class, priority = 900)
public class ClientGunTooltipMixinOverwrite {
    private MutableComponent rarity;
    // 新增属性
    private MutableComponent rpm;
    private MutableComponent ammoAmount;
    private MutableComponent pierce;
    private MutableComponent durability;
    // 爆炸属性
    private @Nullable MutableComponent explosionDamage;
    private @Nullable MutableComponent explosionRadius;

    // 详细信息属性
    private MutableComponent[] damageDistribution;
    private MutableComponent bulletAmount;
    private MutableComponent bulletSpeed;
    private MutableComponent hipInaccuracy;
    private MutableComponent sneakInaccuracy;
    private MutableComponent lieInaccuracy;
    private MutableComponent aimInaccuracy;
    private MutableComponent knockback;
    private MutableComponent recoilPitch;
    private MutableComponent recoilYaw;
    private MutableComponent weightDetail;
    @Shadow(remap = false)
    private boolean shouldShow(GunTooltipPart part){
        throw new AbstractMethodError();
    }
    @Shadow(remap = false)
    private @Nullable List<FormattedCharSequence> desc;
    /**
     * @author Synkra
     * @reason 修改Tooltip高度计算
     */
    @Overwrite
    public int getHeight() {
        int height = 0;
        height+=5;
        if (shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            height += 10 * this.desc.size() + 2;
        }
        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            height += 24;
        }

        // 基本信息高度计算
        if (shouldShow(GunTooltipPart.BASE_INFO)) {
            height += 14; // 等级信息
            // 移除了枪械类型的高度计算
            height += 40; // 基础属性现在每行显示两个，所以高度增加（伤害/射速、穿甲率/爆头倍率、弹容/穿透力、耐久度、爆炸伤害/爆炸半径）

            // 详细信息高度计算（按Shift显示）
            if (isShiftPressed_fawemixin()) {
                height += 70; // 详细信息现在每行显示两个属性，所以高度减半
            } else {
                height += 20; // 提示信息
            }
        }

        if (shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            height += 18;
        }
        if (shouldShow(GunTooltipPart.PACK_INFO)) {
            height += 28; // 增加高度以避免重叠
        }
        return height;
    }
    @Final
    @Shadow(remap = false)
    private CommonGunIndex gunIndex;
    @Shadow(remap = false)
    private int maxWidth;
    @Final
    @Shadow(remap = false)
    private ItemStack ammo;
    @Shadow(remap = false)
    private Component ammoName;
    @Final
    @Shadow(remap = false)
    private ItemStack gun;
    @Final
    @Shadow(remap = false)
    private IGun iGun;
    @Final
    @Shadow(remap = false)
    private static DecimalFormat CURRENT_AMMO_FORMAT_PERCENT;
    @Shadow(remap = false)
    private MutableComponent ammoCountText;
    @Final
    @Shadow(remap = false)
    private @Nullable GunDisplayInstance display;
    @Final
    @Shadow(remap = false)
    private static DecimalFormat DAMAGE_FORMAT;
    @Shadow(remap = false)
    private MutableComponent damage;
    @Shadow(remap = false)
    private MutableComponent armorIgnore;
    @Shadow(remap = false)
    private MutableComponent headShotMultiplier;
    @Shadow(remap = false)
    private MutableComponent tips;
    @Shadow(remap = false)
    private @Nullable MutableComponent packInfo;
    @Final
    @Shadow(remap = false)
    private static DecimalFormat FORMAT;
    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private void modifyWidth(Font font, CallbackInfoReturnable<Integer> cir) {
        // 确保宽度足够容纳两列文本
        cir.setReturnValue(Math.max(this.maxWidth, 270));
    }
    /**
     * @author Synkra
     * @reason 完全替换Tooltip，但保留gunsmithlib兼容
     */
    @Overwrite(remap = false)
    private void getText() {
        Font font = Minecraft.getInstance().font;
        BulletData bulletData = gunIndex.getBulletData();
        GunData gunData = gunIndex.getGunData();

        // === 调用gunsmithlib需要的方法 ===
        // 让gunsmithlib的@ModifyExpressionValue能够注入
        double originalDamage = AttachmentDataUtils.getDamageWithAttachment(gun, gunData);
        double originalArmorIgnore = AttachmentDataUtils.getArmorIgnoreWithAttachment(gun, gunData);
        double originalHeadshotMultiplier = AttachmentDataUtils.getHeadshotMultiplier(gun, gunData);

        // 这些值会被gunsmithlib的mixin修改
        // gunsmithlib会通过@ModifyExpressionValue修改这些返回值

        // === FAWE自己的逻辑开始 ===
        if (shouldShow(GunTooltipPart.DESCRIPTION)) {
            @Nullable String tooltip = gunIndex.getPojo().getTooltip();
            if (tooltip != null) {
                List<FormattedCharSequence> split = font.split(Component.translatable(tooltip), 300);
                if (split.size() > 3) {
                    this.desc = split.subList(0, 3);
                } else {
                    this.desc = split;
                }
                for (FormattedCharSequence sequence : this.desc) {
                    this.maxWidth = Math.max(font.width(sequence), this.maxWidth);
                }
            }
        }

        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            this.ammoName = ammo.getHoverName();
            this.maxWidth = Math.max(font.width(this.ammoName) + 22, this.maxWidth);

            int barrelBulletAmount = (iGun.hasBulletInBarrel(gun) && gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT) ? 1 : 0;
            int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(gun, gunIndex.getGunData()) + barrelBulletAmount;
            int currentAmmoCount = iGun.getCurrentAmmoCount(this.gun) + barrelBulletAmount;

            if (!iGun.useDummyAmmo(gun)) {
                if (display != null && display.getAmmoCountStyle()== AmmoCountStyle.PERCENT) {
                    this.ammoCountText = Component.literal(CURRENT_AMMO_FORMAT_PERCENT.format((float) currentAmmoCount / (maxAmmoCount == 0 ? 1f : maxAmmoCount)));
                } else {
                    this.ammoCountText = Component.literal("%d/%d".formatted(currentAmmoCount, maxAmmoCount));
                }
            } else {
                int dummyAmmoAmount = iGun.getDummyAmmoAmount(gun);
                if (display != null && display.getAmmoCountStyle()== AmmoCountStyle.PERCENT) {
                    String p = CURRENT_AMMO_FORMAT_PERCENT.format((float) currentAmmoCount / (maxAmmoCount == 0 ? 1f : maxAmmoCount));
                    this.ammoCountText = Component.literal("%s (%d)".formatted(p, dummyAmmoAmount));
                } else {
                    this.ammoCountText = Component.literal("%d/%d (%d)".formatted(currentAmmoCount, maxAmmoCount, dummyAmmoAmount));
                }

            }
            if (iGun.useInventoryAmmo(gun)) {
                this.ammoCountText = Component.translatable("tooltip.fawemixinmod.tacz.gun.inventory_mode").withStyle(ChatFormatting.YELLOW);
            }
            this.maxWidth = Math.max(font.width(this.ammoCountText) + 22, this.maxWidth);
        }

        if (shouldShow(GunTooltipPart.BASE_INFO)) {
            // 移除了原tacz的等级信息和枪械类型显示

            // 稀有度
            ResourceLocation gunId = iGun.getGunId(gun);
            GunRarity gunRarity = GunRankingClientCache.getGunRarity(gunId);
            this.rarity = (gunRarity.getDisplayName());
            this.maxWidth = Math.max(font.width(this.rarity), this.maxWidth);

            // === 使用被gunsmithlib修改后的值 ===
            double damage = originalDamage; // 这个值已被gunsmithlib修改
            int bulletAmount = gunData.getBulletData().getBulletAmount();

            // 获取伤害倍数并直接应用到伤害值上
            float damageMultiplier = GunRankingManager.getDamageMultiplier(gunRarity);
            damage *= damageMultiplier;

            MutableComponent value;
            if (display != null && display.getDamageStyle() == DamageStyle.PER_PROJECTILE && bulletAmount > 1) {
                value = Component.literal(DAMAGE_FORMAT.format(damage/bulletAmount) + "x" + bulletAmount).withStyle(ChatFormatting.YELLOW);
            } else {
                value = Component.literal(DAMAGE_FORMAT.format(damage)).withStyle(ChatFormatting.YELLOW);
            }
            if (bulletData.getExplosionData() != null && (AttachmentDataUtils.isExplodeEnabled(gun, gunData) || bulletData.getExplosionData().isExplode())) {
                value.append(" + ").append(DAMAGE_FORMAT.format(bulletData.getExplosionData().getDamage() * SyncConfig.DAMAGE_BASE_MULTIPLIER.get())).append(Component.translatable("tooltip.fawemixinmod.tacz.gun.explosion"));
            }

            this.damage = Component.translatable("tooltip.fawemixinmod.tacz.gun.damage").withStyle(ChatFormatting.AQUA).append(value.withStyle(ChatFormatting.YELLOW));
            this.maxWidth = Math.max(font.width(this.damage), this.maxWidth);

            // 射速
            int rpmValue = OtherUtil.getRPM(gun, gunData);
            this.rpm = Component.translatable("tooltip.fawemixinmod.tacz.gun.rpm")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(rpmValue)).withStyle(ChatFormatting.YELLOW));
            this.maxWidth = Math.max(font.width(this.rpm), this.maxWidth);

            // 穿甲率（使用被gunsmithlib修改后的值）
            double armorIgnoreValue = originalArmorIgnore; // 这个值已被gunsmithlib修改
            this.armorIgnore = Component.translatable("tooltip.fawemixinmod.tacz.gun.armor_ignore")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(FORMAT.format(armorIgnoreValue)).withStyle(ChatFormatting.YELLOW));
            this.maxWidth = Math.max(font.width(this.armorIgnore), this.maxWidth);

            // 爆头倍率（使用被gunsmithlib修改后的值）
            double headShotMultiplierValue = originalHeadshotMultiplier; // 这个值已被gunsmithlib修改
            this.headShotMultiplier = Component.translatable("tooltip.fawemixinmod.tacz.gun.head_shot_multiplier")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(FORMAT.format(headShotMultiplierValue)).withStyle(ChatFormatting.YELLOW));
            this.maxWidth = Math.max(font.width(this.headShotMultiplier), this.maxWidth);

            // 弹容
            int baseAmmoAmount = gunData.getAmmoAmount();
            int[] extendedMagAmmoAmount = gunData.getExtendedMagAmmoAmount();
            this.ammoAmount = Component.translatable("tooltip.fawemixinmod.tacz.gun.ammo_amount")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(baseAmmoAmount)).withStyle(ChatFormatting.YELLOW));

            if (extendedMagAmmoAmount != null && extendedMagAmmoAmount.length >= 3) {
                this.ammoAmount.append(Component.literal(" (I:").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(extendedMagAmmoAmount[0])).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" II:").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(extendedMagAmmoAmount[1])).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" III:").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(extendedMagAmmoAmount[2])).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY));
            }
            this.maxWidth = Math.max(font.width(this.ammoAmount), this.maxWidth);

            // 穿透力
            int pierceValue = bulletData.getPierce();
            this.pierce = Component.translatable("tooltip.fawemixinmod.tacz.gun.pierce")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(pierceValue)).withStyle(ChatFormatting.YELLOW));
            this.maxWidth = Math.max(font.width(this.pierce), this.maxWidth);

            // 耐久度信息
            int durabilityValue = getDurability(gun);
            int maxDurabilityValue = getMaxDurability(gun);
            this.durability = Component.translatable("tooltip.fawemixinmod.tacz.gun.durability")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.format("%d/%d", durabilityValue, maxDurabilityValue)).withStyle(ChatFormatting.YELLOW));
            this.maxWidth = Math.max(font.width(this.durability), this.maxWidth);

            // 爆炸伤害和爆炸半径
            if (bulletData.getExplosionData() != null && bulletData.getExplosionData().isExplode()) {
                ExplosionData explosionData = bulletData.getExplosionData();

                this.explosionDamage = Component.translatable("tooltip.fawemixinmod.tacz.gun.explosion.damage")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(DAMAGE_FORMAT.format(explosionData.getDamage())).withStyle(ChatFormatting.YELLOW));
                this.maxWidth = Math.max(font.width(this.explosionDamage), this.maxWidth);

                this.explosionRadius = Component.translatable("tooltip.fawemixinmod.tacz.gun.explosion.radius")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(DAMAGE_FORMAT.format(explosionData.getRadius())).withStyle(ChatFormatting.YELLOW));
                this.maxWidth = Math.max(font.width(this.explosionRadius), this.maxWidth);
            }

            // 预先计算详细信息
            faweMixin$calculateDetailedInfo(font, bulletData, gunData, damage);
        }

        if (shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            String keyName = Component.keybind(RefitKey.REFIT_KEY.getName()).getString().toUpperCase(Locale.ENGLISH);
            this.tips = Component.translatable("tooltip.fawemixinmod.tacz.gun.tips", keyName).withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.ITALIC);
            this.maxWidth = Math.max(font.width(this.tips), this.maxWidth);
        }

        if (shouldShow(GunTooltipPart.PACK_INFO)) {
            ResourceLocation gunId = iGun.getGunId(gun);
            PackInfo packInfoObject = ClientAssetsManager.INSTANCE.getPackInfo(gunId);
            if (packInfoObject != null) {
                packInfo = Component.translatable(packInfoObject.getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
                this.maxWidth = Math.max(font.width(this.packInfo), this.maxWidth);
            }
        }
    }
    @Unique
    private static boolean isShiftPressed_fawemixin() {
        return Screen.hasShiftDown();
    }

    /**
     * @author Synkra
     * @reason 详细化tooltip
     */
    @Overwrite
    public void renderText(Font font, int pX, int pY, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        int yOffset = pY;

        if (shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            yOffset += 2;
            for (FormattedCharSequence sequence : this.desc) {
                font.drawInBatch(sequence, pX, yOffset, 0xaaaaaa, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            }
        }


        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            yOffset += 4;

            // 弹药名
            font.drawInBatch(this.ammoName, pX + 20, yOffset, 0xffaa00, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);

            // 弹药数
            font.drawInBatch(this.ammoCountText, pX + 20, yOffset + 10, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);

            yOffset += 20;
        }


        if (shouldShow(GunTooltipPart.BASE_INFO)) {
            yOffset += 4;

            // 稀有度
            font.drawInBatch(this.rarity, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;

            // 移除了枪械类型的渲染代码
            // 枪械类型
            // if (this.gunType != null) {
            //     font.drawInBatch(this.gunType, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            //     yOffset += 10;
            // }

            // 基础属性（每行显示两个属性）
            // 伤害和射速
            font.drawInBatch(this.damage, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            font.drawInBatch(this.rpm, pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;

            // 穿甲率和爆头倍率
            font.drawInBatch(this.armorIgnore, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            font.drawInBatch(this.headShotMultiplier, pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;

            // 弹容和穿透力
            font.drawInBatch(this.ammoAmount, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            font.drawInBatch(this.pierce, pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;

            // 爆炸伤害和爆炸半径（如果存在）
            if (this.explosionDamage != null && this.explosionRadius != null) {
                font.drawInBatch(this.explosionDamage, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                font.drawInBatch(this.explosionRadius, pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            }

            font.drawInBatch(Component.literal("——————————————————————————————").withStyle(ChatFormatting.GRAY), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;
            // 耐久度
            font.drawInBatch(this.durability, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;
            font.drawInBatch(Component.literal("——————————————————————————————").withStyle(ChatFormatting.GRAY), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;
            // 详细信息（按Shift显示）
            if (isShiftPressed_fawemixin()) {
                // 伤害分布
                if (this.damageDistribution != null) {
                    for (MutableComponent component : this.damageDistribution) {
                        font.drawInBatch(Objects.requireNonNullElseGet(component, Component::empty), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                        yOffset += 10;
                    }
                }

                // 弹丸数量和弹速（放在同一行）
                font.drawInBatch(Objects.requireNonNullElseGet(this.bulletAmount, Component::empty), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                font.drawInBatch(Objects.requireNonNullElseGet(this.bulletSpeed, Component::empty), pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;

                // 腰际射击扩散和潜行射击扩散
                font.drawInBatch(Objects.requireNonNullElseGet(this.hipInaccuracy, Component::empty), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                font.drawInBatch(Objects.requireNonNullElseGet(this.sneakInaccuracy, Component::empty), pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;

                // 趴伏射击扩散和瞄准精度
                font.drawInBatch(Objects.requireNonNullElseGet(this.lieInaccuracy, Component::empty), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                font.drawInBatch(Objects.requireNonNullElseGet(this.aimInaccuracy, Component::empty), pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;

                // 击退和垂直后坐力
                font.drawInBatch(Objects.requireNonNullElseGet(this.knockback, Component::empty), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                font.drawInBatch(Objects.requireNonNullElseGet(this.recoilPitch, Component::empty), pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;

                // 水平后坐力和重量
                font.drawInBatch(Objects.requireNonNullElseGet(this.recoilYaw, Component::empty), pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                font.drawInBatch(Objects.requireNonNullElseGet(this.weightDetail, Component::empty), pX + 150, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            } else {
                // 提示按Shift查看详细信息
                MutableComponent shiftTip = Component.translatable("tooltip.fawemixinmod.tacz.gun.shift_tip")
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(ChatFormatting.ITALIC);
                font.drawInBatch(shiftTip, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
                MutableComponent shiftTip2 = Component.translatable("tooltip.fawemixinmod.tacz.gun.shift_tip_2")
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(ChatFormatting.ITALIC);
                font.drawInBatch(shiftTip2, pX, yOffset, 0x777777, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            }
        }


        if (shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            yOffset += 4;

            // Z 键说明
            font.drawInBatch(this.tips, pX, yOffset, 0xffffff, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;
        }


        if (shouldShow(GunTooltipPart.PACK_INFO)) {
            // 枪包名
            if (packInfo != null) {
                font.drawInBatch(this.packInfo, pX, yOffset, 0xffffff, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            }
        }
    }
    @Unique
    private void faweMixin$calculateDetailedInfo(Font font, BulletData bulletData, GunData gunData, double damage) {
        // 伤害分布 - 改进的可视化显示
        ExtraDamage extraDamage = bulletData.getExtraDamage();
        if (extraDamage != null && extraDamage.getDamageAdjust() != null) {
            LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust = extraDamage.getDamageAdjust();

            // 获取枪械稀有度并计算伤害倍数
            ResourceLocation gunId = iGun.getGunId(gun);
            GunRarity gunRarity = GunRankingClientCache.getGunRarity(gunId);
            float damageMultiplier = GunRankingManager.getDamageMultiplier(gunRarity);

            // 创建三段式伤害分布显示 (参考示例代码)
            MutableComponent rangeBar = Component.literal("");
            MutableComponent rangeText = Component.literal("");
            MutableComponent damageText = Component.literal("");

            // 构建40个字符的射程标尺
            for (int i = 0; i < 45; i++) {
                float distance = i * 2.0f; // 每个字符代表2米

                // 确定当前距离对应的颜色段
                int segment = 0;
                for (int j = 0; j < damageAdjust.size() && j < 3; j++) {
                    if (distance >= damageAdjust.get(j).getDistance()) {
                        segment = j + 1;
                    }
                }

                // 根据段落设置颜色
                ChatFormatting color = switch (segment) {
                    case 0 -> ChatFormatting.GREEN;   // 第一段绿色
                    case 1 -> ChatFormatting.GOLD;    // 第二段橙色
                    case 2 -> ChatFormatting.RED;     // 第三段红色
                    default -> ChatFormatting.DARK_GRAY;   // 其余灰色
                };

                rangeBar.append(Component.literal("-").withStyle(color));
            }

            // 构建伤害和距离文本
            damageText.append(Component.translatable("tooltip.fawemixinmod.tacz.gun.damage_distribution").withStyle(ChatFormatting.AQUA));
            rangeText.append(Component.translatable("tooltip.fawemixinmod.tacz.gun.range_threshold").withStyle(ChatFormatting.AQUA));

            // 添加各段的伤害值和距离阈值
            for (int i = 0; i < damageAdjust.size() && i < 3; i++) {
                ExtraDamage.DistanceDamagePair pair = damageAdjust.get(i);
                ChatFormatting color = switch (i) {
                    case 0 -> ChatFormatting.GREEN;   // 第一段绿色
                    case 1 -> ChatFormatting.GOLD;    // 第二段橙色
                    case 2 -> ChatFormatting.RED;     // 第三段红色
                    default -> ChatFormatting.GRAY;   // 其余灰色
                };

                // 添加分隔符和伤害值
                if(i!=0)damageText.append(Component.literal(" / ").withStyle(ChatFormatting.GRAY));
                // 应用伤害倍数
                damageText.append(Component.literal(String.format("%.1f", pair.getDamage() * damageMultiplier)).withStyle(color));

                // 添加分隔符和距离阈值
                if(i!=0)rangeText.append(Component.literal(" / ").withStyle(ChatFormatting.GRAY));
                if (pair.getDistance() >= Integer.MAX_VALUE) {
                    rangeText.append(Component.literal("∞").withStyle(ChatFormatting.RED)); // 无限远显示为无穷大符号
                } else {
                    rangeText.append(Component.literal(String.format("%.0f", pair.getDistance())).withStyle(color));
                }
            }

            // 创建可视化组件 - 顺序：距离阈值、彩色标尺、伤害值
            this.damageDistribution = new MutableComponent[3];
            this.damageDistribution[0] = rangeText;  // 距离阈值在上
            this.damageDistribution[1] = rangeBar;   // 彩色标尺在中
            this.damageDistribution[2] = damageText; // 伤害值在下

            for (int i = 0; i < this.damageDistribution.length; i++) {
                if (this.damageDistribution[i] == null) {
                    this.damageDistribution[i] = Component.empty();
                }
                this.maxWidth = Math.max(font.width(this.damageDistribution[i]), this.maxWidth);
            }
        } else {
            // 没有伤害衰减的情况
            this.damageDistribution = new MutableComponent[3];

            // 获取枪械稀有度并计算伤害倍数
            ResourceLocation gunId = iGun.getGunId(gun);
            GunRarity gunRarity = GunRankingClientCache.getGunRarity(gunId);
            float damageMultiplier = GunRankingManager.getDamageMultiplier(gunRarity);

            // 只显示初始伤害值 (使用青色字体，黄色数值)
            MutableComponent damageText = Component.translatable("tooltip.fawemixinmod.tacz.gun.damage_distribution")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.format("%.1f", damage * damageMultiplier)).withStyle(ChatFormatting.YELLOW));

            MutableComponent rangeText = Component.translatable("tooltip.fawemixinmod.tacz.gun.range_threshold")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("∞").withStyle(ChatFormatting.YELLOW));

            this.damageDistribution[0] = rangeText; // 距离阈值在上
            this.damageDistribution[1] = Component.literal("-".repeat(45)).withStyle(ChatFormatting.GREEN); // 绿色标尺在中
            this.damageDistribution[2] = damageText != null ? damageText : Component.empty(); // 伤害文本在下

            for (int i = 0; i < this.damageDistribution.length; i++) {
                if (this.damageDistribution[i] == null) {
                    this.damageDistribution[i] = Component.empty();
                }
                this.maxWidth = Math.max(font.width(this.damageDistribution[i]), this.maxWidth);
            }
        }

        // 弹丸数量
        this.bulletAmount = Component.translatable("tooltip.fawemixinmod.tacz.gun.bullet_amount")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.valueOf(bulletData.getBulletAmount())).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.bulletAmount), this.maxWidth);

        // 弹速
        this.bulletSpeed = Component.translatable("tooltip.fawemixinmod.tacz.gun.bullet_speed")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%dm/s", (int) bulletData.getSpeed())).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.bulletSpeed), this.maxWidth);

        // 腰际射击扩散
        float hipValue = gunData.getInaccuracy(InaccuracyType.STAND);
        // 应用耐久度对散布的影响
        hipValue *= DurabilityUtil.getInaccuracyMultiplier(gun);
        this.hipInaccuracy = Component.translatable("tooltip.fawemixinmod.tacz.gun.hip_inaccuracy")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", hipValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.hipInaccuracy), this.maxWidth);

        // 潜行射击扩散
        float sneakValue = gunData.getInaccuracy(InaccuracyType.SNEAK);
        // 应用耐久度对散布的影响
        sneakValue *= DurabilityUtil.getInaccuracyMultiplier(gun);
        this.sneakInaccuracy = Component.translatable("tooltip.fawemixinmod.tacz.gun.sneak_inaccuracy")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", sneakValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.sneakInaccuracy), this.maxWidth);

        // 趴伏射击扩散
        float lieValue = gunData.getInaccuracy(InaccuracyType.LIE);
        // 应用耐久度对散布的影响
        lieValue *= DurabilityUtil.getInaccuracyMultiplier(gun);
        this.lieInaccuracy = Component.translatable("tooltip.fawemixinmod.tacz.gun.lie_inaccuracy")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", lieValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.lieInaccuracy), this.maxWidth);

        // 瞄准精度
        float aimValue = gunData.getInaccuracy(InaccuracyType.AIM);
        // 应用耐久度对散布的影响
        aimValue *= DurabilityUtil.getInaccuracyMultiplier(gun);
        this.aimInaccuracy = Component.translatable("tooltip.fawemixinmod.tacz.gun.aim_inaccuracy")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", aimValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.aimInaccuracy), this.maxWidth);

        // 击退
        this.knockback = Component.translatable("tooltip.fawemixinmod.tacz.gun.knockback")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", bulletData.getKnockback())).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.knockback), this.maxWidth);

        // 垂直后坐力
        GunRecoil recoil = gunData.getRecoil();
        float pitchValue = 0;
        if (recoil != null && recoil.getPitch() != null && recoil.getPitch().length > 0) {
            GunRecoilKeyFrame[] pitchFrames = recoil.getPitch();
            float[] values = pitchFrames[0].getValue();
            pitchValue = Math.max(Math.abs(values[0]), Math.abs(values[1]));
        }
        // 应用耐久度对后坐力的影响
        pitchValue *= DurabilityUtil.getRecoilMultiplier(gun);
        this.recoilPitch = Component.translatable("tooltip.fawemixinmod.tacz.gun.recoil.pitch")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", pitchValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.recoilPitch), this.maxWidth);

        // 水平后坐力
        float yawValue = 0;
        if (recoil != null && recoil.getYaw() != null && recoil.getYaw().length > 0) {
            GunRecoilKeyFrame[] yawFrames = recoil.getYaw();
            float[] values = yawFrames[0].getValue();
            yawValue = Math.max(Math.abs(values[0]), Math.abs(values[1]));
        }
        // 应用耐久度对后坐力的影响
        yawValue *= DurabilityUtil.getRecoilMultiplier(gun);
        this.recoilYaw = Component.translatable("tooltip.fawemixinmod.tacz.gun.recoil.yaw")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", yawValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.recoilYaw), this.maxWidth);

        // 重量
        double weightValue = AttachmentDataUtils.getWightWithAttachment(gun, gunData);
        this.weightDetail = Component.translatable("tooltip.fawemixinmod.tacz.gun.weight")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.2f", weightValue)).withStyle(ChatFormatting.YELLOW));
        this.maxWidth = Math.max(font.width(this.weightDetail), this.maxWidth);
    }
}
