package com.synkra.fawemixinmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec.BooleanValue KILLFEED_ENABLED;
    public static ForgeConfigSpec.IntValue KILLFEED_MAX_ENTRIES;
    public static ForgeConfigSpec.IntValue KILLFEED_DURATION_MILLISECONDS;
    public static ForgeConfigSpec.IntValue KILLFEED_POSITION_X_OFFSET;
    public static ForgeConfigSpec.IntValue KILLFEED_POSITION_Y_OFFSET;
    public static ForgeConfigSpec.DoubleValue KILLFEED_SCALE;
    public static final ForgeConfigSpec SPEC;
    public static ForgeConfigSpec.ConfigValue<Boolean> GUN_HUD_OVERLAY_ENABLED_MIXIN;
    public static ForgeConfigSpec.ConfigValue<Boolean> GUN_HUD_OVERLAY_ENABLED_VERSION_MIXIN;
    public static ForgeConfigSpec.ConfigValue<Boolean> GUN_REFIT_SCREEN_ENABLED_MIXIN;



    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }

    private static void init(ForgeConfigSpec.Builder builder) {
        builder.push("kill_feed");

        builder.comment("是否开启击杀播报");
        KILLFEED_ENABLED = builder.define("Enabled", true);

        builder.comment("最大击杀播报条目");
        KILLFEED_MAX_ENTRIES = builder.defineInRange("MaxEntries", 12, 1, 100);

        builder.comment("击杀播报条目持续时间");
        KILLFEED_DURATION_MILLISECONDS = builder.defineInRange("DurationMilliseconds", 5000, 1000, 30000);

        builder.comment("X偏移");
        KILLFEED_POSITION_X_OFFSET = builder.defineInRange("PositionXOffset", 0, -1000, 1000);

        builder.comment("Y偏移");
        KILLFEED_POSITION_Y_OFFSET = builder.defineInRange("PositionYOffset", 60, 0, 1000);

        builder.comment("击杀播报条目缩放");
        KILLFEED_SCALE = builder.defineInRange("Scale", 1.0, 0.1, 5.0);

        builder.pop();

        builder.push("gun_hud_overlay");

        builder.comment("是否修改枪的HUD");
        GUN_HUD_OVERLAY_ENABLED_MIXIN = builder.define("gunHudOverlayEnabledMixin", true);

        builder.comment("是否修该枪的HUD里面的版本(该选项不被上一条选项影响).");
        GUN_HUD_OVERLAY_ENABLED_VERSION_MIXIN = builder.define("gunHudOverlayEnabledVersionMixin", true);

        builder.pop();

        builder.push("gun_refit_screen");

        builder.comment("Whether mixin gun refit screen.");
        GUN_REFIT_SCREEN_ENABLED_MIXIN = builder.define("gunRefitScreenEnabledMixin", true);

        builder.pop();
    }
}