package com.synkra.fawemixinmod.tacz.client;

import com.synkra.fawemixinmod.tacz.GunRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GunRankingClientCache {
    private static final Map<ResourceLocation, GunRarity> GUN_RARITY_CACHE = new HashMap<>();

    public static void setGunRarities(Map<ResourceLocation, GunRarity> gunRarities) {
        GUN_RARITY_CACHE.clear();
        GUN_RARITY_CACHE.putAll(gunRarities);
    }

    public static GunRarity getGunRarity(ResourceLocation gunId) {
        return GUN_RARITY_CACHE.getOrDefault(gunId, GunRarity.COMMON);
    }

    public static void clear() {
        GUN_RARITY_CACHE.clear();
    }
}