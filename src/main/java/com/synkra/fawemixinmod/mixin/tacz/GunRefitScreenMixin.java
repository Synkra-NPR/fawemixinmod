package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.config.ClientConfig;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.client.gui.components.FlatColorButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.synkra.fawemixinmod.tacz.GunRankingManager.GUN_RANKING_TAG;
import static com.synkra.fawemixinmod.tacz.util.OtherUtil.calculateCustomPulseColor;

@Mixin(GunRefitScreen.class)
public class GunRefitScreenMixin extends ScreenMixin {
    @Unique
    private static long faweMixin$startTime = 0;
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void onConstructorHead(CallbackInfo ci) {
        faweMixin$startTime = System.currentTimeMillis();
    }
    @Redirect(
            method = "init()V",
            at = @At(
                    value = "NEW",
                    target = "Lcom/tacz/guns/client/gui/components/FlatColorButton;",
                    remap = false,
                    ordinal = 0
            )
    )
    private FlatColorButton replaceFlatButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        if(ClientConfig.GUN_REFIT_SCREEN_ENABLED_MIXIN.get()){
            return faweMixin$createPulseButton(x, y, width, height, message, onPress);
        }else {
            return new FlatColorButton(x, y, width, height, message, onPress);
        }
    }
    @Redirect(
            method = "init()V",
            at = @At(
                    value = "NEW",
                    target = "Lcom/tacz/guns/client/gui/components/FlatColorButton;",
                    remap = false,
                    ordinal = 2
            )
    )
    private FlatColorButton replaceFlatButton_2(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        if(ClientConfig.GUN_REFIT_SCREEN_ENABLED_MIXIN.get()){
            return faweMixin$createPulseButton(x, y+7, width, height, message, onPress); // 原版的这个按钮y位置似乎有问题，这里修复一下,+个7
        }else {
            return new FlatColorButton(x, y+7, width, height, message, onPress);
        }
    }

    @Unique
    private FlatColorButton faweMixin$createPulseButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        return new FlatColorButton(x, y, width, height, message, onPress) {
            private int pulseColor= -1;
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                LocalPlayer player = getMinecraft().player;
                Inventory inventory = player.getInventory();
                ItemStack gunItem = inventory.getSelected();


                Minecraft minecraft = Minecraft.getInstance();
                Font font = minecraft.font;

                // 计算动画进度
                long currentTime = System.currentTimeMillis();
                float progress = ((currentTime - faweMixin$startTime) % 1600L) / 1600.0f; // 0.0到1.0循环
                if(pulseColor==-1) {
                    if (gunItem.getTag() != null) {
                        pulseColor = GunRarity.getColor(gunItem.getTag().getInt(GUN_RANKING_TAG));
                    } else {
                        pulseColor = 0xAF2222;
                    }
                }
                // 绘制自定义颜色的脉冲扫描效果
                drawPulse(graphics, progress, pulseColor);

                // 绘制边框（悬停时更明显）
                if (this.isHoveredOrFocused()) {
                    graphics.fillGradient(this.getX(), this.getY() + 1, this.getX() + 1, this.getY() + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
                    graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0xff_F3EFE0, 0xff_F3EFE0);
                    graphics.fillGradient(this.getX() + this.width - 1, this.getY() + 1, this.getX() + this.width, this.getY() + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
                    graphics.fillGradient(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
                }

                this.renderScrollingString(graphics, font, 2, 0xF3EFE0);
                this.renderToolTip(graphics, minecraft.screen, mouseX, mouseY);
            }

            private void drawPulse(GuiGraphics graphics, float progress, int pulseColor) {
                int x = this.getX();
                int y = this.getY();
                int width = this.getWidth();
                int height = this.getHeight();

                // 定义脉冲宽度（按钮宽度的百分比）
                float pulseWidth = 0.8f; // 脉冲宽度为按钮宽度的比例

                // 计算脉冲起始位置
                float pulseStartX = progress * (1.0f + pulseWidth) - pulseWidth;

                // 在按钮上绘制脉冲效果
                for (int i = 0; i < width; i++) {
                    float pixelPos = (float)i / width;

                    // 计算该像素在脉冲中的位置（-0.5到0.5）
                    float pulsePos = (pixelPos - pulseStartX) / pulseWidth;

                    // 如果在脉冲范围内
                    if (pulsePos >= 0.0f && pulsePos <= 1.0f) {
                        // 计算脉冲强度（使用余弦曲线实现平滑过渡）
                        float intensity = (float)((Math.cos(pulsePos * Math.PI * 2 - Math.PI) + 1.0f) / 2.0f);

                        // 计算自定义脉冲颜色
                        int customColor = calculateCustomPulseColor(intensity, pulseColor);

                        // 绘制像素
                        graphics.fill(x + i, y, x + i + 1, y + height, customColor);
                    }else{
                        graphics.fill(x + i, y, x + i + 1, y + height, calculateCustomPulseColor(0, pulseColor));
                    }
                }
            }
        };
    }
}