package com.synkra.fawemixinmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CommonConfig {
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GUN_BOX_BLACK_LIST;
    public static ForgeConfigSpec.BooleanValue ENABLED_MIXIN_GUN_RECIPE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GUN_RARITY_COLOR;
    public static ForgeConfigSpec.ConfigValue<List<? extends Double>> GUN_RARITY_DISTRIBUTION;
    public static ForgeConfigSpec.DoubleValue GUN_DURABILITY_FACTOR;
    public static ForgeConfigSpec.BooleanValue ENABLED_GUN_RANDOM;
    public static final ForgeConfigSpec SPEC;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
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
        GUN_RARITY_DISTRIBUTION = builder.defineList("distribution", Arrays.asList(0.20, 0.19, 0.17, 0.17, 0.16, 0.11, 0.05), o -> o instanceof Double);

        builder.pop();

        builder.push("gun_box");

        builder.comment("枪盒黑名单，这些枪械将被标记为异常稀有度，无法通过随机开启枪盒获取.");
        GUN_BOX_BLACK_LIST = builder.defineList("blackList", Arrays.asList("emxarms:emx_nikana","mpworld:22r","mpworld:qwhdknife2","mpworld:qwhdknife","rcp:at4","rebel:bq01","rebel:t70th","rcp:rpg26","emxarms:emx_mac50flux","blitz:blitz"), o -> o instanceof String);

        builder.pop();

    }
}