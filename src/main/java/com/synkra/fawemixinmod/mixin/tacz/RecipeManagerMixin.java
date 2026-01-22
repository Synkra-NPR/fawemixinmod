// 文件: src/main/java/com/synkra/fawemixinmod/mixin/tacz/RecipeManagerFilterMixin.java
package com.synkra.fawemixinmod.mixin.tacz;

import com.synkra.fawemixinmod.config.CommonConfig;
import com.synkra.fawemixinmod.tacz.GunRankingManager;
import com.synkra.fawemixinmod.tacz.GunRarity;
import com.synkra.fawemixinmod.tacz.init.ModItems;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.init.ModRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(value = RecipeManager.class,priority = 242)
public class RecipeManagerMixin {

    @Inject(
            method = "byType(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/Map;",
            at = @At("RETURN"),
            cancellable = true
    )
    private <T extends Recipe<?>> void onByType(RecipeType<T> pRecipeType, CallbackInfoReturnable<Map<ResourceLocation, T>> cir) {
        // 检查是否是枪械合成配方类型，且启用了Mixin枪械配方功能
        if (pRecipeType == ModRecipe.GUN_SMITH_TABLE_CRAFTING.get() && CommonConfig.ENABLED_MIXIN_GUN_RECIPE.get()) {
            Map<ResourceLocation, T> originalRecipes = cir.getReturnValue();

            if (CommonConfig.ENABLED_GUN_RANDOM.get()) {
                // 删除模式：过滤掉枪械配方
                Map<ResourceLocation, T> filteredRecipes = originalRecipes
                        .entrySet()
                        .stream()
                        .filter(entry -> !(entry.getValue() instanceof GunSmithTableRecipe gunSmithRecipe &&
                                gunSmithRecipe.getResult().getResult().getItem() instanceof IGun))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                cir.setReturnValue((Map<ResourceLocation, T>) filteredRecipes);
            } else {
                // 修改模式：不保留原始配方，只添加枪盒合成枪的配方
                Map<ResourceLocation, T> modifiedRecipesMap = new java.util.HashMap<>();

                // 遍历所有原始枪械配方，创建对应的枪盒合成枪配方
                for (Map.Entry<ResourceLocation, T> entry : originalRecipes.entrySet()) {
                    T recipe = entry.getValue();

                    if (recipe instanceof GunSmithTableRecipe gunSmithRecipe &&
                            gunSmithRecipe.getResult().getResult().getItem() instanceof IGun) {

                        Item outputGun = gunSmithRecipe.getResult().getResult().getItem();
                        ResourceLocation gunId = ((IGun)outputGun).getGunId(gunSmithRecipe.getResult().getResult());
                        GunRarity rarity = GunRankingManager.getGunRarity(gunId);
                        // 根据稀有度选择对应的枪盒作为材料
                        Item gunBoxItem = (switch (rarity) {
                            case COMMON -> ModItems.GUN_BOX_MK1.get();
                            case UNCOMMON -> ModItems.GUN_BOX_MK2.get();
                            case RARE -> ModItems.GUN_BOX_MK3.get();
                            case EPIC -> ModItems.GUN_BOX_MK4.get();
                            case LEGENDARY -> ModItems.GUN_BOX_MK5.get();
                            case MYTHIC -> ModItems.GUN_BOX_MK6.get();
                            case IMMORTAL -> ModItems.GUN_BOX_MK7.get();
                            default -> ModItems.GUN_BOX_MK1.get();
                        });

                        List<GunSmithTableIngredient> newIngredients = java.util.Arrays.asList(new GunSmithTableIngredient(Ingredient.of(Items.APPLE), 1));

                        gunSmithRecipe.getInputs().clear();
                        gunSmithRecipe.getInputs().add(new GunSmithTableIngredient(Ingredient.of(gunBoxItem), 1));
                        modifiedRecipesMap.put(entry.getKey(), entry.getValue());
                    } else {
                        // 保留非枪械配方（如附件、弹药等）
                        modifiedRecipesMap.put(entry.getKey(), entry.getValue());
                    }
                }
                cir.setReturnValue(modifiedRecipesMap);
            }
        }
    }
}