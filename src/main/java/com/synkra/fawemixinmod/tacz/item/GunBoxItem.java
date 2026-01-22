package com.synkra.fawemixinmod.tacz.item;

import com.synkra.fawemixinmod.config.CommonConfig;
import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GunBoxItem extends Item {
    private final GunRarity rarity;

    public GunBoxItem(GunRarity rarity) {
        super(new Properties().stacksTo(1));
        this.rarity = rarity;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        if (!CommonConfig.ENABLED_GUN_RANDOM.get()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 获取该稀有度的所有枪械
            List<ItemStack> gunsOfRarity = getGunsOfRarity();

            if (!gunsOfRarity.isEmpty()) {
                // 随机选择一个枪械
                Random random = new Random();
                ItemStack gunStack = gunsOfRarity.get(random.nextInt(gunsOfRarity.size()));

                // 给予玩家枪械
                if (!player.getInventory().add(gunStack)) {
                    player.drop(gunStack, false);
                }

                // 消耗枪械盒
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 获取指定稀有度的所有枪械ID
     */
    private List<ItemStack> getGunsOfRarity() {
        return TimelessAPI.getAllCommonGunIndex().stream()
                .filter(entry -> {
                    ResourceLocation gunId = entry.getKey();
                    GunRarity gunRarity = GunRankingManager.getGunRarity(gunId);
                    return gunRarity == this.rarity;
                })
                .map(entry -> {
                    ResourceLocation gunId = entry.getKey();
                    CommonGunIndex index = entry.getValue();
                    // 创建枪械物品
                    return GunItemBuilder.create()
                            .setId(gunId)
                            .setFireMode(index.getGunData().getFireModeSet().get(0))
                            .setAmmoCount(index.getGunData().getAmmoAmount())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        return Component.translatable("item.fawemixinmod.gun_box", Component.translatable("tooltip.fawemixinmod.tacz.gun.rarity." + rarity.getNameKey()));
    }

    @Override
    @Nonnull
    public Rarity getRarity(@NotNull ItemStack stack) {
        return switch (rarity) {
            case UNCOMMON, RARE -> Rarity.UNCOMMON;
            case EPIC, LEGENDARY -> Rarity.RARE;
            case MYTHIC, IMMORTAL -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
    }
}