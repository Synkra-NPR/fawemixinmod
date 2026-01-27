package com.synkra.fawemixinmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.DistExecutor;

public class CommonConfig {
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GUN_BOX_BLACK_LIST;
    public static ForgeConfigSpec.BooleanValue ENABLED_MIXIN_GUN_RECIPE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GUN_RARITY_COLOR;
    public static ForgeConfigSpec.ConfigValue<List<? extends Double>> GUN_RARITY_DISTRIBUTION;
    public static ForgeConfigSpec.DoubleValue GUN_DURABILITY_FACTOR;
    public static ForgeConfigSpec.BooleanValue ENABLED_GUN_RANDOM;
    public static ForgeConfigSpec.ConfigValue<List<? extends Double>> GUN_DAMAGE_MODIFIER;
    public static final ForgeConfigSpec SPEC;
    
    // 用于客户端同步的变量
    private static boolean client_enabledMixinGunRecipe = true;
    private static boolean client_enabledGunRandom = true;
    private static double client_gunDurabilityFactor = 1.0;
    private static List<Integer> client_gunRarityColor = Arrays.asList(0xD9D9D9, 0x64D679, 0x3282F6, 0xC635D1, 0xF09B59, 0xBD291D, 0xFFFD55, 0xAAAAAA);
    private static List<Double> client_gunRarityDistribution = Arrays.asList(0.20, 0.18, 0.16, 0.16, 0.15, 0.10, 0.05);
    private static List<Double> client_gunDamageModifier = Arrays.asList(0.75,1.0,1.3,1.7,2.15,2.65,3.5,1.0);
    private static List<String> client_gunBoxBlackList = Arrays.asList("emxarms:emx_nikana","mpworld:22r","mpworld:qwhdknife2","mpworld:qwhdknife","rcp:at4","rebel:bq01","rebel:t70th","rcp:rpg26","emxarms:emx_mac50flux","blitz:blitz","rebel:fmc");
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }
    
    // 从服务器同步配置的方法
    public static void syncFromServer(boolean enabledMixinGunRecipe, boolean enabledGunRandom,
                                    double gunDurabilityFactor, List<Integer> gunRarityColor,
                                    List<Double> gunRarityDistribution, List<Double> gunDamageModifier,
                                    List<String> gunBoxBlackList) {
        // 只在客户端运行时更新客户端配置值
        if (FMLEnvironment.dist.isClient()) {
            client_enabledMixinGunRecipe = enabledMixinGunRecipe;
            client_enabledGunRandom = enabledGunRandom;
            client_gunDurabilityFactor = gunDurabilityFactor;
            client_gunRarityColor = gunRarityColor;
            client_gunRarityDistribution = gunRarityDistribution;
            client_gunDamageModifier = gunDamageModifier;
            client_gunBoxBlackList = gunBoxBlackList;
        }
    }
    
    // 获取配置值，自动根据环境决定使用哪个值
    public static boolean getClientEnabledMixinGunRecipe() {
        return DistExecutor.safeRunForDist(
            () -> () -> client_enabledMixinGunRecipe,
            () -> () -> ENABLED_MIXIN_GUN_RECIPE.get()
        );
    }
    
    public static boolean getClientEnabledGunRandom() {
        return DistExecutor.safeRunForDist(
            () -> () -> client_enabledGunRandom,
            () -> () -> ENABLED_GUN_RANDOM.get()
        );
    }
    
    public static double getClientGunDurabilityFactor() {
        return DistExecutor.safeRunForDist(
            () -> () -> client_gunDurabilityFactor,
            () -> () -> GUN_DURABILITY_FACTOR.get()
        );
    }
    
    public static List<Integer> getClientGunRarityColor() {
        return (List<Integer>) DistExecutor.safeRunForDist(
            () -> () -> client_gunRarityColor,
            () -> () -> GUN_RARITY_COLOR.get()
        );
    }
    
    public static List<Double> getClientGunRarityDistribution() {
        return (List<Double>) DistExecutor.safeRunForDist(
            () -> () -> client_gunRarityDistribution,
            () -> () -> GUN_RARITY_DISTRIBUTION.get()
        );
    }
    
    public static List<Double> getClientGunDamageModifier() {
        return (List<Double>) DistExecutor.safeRunForDist(
            () -> () -> client_gunDamageModifier,
            () -> () -> GUN_DAMAGE_MODIFIER.get()
        );
    }
    
    public static List<String> getClientGunBoxBlackList() {
        return (List<String>) DistExecutor.safeRunForDist(
            () -> () -> client_gunBoxBlackList,
            () -> () -> GUN_BOX_BLACK_LIST.get()
        );
    }

    private static void init(ForgeConfigSpec.Builder builder) {
        builder.push("gunDurability");

        builder.comment("枪械耐久度系数.");
        GUN_DURABILITY_FACTOR = builder.defineInRange("gunDurabilityFactor", 1.0,0.1,999999);

        builder.pop();
        builder.push("gun_recipe");

        builder.comment("启用枪械配方修改.");
        ENABLED_MIXIN_GUN_RECIPE = builder.define("enabledMixinGunRecipe", true);

        builder.comment("启用右键打开枪盒随机获取枪械，如果禁用，则枪盒不可打开，同时枪械可以在原来的枪匠台使用一个对应的枪盒合成.");
        ENABLED_GUN_RANDOM = builder.define("enabledBoxGunRandom", true);

        builder.pop();

        builder.push("gun_rarity");

        builder.comment("枪械稀有度颜色.(普通,稀有,精良,史诗,传说,神话,不朽,异常)");
        GUN_RARITY_COLOR = builder.defineList("color", Arrays.asList(0xD9D9D9, 0x64D679, 0x3282F6, 0xC635D1, 0xF09B59, 0xBD291D, 0xFFFD55, 0xAAAAAA), o -> o instanceof Integer);

        builder.comment("枪械稀有度分布.(普通,稀有,精良,史诗,传说,神话,不朽)");
        GUN_RARITY_DISTRIBUTION = builder.defineList("distribution", Arrays.asList(0.20, 0.18, 0.16, 0.16, 0.15, 0.10, 0.05), o -> o instanceof Double);

        builder.comment("枪械伤害倍率修正.(普通,稀有,精良,史诗,传说,神话,不朽,异常)");
        GUN_DAMAGE_MODIFIER = builder.defineList("gunDamageModifier", Arrays.asList(0.75,1.0,1.3,1.7,2.15,2.65,3.5,1.0), o -> o instanceof Double);


        builder.pop();

        builder.push("gun_box");

        builder.comment("枪盒黑名单，这些枪械将被标记为异常稀有度，无法通过随机开启枪盒获取.");
        GUN_BOX_BLACK_LIST = builder.defineList("blackList", Arrays.asList("emxarms:emx_nikana","mpworld:22r","mpworld:qwhdknife2","mpworld:qwhdknife","rcp:at4","rebel:bq01","rebel:t70th","rcp:rpg26","emxarms:emx_mac50flux","blitz:blitz","rebel:fmc"), o -> o instanceof String);

        builder.pop();

    }
}