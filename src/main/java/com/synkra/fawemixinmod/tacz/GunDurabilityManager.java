package com.synkra.fawemixinmod.tacz;

import com.synkra.fawemixinmod.config.CommonConfig;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GunDurabilityManager {
    public static final String GUN_DURABILITY_TAG = "durability";
    public static final String GUN_MAX_DURABILITY_TAG = "maxDurability";
    /**
     * 根据枪械类型和属性计算初始耐久度
     * @param gunId 枪械ID
     * @return 计算后的初始耐久度
     */
    public static int calculateInitialDurability(ResourceLocation gunId) {
        return (int) (CommonConfig.getClientGunDurabilityFactor() * TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> {
                    // 获取枪械类型
                    String typeStr = gunIndex.getType();
                    GunTabType type = null;
                    try {
                        type = GunTabType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // 如果类型无法识别，默认返回
                        return 2000;
                    }

                    // 获取枪械数据
                    int rpm = gunIndex.getGunData().getRoundsPerMinute();

                    switch (type) {
                        case RIFLE:
                        case SMG:
                        case MG:
                            // 步枪，冲锋枪，机枪：耐久为
                            return Math.max(Math.min(rpm * 4, 3600), 2400);

                        case RPG:
                            boolean hasExplosion = gunIndex.getGunData().getBulletData().getExplosionData() != null &&
                                    gunIndex.getGunData().getBulletData().getExplosionData().isExplode();
                            if(rpm>600){
                                return 1200;
                            }
                            return hasExplosion ? 136 : 240;

                        case SHOTGUN:
                            // 霰弹枪：如果最大扩容>16(如果有的话，否则为基础弹容)&&射速>250
                            int baseAmmo = gunIndex.getGunData().getAmmoAmount();
                            int[] extendedAmmo = gunIndex.getGunData().getExtendedMagAmmoAmount();
                            int maxAmmo = (extendedAmmo != null && extendedAmmo.length > 2) ? extendedAmmo[2] : baseAmmo;

                            if (maxAmmo > 16 && rpm > 250) {
                                return Math.max(Math.min(rpm * 2, 1800), 256);
                            } else {
                                return Math.max(Math.min(rpm * 2, 900), 400);
                            }

                        case SNIPER:
                            // 狙击枪：耐久为168
                            return 168;

                        case PISTOL:
                            // 手枪
                            return Math.max(Math.min(rpm*2, 2000), 1200);

                        default:
                            // 默认返回1440
                            return 2000;
                    }
                }).orElse(2000)); // 如果无法获取枪械索引，默认返回1440
    }
    public static int getDurability(ItemStack gun) {
        if(gun.getItem() instanceof com.tacz.guns.api.item.IGun) {
            CompoundTag nbt = gun.getOrCreateTag();
            if (nbt.contains(GUN_DURABILITY_TAG, Tag.TAG_INT)) {
                return nbt.getInt(GUN_DURABILITY_TAG);
            }
            // 默认耐久度为1440
            return 1440;
        }else{
            return 0;
        }
    }

    public static void setDurability(ItemStack gun, int durability) {
        CompoundTag nbt = gun.getOrCreateTag();
        nbt.putInt(GUN_DURABILITY_TAG, Math.max(durability, 0));
    }

    public static int getMaxDurability(ItemStack gun) {
        CompoundTag nbt = gun.getOrCreateTag();
        if (nbt.contains(GUN_MAX_DURABILITY_TAG, Tag.TAG_INT)) {
            return nbt.getInt(GUN_MAX_DURABILITY_TAG);
        }
        // 默认最大耐久度为1440
        return 1440;
    }

    public static void setMaxDurability(ItemStack gun, int maxDurability) {
        CompoundTag nbt = gun.getOrCreateTag();
        nbt.putInt(GUN_MAX_DURABILITY_TAG, Math.max(maxDurability, 0));
    }

    /**
     * 减少枪械耐久度
     */
    public static void reduceDurability(ItemStack gun, int amount) {
        int currentDurability = getDurability(gun);
        setDurability(gun, Math.max(0, currentDurability - amount));
    }
}