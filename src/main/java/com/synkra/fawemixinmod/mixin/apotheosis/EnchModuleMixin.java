package com.synkra.fawemixinmod.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.placebo.recipe.RecipeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(EnchModule.class)
public class EnchModuleMixin {
    @Redirect(
            method = "init(Lnet/minecraftforge/fml/event/lifecycle/FMLCommonSetupEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/placebo/recipe/RecipeHelper;registerProvider(Ljava/util/function/Consumer;)V"
            ),
            remap = false
    )
    private void redirectRegisterProvider(RecipeHelper helper, Consumer<RecipeHelper.RecipeFactory> consumer) {
        //doNothingToCancelRecipesReg
    }
}
