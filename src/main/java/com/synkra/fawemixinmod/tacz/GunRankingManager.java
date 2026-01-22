package com.synkra.fawemixinmod.tacz;

import com.synkra.fawemixinmod.config.CommonConfig;
import com.synkra.fawemixinmod.tacz.util.OtherUtil;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class GunRankingManager {
    public static final String GUN_RANKING_TAG = "GunRanking";

    private static final Map<ResourceLocation, GunRarity> GUN_RARITY_MAP = new HashMap<>();
    private static boolean isInitialized = false;
    private static final List<? extends String> ABNORMAL_GUNS_IDS = CommonConfig.GUN_BOX_BLACK_LIST.get();
    private static final double[] distribution = new double[7];
    public static void init() {
        if (isInitialized) {
            return;
        }
        List<Double> reversedDistribution = OtherUtil.reversed((List<Double>)CommonConfig.GUN_RARITY_DISTRIBUTION.get());
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = reversedDistribution.get(i);
        }

        CommonAssetsManager assetsManager = CommonAssetsManager.getInstance();
        if (assetsManager == null) {
            return;
        }

        Set<Map.Entry<ResourceLocation, CommonGunIndex>> allGuns = assetsManager.getAllGuns();
        if (allGuns.isEmpty()) {
            return;
        }

        Map<ResourceLocation, Double> gunScores = new HashMap<>();
        List<ResourceLocation> exceptionGuns = new ArrayList<>();

        // 为所有枪械计算分数
        for (Map.Entry<ResourceLocation, CommonGunIndex> entry : allGuns) {
            ResourceLocation gunId = entry.getKey();
            CommonGunIndex gunIndex = entry.getValue();

            try {
                GunData gunData = gunIndex.getGunData();
                if (gunData != null) {
                    // 获取基本属性
                    float damage = gunData.getBulletData().getDamageAmount();
                    int rpm = gunData.getRoundsPerMinute();

                    // 检查异常枪械

                    if (damage > 500 || damage < 0.8 || rpm > 10000 || ABNORMAL_GUNS_IDS.contains(gunId.toString())) {
                        exceptionGuns.add(gunId);
                        GUN_RARITY_MAP.put(gunId, GunRarity.ABNORMAL);
                        continue;
                    }

                    double score = calculateZombieCombatScore(gunData, gunIndex);

                    gunScores.put(gunId, score);
                } else {
                    exceptionGuns.add(gunId);
                    GUN_RARITY_MAP.put(gunId, GunRarity.ABNORMAL);
                }
            } catch (Exception e) {
                exceptionGuns.add(gunId);
                GUN_RARITY_MAP.put(gunId, GunRarity.ABNORMAL);
            }
        }

        // 按分数排序（从高到低）
        List<Map.Entry<ResourceLocation, Double>> sortedGuns = new ArrayList<>(gunScores.entrySet());
        sortedGuns.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 根据百分位分配稀有度
        assignRaritiesByPercentile(sortedGuns);

        isInitialized = true;
    }

    /**
     * 基于改进系统计算僵尸战斗分数
     * 考虑伤害输出、DPS、弹药容量、装填效率、准确性、
     * 穿透力、爆炸、特殊能力和霰弹枪调整
     */
    private static double calculateZombieCombatScore(GunData gunData, CommonGunIndex gunIndex) {
        // 从枪械数据中获取基本属性
        float damage = gunData.getBulletData().getDamageAmount();
        int bulletAmount = gunIndex.getBulletData().getBulletAmount();
        int rpm = gunData.getRoundsPerMinute();
        double fireInterval = 1/(rpm / 60.0);
        double totalDamage = damage * bulletAmount;


        // 如果可用，使用最大扩容弹匣容量
        int maxAmmoCapacity = gunData.getAmmoAmount();
        if (gunData.getExtendedMagAmmoAmount() != null && gunData.getExtendedMagAmmoAmount().length >= 3) {
            maxAmmoCapacity = Math.max(maxAmmoCapacity, gunData.getExtendedMagAmmoAmount()[2]); // 三级扩容弹匣
        }
        // 检查是否为背包直读模式
        boolean isInventoryMode = gunData.getReloadData().getType() == FeedType.INVENTORY;
        // 4. 装填
        double reloadTime = gunData.getReloadData().getType() == FeedType.MAGAZINE ?
                gunData.getReloadData().getFeed().getEmptyTime() + gunData.getReloadData().getCooldown().getEmptyTime() : 3.0;

        // 5. 散布
        Map<InaccuracyType, Float> inaccuracyMap = gunData.getInaccuracy();
        if (inaccuracyMap == null) {
            inaccuracyMap = InaccuracyType.getDefaultInaccuracy();
        }
        float hipInaccuracy = inaccuracyMap.getOrDefault(InaccuracyType.STAND, 3.0f);

        // 6. 后坐力
        GunRecoil recoil = gunData.getRecoil();
        float averagePitchRecoil = 0;//俯仰
        float averageYawRecoil = 0;//偏航

        if (recoil != null && recoil.getPitch() != null) {
            // 计算平均后坐力
            GunRecoilKeyFrame[] pitchFrames = recoil.getPitch();
            float totalPitchRecoil = 0;
            for (GunRecoilKeyFrame frame : pitchFrames) {
                totalPitchRecoil += (frame.getValue()[0] + frame.getValue()[1]) / 2;
            }
            averagePitchRecoil = totalPitchRecoil / pitchFrames.length;

            GunRecoilKeyFrame[] yawFrames = recoil.getYaw();
            float totalYawRecoil = 0;
            for (GunRecoilKeyFrame frame : yawFrames) {
                totalYawRecoil += (frame.getValue()[0] + frame.getValue()[1]) / 2;
            }
            averageYawRecoil = totalYawRecoil / pitchFrames.length;
        }

        // 7. 穿透
        int pierce = gunData.getBulletData().getPierce();
        float armorIgnore = 0;
        if (gunData.getBulletData().getExtraDamage() != null) {
            armorIgnore = gunData.getBulletData().getExtraDamage().getArmorIgnore();
        }
        // 8. 爆炸
        float explosionDamage = 0;
        float explosionRadius = 0;
        if (gunData.getBulletData().getExplosionData() != null && gunData.getBulletData().getExplosionData().isExplode()) {
            ExplosionData explosionData = gunData.getBulletData().getExplosionData();
            explosionDamage = explosionData.getDamage();
            explosionRadius = explosionData.getRadius();
        }

        // 9. 爆头倍率
        float headShotMultiplier = 1.0f;
        if (gunData.getBulletData().getExtraDamage() != null) {
            headShotMultiplier = gunData.getBulletData().getExtraDamage().getHeadShotMultiplier();
        }


        int averageFireCountIn60s;

        if(isInventoryMode){
            averageFireCountIn60s = rpm;
        }else {
            int currentAmmos = maxAmmoCapacity;
            double time=60.0;;
            int fireCount=0;
            while(time>0){
                if(currentAmmos<=0){
                    time-=reloadTime;
                    currentAmmos=maxAmmoCapacity;
                }
                time-=fireInterval;
                currentAmmos--;
                fireCount++;
            }
            averageFireCountIn60s = fireCount;
        }

        //对于8米外单个目标的命中率
        double accuracy8m1t = 0;
        {
            double spreadRadius = Math.tan(Math.toRadians(hipInaccuracy))*8;
            accuracy8m1t=Math.min(1,(2*1)/(Math.pow(spreadRadius,2)*Math.PI)*(1-averageYawRecoil*0.011)*(1-averagePitchRecoil*0.005));
        }
        //对于15米外单个目标的命中率
        double accuracy15m1t = 0;
        {
            double spreadRadius = Math.tan(Math.toRadians(hipInaccuracy))*15;
            accuracy15m1t=Math.min(1,(2*1)/(Math.pow(spreadRadius,2)*Math.PI)*(1-averageYawRecoil*0.011)*(1-averagePitchRecoil*0.005));
        }
        //对于30米外单个目标的命中率
        double accuracy30m1t = 0;
        {
            double spreadRadius = Math.tan(Math.toRadians(hipInaccuracy))*30;
            accuracy30m1t=Math.min(1,(2*1)/(Math.pow(spreadRadius,2)*Math.PI)*(1-averageYawRecoil*0.011)*(1-averagePitchRecoil*0.005));
        }

        //对于8米外4个菱形排列的目标的命中效率，可以突破100%，表示对多个敌人造成了伤害
        double accuracy8m4t = 1;
        {
            accuracy8m4t += pierce>=2?1:0;
            accuracy8m4t += 3*(explosionDamage/totalDamage);
        }
        //对于8米外5个水平排列，间距2m的目标的命中效率，可以突破100%，表示对多个敌人造成了伤害
        double accuracy8m5t = 1;
        {
            accuracy8m5t += (explosionRadius>=3.8?5:(explosionRadius>=1.9?3:1))*(explosionDamage/totalDamage);
        }
        //对于8米外9个间隔1.5m的矩阵排列的目标的命中效率，可以突破100%，表示对多个敌人造成了伤害
        double accuracy8m9t = 1;
        {
            int hitCount=1;
            if(explosionRadius>=0.9*1.5)hitCount+=3;
            if(explosionRadius>=1.27*1.5)hitCount+=2;
            if(explosionRadius>=1.8*1.5)hitCount+=1;
            if(explosionRadius>=2.07*1.5)hitCount+=2;
            accuracy8m9t += pierce>=3?2:(pierce>=2?1:0);
            accuracy8m9t += (hitCount)*(explosionDamage/totalDamage);
        }

        //计算伤害
        double damage8m1t=averageFireCountIn60s*accuracy8m1t*(getDamageAtDistance(gunIndex,8)+explosionDamage)*((1-headShotMultiplier/2)+1)*(1+armorIgnore);
        double damage15m1t=averageFireCountIn60s*accuracy15m1t*(getDamageAtDistance(gunIndex,15)+explosionDamage)*((1-headShotMultiplier/2)+1)*(1+armorIgnore);
        double damage30m1t=averageFireCountIn60s*accuracy30m1t*(getDamageAtDistance(gunIndex,30)+explosionDamage)*((1-headShotMultiplier/2)+1)*(1+armorIgnore);

        double damage8m4t=averageFireCountIn60s*accuracy8m4t*(getDamageAtDistance(gunIndex,8))*((1-headShotMultiplier/2)+1)*(1+armorIgnore);
        double damage8m5t=averageFireCountIn60s*accuracy8m5t*(getDamageAtDistance(gunIndex,8))*((1-headShotMultiplier/2)+1)*(1+armorIgnore);
        double damage8m9t=averageFireCountIn60s*accuracy8m9t*(getDamageAtDistance(gunIndex,8))*((1-headShotMultiplier/2)+1)*(1+armorIgnore);

        double singleTargetScore=damage8m1t+damage15m1t+damage30m1t;
        double multiTargetScore=damage8m4t/2+damage8m5t/2.5+damage8m9t/4.5;

        return singleTargetScore+multiTargetScore;
    }

    /**
     * 根据固定百分位分配稀有度
     * 分布:
     * IMMORTAL: 前 5%
     * MYTHIC: 接下来 11%
     * LEGENDARY: 接下来 16%
     * EPIC: 接下来 17%
     * RARE: 接下来 17%
     * UNCOMMON: 接下来 19%
     * COMMON: 最后 20%
     */
    private static void assignRaritiesByPercentile(List<Map.Entry<ResourceLocation, Double>> sortedGuns) {
        int totalGuns = sortedGuns.size();
        if (totalGuns == 0) return;

        // 分布百分比

        GunRarity[] rarities = {
                GunRarity.IMMORTAL,   // 7 - 前 5%
                GunRarity.MYTHIC,     // 6 - 接下来 11%
                GunRarity.LEGENDARY,  // 5 - 接下来 16%
                GunRarity.EPIC,       // 4 - 接下来 17%
                GunRarity.RARE,       // 3 - 接下来 17%
                GunRarity.UNCOMMON,   // 2 - 接下来 19%
                GunRarity.COMMON      // 1 - 最后 20%
        };

        int currentIndex = 0;
        for (int rarityIndex = 0; rarityIndex < rarities.length; rarityIndex++) {
            int countForRarity = Math.toIntExact(Math.round(totalGuns * distribution[rarityIndex]));

            // 确保最后一种稀有度包含所有剩余的枪械
            if (rarityIndex == rarities.length - 1) {
                countForRarity = totalGuns - currentIndex;
            }

            // 为此稀有度分配计算出的数量的枪械
            for (int i = 0; i < countForRarity && currentIndex < totalGuns; i++) {
                ResourceLocation gunId = sortedGuns.get(currentIndex).getKey();
                GUN_RARITY_MAP.put(gunId, rarities[rarityIndex]);
                currentIndex++;
            }
        }
    }

    public static GunRarity getGunRarity(ResourceLocation gunId) {
        return GUN_RARITY_MAP.getOrDefault(gunId, GunRarity.ABNORMAL);
    }
    public static float getDamageAtDistance(CommonGunIndex gunIndex, float distance) {
        if (gunIndex == null) {
            return 0f;
        }

        GunData gunData = gunIndex.getGunData();
        if (gunData == null) {
            return 0f;
        }

        BulletData bulletData = gunData.getBulletData();
        float baseDamage = bulletData.getDamageAmount();

        // 应用距离衰减
        ExtraDamage extraDamage = bulletData.getExtraDamage();
        if (extraDamage != null) {
            LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust = extraDamage.getDamageAdjust();
            if (damageAdjust != null && !damageAdjust.isEmpty()) {
                // 找到适合当前距离的伤害值
                for (ExtraDamage.DistanceDamagePair pair : damageAdjust) {
                    if (distance <= pair.getDistance() || Float.isInfinite(pair.getDistance())) {
                        baseDamage = pair.getDamage();
                        break;
                    }
                }
            }
        }
        return baseDamage;
    }
    public static float getDamageMultiplier(GunRarity rarity) {
        return switch (rarity) {
            case COMMON -> 0.75f;
            case UNCOMMON -> 1f;
            case RARE -> 1.3f;
            case EPIC -> 1.7f;
            case LEGENDARY -> 2.15f;
            case MYTHIC -> 2.65f;
            case IMMORTAL -> 3.5f;
            case ABNORMAL -> 1.0f;
            default -> 1.0f;
        };
    }

    public static int getGunRankingValue(GunRarity rarity) {
        return switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case EPIC -> 4;
            case LEGENDARY -> 5;
            case MYTHIC -> 6;
            case IMMORTAL -> 7;
            case ABNORMAL -> -1;
            default -> 0;
        };
    }

    public static Map<ResourceLocation, GunRarity> getGunRarityMap() {
        return new HashMap<>(GUN_RARITY_MAP);
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void clear() {
        GUN_RARITY_MAP.clear();
        isInitialized = false;
    }

    /**
     * 设置枪械稀有度等级
     * @param gun 枪械物品
     * @param ranking 稀有度等级 (普通到不朽为1-7，异常为-1，其余情况为0)
     */
    public static void setGunRanking(ItemStack gun, int ranking) {
        CompoundTag nbt = gun.getOrCreateTag();
        nbt.putInt(GUN_RANKING_TAG, ranking);
    }
}