package com.synkra.fawemixinmod.tacz.init;

import com.synkra.fawemixinmod.FaweMixinMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("all")
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FaweMixinMod.MODID);

    public static RegistryObject<CreativeModeTab> OTHER_TAB = TABS.register("fawemixinmod", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tab.fawemixinmod"))
            .icon(() -> ModItems.GUN_BOX_MK1.get().getDefaultInstance())
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((parameters, output) -> {
                // 添加枪械盒物品
                output.accept(ModItems.GUN_BOX_MK1.get().getDefaultInstance());
                output.accept(ModItems.GUN_BOX_MK2.get().getDefaultInstance());
                output.accept(ModItems.GUN_BOX_MK3.get().getDefaultInstance());
                output.accept(ModItems.GUN_BOX_MK4.get().getDefaultInstance());
                output.accept(ModItems.GUN_BOX_MK5.get().getDefaultInstance());
                output.accept(ModItems.GUN_BOX_MK6.get().getDefaultInstance());
                output.accept(ModItems.GUN_BOX_MK7.get().getDefaultInstance());
            }).build());
}
