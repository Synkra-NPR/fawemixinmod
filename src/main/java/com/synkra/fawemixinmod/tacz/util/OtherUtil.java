package com.synkra.fawemixinmod.tacz.util;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.modifier.custom.RpmModifier;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.tacz.guns.resource.modifier.AttachmentPropertyManager.getModifiers;

public class OtherUtil {
    public static long lastFireTime = 0;
    public static long lastReloadFinishTime = 0;
    public static long lastSelectFireTime = 0;
    public static int getRPM(ItemStack gunItem, GunData gunData){
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 300;
        }
        FireMode fireMode = iGun.getFireMode(gunItem);
        int roundsPerMinute = gunData.getRoundsPerMinute(fireMode);
        Class<?> targetClass = AttachmentDataUtils.class;
        try {
            Method privateStaticMethod = targetClass.getDeclaredMethod(
                    "getModifiers",
                    ItemStack.class ,
                    GunData.class,
                    String.class
            );
            privateStaticMethod.setAccessible(true);
            List<Modifier> modifiers = (List<Modifier>)privateStaticMethod.invoke(null, gunItem, gunData, RpmModifier.ID);
            double eval = AttachmentPropertyManager.eval(modifiers, roundsPerMinute);
            return (int) Math.round(eval);
        }catch (NoSuchMethodException e){
            return roundsPerMinute;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static int calculateCustomPulseColor(float intensity, int baseColor) {
        // 解析传入的颜色
        int alpha = (baseColor >> 24) & 0xFF;
        int red = (baseColor >> 16) & 0xFF;
        int green = (baseColor >> 8) & 0xFF;
        int blue = baseColor & 0xFF;

        // 如果传入的颜色没有Alpha通道（0xRRGGBB格式），使用默认Alpha
        if (alpha == 0) {
            alpha = 0xAF; // 使用与背景相同的Alpha值
        }

        // 根据强度调整颜色亮度
        // 使用更自然的亮度调整：强度低时颜色较暗，强度高时接近原色
        float adjustedIntensity = intensity * 0.8f + 0.2f; // 保证最低亮度

        int adjustedRed = (int)(red * adjustedIntensity);
        int adjustedGreen = (int)(green * adjustedIntensity);
        int adjustedBlue = (int)(blue * adjustedIntensity);

        // 确保值在有效范围内
        adjustedRed = Math.min(255, Math.max(0, adjustedRed));
        adjustedGreen = Math.min(255, Math.max(0, adjustedGreen));
        adjustedBlue = Math.min(255, Math.max(0, adjustedBlue));

        // Alpha值也可以根据强度稍微调整以增强效果
        int adjustedAlpha = (int)(alpha * (0.7f + 0.3f * intensity));
        adjustedAlpha = Math.min(255, Math.max(0, adjustedAlpha));

        // 返回ARGB格式颜色
        return (adjustedAlpha << 24) | (adjustedRed << 16) | (adjustedGreen << 8) | adjustedBlue;
    }
    public static int calculateCustomPulseColorFullAlphaDefault(float intensity, int baseColor) {
        // 解析传入的颜色
        int red = (baseColor >> 16) & 0xFF;
        int green = (baseColor >> 8) & 0xFF;
        int blue = baseColor & 0xFF;


        // 根据强度调整颜色亮度
        // 使用更自然的亮度调整：强度低时颜色较暗，强度高时接近原色
        float adjustedIntensity = intensity * 0.8f + 0.2f; // 保证最低亮度

        int adjustedRed = (int)(red * adjustedIntensity);
        int adjustedGreen = (int)(green * adjustedIntensity);
        int adjustedBlue = (int)(blue * adjustedIntensity);

        // 确保值在有效范围内
        adjustedRed = Math.min(255, Math.max(0, adjustedRed));
        adjustedGreen = Math.min(255, Math.max(0, adjustedGreen));
        adjustedBlue = Math.min(255, Math.max(0, adjustedBlue));

        // Alpha值也可以根据强度稍微调整以增强效果
        int adjustedAlpha = (int)(0xFF * (0.3f + 0.7f * intensity));//默认不透明度为0.3，这里不同
        adjustedAlpha = Math.min(255, Math.max(0, adjustedAlpha));

        // 返回ARGB格式颜色
        return (adjustedAlpha << 24) | (adjustedRed << 16) | (adjustedGreen << 8) | adjustedBlue;
    }
    public static int adjustBrightnessFast(int argb, float brightnessPercent) {
        // 转换为定点数 (0-255范围，1.0 = 256)
        int brightness256 = (int)(brightnessPercent * 256);
        brightness256 = Math.min(512, Math.max(0, brightness256)); // 限制0-512 (0-2.0)

        // 提取并调整RGB
        int red = (((argb >> 16) & 0xFF) * brightness256) >> 8;
        int green = (((argb >> 8) & 0xFF) * brightness256) >> 8;
        int blue = ((argb & 0xFF) * brightness256) >> 8;

        // 限制范围
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        // 组合结果
        return (argb & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }
    public static int adjustAlpha(int argb, float alphaPercent) {
        // 提取原RGB
        int rgb = argb & 0x00FFFFFF;

        // 计算新alpha
        int newAlpha = Math.min(255, Math.max(0, (int)(alphaPercent * 255)));

        // 组合新颜色
        return (newAlpha << 24) | rgb;
    }

    /**
     *
     * @param baseColor ARGB格式的基础颜色
     * @param overlayColor ARGB格式的叠加颜色
     * @param overlayPercent 叠加百分比 (0-1)
     * @return 叠加后的ARGB颜色
     */
    public static int overlayColorFast(int baseColor, int overlayColor, float overlayPercent) {
        // 将百分比转换为定点数 (0-256, 1.0 = 256)
        int overlayRatio = Math.min(256, Math.max(0, (int)(overlayPercent * 256)));

        // 提取覆盖色透明度
        int overlayAlpha = (overlayColor >> 24) & 0xFF;

        // 如果覆盖色完全透明或覆盖百分比为0，直接返回原色
        if (overlayAlpha == 0 || overlayRatio == 0) {
            return baseColor;
        }

        // 计算实际覆盖比例（考虑覆盖色透明度）
        int actualOverlay = (overlayAlpha * overlayRatio) >> 8; // 0-255
        int baseRatio = 255 - actualOverlay;

        // 提取通道
        int baseR = (baseColor >> 16) & 0xFF;
        int baseG = (baseColor >> 8) & 0xFF;
        int baseB = baseColor & 0xFF;

        int overlayR = (overlayColor >> 16) & 0xFF;
        int overlayG = (overlayColor >> 8) & 0xFF;
        int overlayB = overlayColor & 0xFF;

        // 混合RGB
        int blendedR = (baseR * baseRatio + overlayR * actualOverlay) >> 8;
        int blendedG = (baseG * baseRatio + overlayG * actualOverlay) >> 8;
        int blendedB = (baseB * baseRatio + overlayB * actualOverlay) >> 8;

        // 混合Alpha（使用更简单的算法）
        int baseAlpha = (baseColor >> 24) & 0xFF;
        int blendedAlpha = baseAlpha + ((255 - baseAlpha) * actualOverlay >> 8);

        // 确保值在范围内
        blendedR = Math.min(255, Math.max(0, blendedR));
        blendedG = Math.min(255, Math.max(0, blendedG));
        blendedB = Math.min(255, Math.max(0, blendedB));
        blendedAlpha = Math.min(255, Math.max(0, blendedAlpha));

        return (blendedAlpha << 24) | (blendedR << 16) | (blendedG << 8) | blendedB;
    }

    /**
     *
     * @param baseColor ARGB格式的基础颜色
     * @param overlayColor ARGB格式的叠加颜色
     * @param overlayPercent 叠加百分比 (0-1)
     * @return 叠加后的ARGB颜色
     */
    public static int overlayColorKeepAlpha(int baseColor, int overlayColor, float overlayPercent) {
        // 提取底层透明度
        int baseAlpha = (baseColor >> 24) & 0xFF;

        // 混合RGB（忽略覆盖色的透明度）
        int blendedRGB = overlayColorFast(baseColor, overlayColor, overlayPercent);

        // 恢复底层透明度
        return (baseAlpha << 24) | (blendedRGB & 0x00FFFFFF);
    }
    public static void printColor(int color) {
        printColor(color,color+": This is colored text。测试。████████");
    }
    public static void printColor(int color,Object text) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        System.out.println("\033[38;2;" + r + ";" + g + ";" + b + "m" +text.toString()+"\033[0m");
    }
    public static List<Double> reversed(List<Double> originalList) {
        List<Double> reversedList = new ArrayList<>(originalList);
        Collections.reverse(reversedList);
        return reversedList;
    }
}
