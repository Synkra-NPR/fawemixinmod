package com.synkra.fawemixinmod.tacz.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.synkra.fawemixinmod.config.ClientConfig;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.synkra.fawemixinmod.tacz.client.GunRankingClientCache;
import com.tacz.guns.api.item.IGun;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KillFeedOverlay implements IGuiOverlay {
    private static final KillFeedOverlay INSTANCE = new KillFeedOverlay();
    private static final List<KillFeedEntry> killEntries = new CopyOnWriteArrayList<>();
    private static final int ENTRY_HEIGHT = 13; // 每个条目的高度

    public static KillFeedOverlay getInstance() {
        return INSTANCE;
    }

    public void addKillEntry(String killerName, String victimName, ItemStack weapon, boolean isKillerPlayer) {
        // 检查配置是否已启用
        if (ClientConfig.KILLFEED_ENABLED != null && !ClientConfig.KILLFEED_ENABLED.get()) {
            return;
        }

        // 添加新条目到列表开头
        killEntries.add(0, new KillFeedEntry(killerName, victimName, weapon, isKillerPlayer));
        // 限制最大条目数量
        if (ClientConfig.KILLFEED_MAX_ENTRIES != null) {
            int maxEntries = ClientConfig.KILLFEED_MAX_ENTRIES.get();
            if (killEntries.size() > maxEntries) {
                killEntries.remove(killEntries.size() - 1);
            }
        }

        // 为新条目之后的所有条目启动下移动画
        long currentTime = System.currentTimeMillis();
        for (int i = 1; i < killEntries.size(); i++) {
            KillFeedEntry entry = killEntries.get(i);
            entry.setYAnimationStartTime(currentTime);
            entry.setYOffset(0); // 重置动画起始位置
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        // 检查配置是否已启用
        if (ClientConfig.KILLFEED_ENABLED != null && !ClientConfig.KILLFEED_ENABLED.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        updateAnimations();

        // 计算起始位置
        int startY = ClientConfig.KILLFEED_POSITION_Y_OFFSET != null ? ClientConfig.KILLFEED_POSITION_Y_OFFSET.get() : 60;
        int maxEntries= ClientConfig.KILLFEED_MAX_ENTRIES.get();
        for (int i = 0; i < maxEntries; i++) {
            if(i>=killEntries.size()){
                break;
            }
            KillFeedEntry entry = killEntries.get(i);

            long lifeTime = entry.getLifeTime();

            // 计算透明度（淡出效果）
            float alpha = 1.0f;
            int duration = ClientConfig.KILLFEED_DURATION_MILLISECONDS != null ? ClientConfig.KILLFEED_DURATION_MILLISECONDS.get() : 5000;
            if (lifeTime > duration - 1000) { // 最后1秒淡出
                alpha = 1.0f - (lifeTime - (duration - 1000)) / 1000.0f;
            }

            if (alpha <= 0.05) {
                continue;
            }
            
            // 计算位置 - 从左侧滑入
            int currentY = startY + i * ENTRY_HEIGHT + (int) entry.getYOffset();
            int baseX = 10 + (ClientConfig.KILLFEED_POSITION_X_OFFSET != null ? ClientConfig.KILLFEED_POSITION_X_OFFSET.get() : 0); // 紧贴左边缘
            int textX = baseX + (int) entry.getXOffset();
            // 绘制击杀者名字
            int killerNameColor = (player.getName().getString().equals(entry.getKillerName())) ?
                    ChatFormatting.YELLOW.getColor() : 0xFFFFFF;
            graphics.drawString(mc.font, entry.getKillerName(), textX, currentY,
                    ((int) (alpha * 255) << 24) | killerNameColor, false);

            // 计算武器图标位置
            ItemStack weapon = entry.getWeapon().isEmpty() ? new ItemStack(Items.BONE) : entry.getWeapon();
            int iconX = textX + mc.font.width(entry.getKillerName()) + 5;


            // 设置透明度并渲染图标（调整Y位置）
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            graphics.renderItem(weapon, iconX, currentY - 3); // 调整Y位置，减少向上偏移
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // 如果是枪械，绘制稀有度信息（在玩家击杀时，紧挨在击杀者名字下面靠右）
            if (weapon.getItem() instanceof IGun && entry.isKillerPlayer()) {
                IGun iGun = (IGun) weapon.getItem();
                ResourceLocation gunId = iGun.getGunId(weapon);
                GunRarity gunRarity = GunRankingClientCache.getGunRarity(gunId);

                // 设置分级颜色
                int levelColor = GunRarity.getColor(gunRarity);
                // 使用统一的稀有度文本
                Component rarityText = Component.translatable("tooltip.fawemixinmod.tacz.gun.killfeed.rarity." + gunRarity.getNameKey());

                // 缩小文字绘制（紧挨在击杀者名字下面靠右）
                PoseStack pose = graphics.pose();
                pose.pushPose();
                float scale = 0.5f;
                pose.scale(scale, scale, 1.0f);

                // 计算缩放后的坐标（与击杀者名字右对齐，并更靠近）
                int killerNameWidth = mc.font.width(entry.getKillerName());
                int scaledX = (int) ((textX + killerNameWidth - mc.font.width(rarityText.getString()) * scale) / scale);
                int scaledY = (int) ((currentY + 9) / scale); // 下移0.6行（原来是6，现在改为9）
                graphics.drawString(mc.font, rarityText.getString(), scaledX, scaledY,
                        ((int) (alpha * 255) << 24) | levelColor, false);
                pose.popPose();
            }

            // 绘制被击杀者名字
            int victimNameColor = (player.getName().getString().equals(entry.getVictimName())) ?
                    ChatFormatting.YELLOW.getColor() : 0xFFFFFF;
            int victimX = iconX + 20;
            graphics.drawString(mc.font, entry.getVictimName(), victimX, currentY,
                    ((int) (alpha * 255) << 24) | victimNameColor, false);
        }
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        List<KillFeedEntry> entriesToRemove = new ArrayList<>();
        int duration = ClientConfig.KILLFEED_DURATION_MILLISECONDS != null ? ClientConfig.KILLFEED_DURATION_MILLISECONDS.get() : 5000;

        for (KillFeedEntry entry : killEntries) {
            long lifeTime = currentTime - entry.getTimestamp();
            entry.setLifeTime(lifeTime);

            // 移除过期条目
            if (lifeTime > duration) {
                entriesToRemove.add(entry);
                continue;
            }

            // 更新滑入动画（300ms完成）- 从左侧滑入
            if (lifeTime < 300) {
                // 使用缓动函数实现先快后慢的效果
                double progress = lifeTime / 300.0;
                double easeOutProgress = 1 - (1 - progress) * (1 - progress);
                entry.setXOffset(-100 * (1 - easeOutProgress)); // 从左向右滑入
            } else {
                entry.setXOffset(0); // 动画结束，保持在原位
            }

            // 处理下移动画
            if (entry.getYAnimationStartTime() > 0) {
                long yAnimTime = currentTime - entry.getYAnimationStartTime();
                if (yAnimTime < 300) { // 300毫秒完成下移
                    double progress = yAnimTime / 300.0;
                    double easeOutProgress = 1 - (1 - progress) * (1 - progress);
                    entry.setYOffset(ENTRY_HEIGHT * easeOutProgress - ENTRY_HEIGHT);
                } else {
                    entry.setYOffset(0);
                    entry.setYAnimationStartTime(0); // 重置动画状态
                }
            }
        }

        int removedCount = entriesToRemove.size();
        killEntries.removeAll(entriesToRemove);
    }
}