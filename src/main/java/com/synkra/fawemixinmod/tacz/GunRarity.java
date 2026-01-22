package com.synkra.fawemixinmod.tacz;

import com.synkra.fawemixinmod.config.CommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * 枪械稀有度
 */
public enum GunRarity {

    COMMON("common"),        // 普通
    UNCOMMON("uncommon"),      // 优秀
    RARE("rare"),              // 精良
    EPIC("epic"),              // 史诗
    LEGENDARY("legendary"),    // 传说
    MYTHIC("mythic"),          // 神话
    IMMORTAL("immortal"),      // 不朽
    ABNORMAL("abnormal");      // 异常

    private final String nameKey;

    private static final Map<GunRarity, Integer> rarityColorMap = new HashMap<>();
    private static void init(){
        rarityColorMap.put(COMMON, CommonConfig.GUN_RARITY_COLOR.get().get(0));
        rarityColorMap.put(UNCOMMON, CommonConfig.GUN_RARITY_COLOR.get().get(1));
        rarityColorMap.put(RARE, CommonConfig.GUN_RARITY_COLOR.get().get(2));
        rarityColorMap.put(EPIC, CommonConfig.GUN_RARITY_COLOR.get().get(3));
        rarityColorMap.put(LEGENDARY, CommonConfig.GUN_RARITY_COLOR.get().get(4));
        rarityColorMap.put(MYTHIC, CommonConfig.GUN_RARITY_COLOR.get().get(5));
        rarityColorMap.put(IMMORTAL, CommonConfig.GUN_RARITY_COLOR.get().get(6));
        rarityColorMap.put(ABNORMAL, CommonConfig.GUN_RARITY_COLOR.get().get(7));
    }

    public static int getColor(GunRarity rarity) {
        if(rarityColorMap.isEmpty()){
            init();
        }
        return rarityColorMap.get(rarity);
    }
    GunRarity(String nameKey) {
        this.nameKey = nameKey;
    }


    public static int getColor(int gunRankingNBTValue) {
        return switch (gunRankingNBTValue) {
            case 1 -> getColor(COMMON);
            case 2 -> getColor(UNCOMMON);
            case 3 -> getColor(RARE);
            case 4 -> getColor(EPIC);
            case 5 -> getColor(LEGENDARY);
            case 6 -> getColor(MYTHIC);
            case 7 -> getColor(IMMORTAL);
            default -> getColor(ABNORMAL);
        };
    }
    public static GunRarity getGunRarityByNBTValue(int gunRankingNBTValue) {
        return switch (gunRankingNBTValue) {
            case 1 -> COMMON;
            case 2 -> UNCOMMON;
            case 3 -> RARE;
            case 4 -> EPIC;
            case 5 -> LEGENDARY;
            case 6 -> MYTHIC;
            case 7 -> IMMORTAL;
            default -> ABNORMAL;
        };
    }

    public String getNameKey() {
        return nameKey;
    }
    /**
     * 获取稀有度的显示名称
     *
     * @return 包含颜色的显示名称组件
     */
    public MutableComponent getDisplayName() {
        return Component.literal("[").withStyle(style -> style.withColor(getColor(this))).append(Component.translatable("tooltip.fawemixinmod.tacz.gun.rarity." + nameKey))
                .withStyle(style -> style.withColor(getColor(this))).append(Component.literal("]").withStyle(style -> style.withColor(getColor(this))));
//        return Component.translatable("tooltip.fawemixinmod.tacz.gun.rarity." + nameKey)
//                .withStyle(style -> style.withColor(colorValue));
    }
}