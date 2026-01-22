package com.synkra.fawemixinmod.tacz.init;

import com.synkra.fawemixinmod.tacz.GunRarity;
import com.synkra.fawemixinmod.tacz.item.GunBoxItem;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import static com.synkra.fawemixinmod.FaweMixinMod.ITEMS;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static RegistryObject<Item> GUN_BOX_MK1 = ITEMS.register("gun_box_mk1", () -> new GunBoxItem(GunRarity.COMMON));
    public static RegistryObject<Item> GUN_BOX_MK2 = ITEMS.register("gun_box_mk2", () -> new GunBoxItem(GunRarity.UNCOMMON));
    public static RegistryObject<Item> GUN_BOX_MK3 = ITEMS.register("gun_box_mk3", () -> new GunBoxItem(GunRarity.RARE));
    public static RegistryObject<Item> GUN_BOX_MK4 = ITEMS.register("gun_box_mk4", () -> new GunBoxItem(GunRarity.EPIC));
    public static RegistryObject<Item> GUN_BOX_MK5 = ITEMS.register("gun_box_mk5", () -> new GunBoxItem(GunRarity.LEGENDARY));
    public static RegistryObject<Item> GUN_BOX_MK6 = ITEMS.register("gun_box_mk6", () -> new GunBoxItem(GunRarity.MYTHIC));
    public static RegistryObject<Item> GUN_BOX_MK7 = ITEMS.register("gun_box_mk7", () -> new GunBoxItem(GunRarity.IMMORTAL));
}