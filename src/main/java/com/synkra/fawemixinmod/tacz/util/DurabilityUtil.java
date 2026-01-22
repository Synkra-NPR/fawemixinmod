package com.synkra.fawemixinmod.tacz.util;

import com.tacz.guns.api.item.IGun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getDurability;
import static com.synkra.fawemixinmod.tacz.GunDurabilityManager.getMaxDurability;

public class DurabilityUtil {



    /**
     * 计算基于耐久度的后坐力修正系数
     *
     * @param gunItem 枪械物品
     * @return 后坐力修正系数
     */
    public static float getRecoilMultiplier(ItemStack gunItem) {
        if (!(gunItem.getItem() instanceof IGun iGun)) {
            return 1.0f;
        }

        int durability = getDurability(gunItem);
        int maxDurability =getMaxDurability(gunItem);

        if (maxDurability <= 0) {
            return 1.0f;
        }

        double x = (double) durability / maxDurability;
        return (float) (1.5+(0.6/(x+0.2)));
    }

    /**
     * 计算基于耐久度的散布修正系数
     *
     * @param gunItem 枪械物品
     * @return 散布修正系数
     */
    public static float getInaccuracyMultiplier(ItemStack gunItem) {
        if (!(gunItem.getItem() instanceof IGun iGun)) {
            return 1.0f;
        }

        int durability = getDurability(gunItem);
        int maxDurability = getMaxDurability(gunItem);

        if (maxDurability <= 0) {
            return 1.0f;
        }

        double x = (double) durability / maxDurability;
        return (float) ((float) ((0.754+(0.2673/(x+0.08236)))-1)*1.3+1);
    }
}