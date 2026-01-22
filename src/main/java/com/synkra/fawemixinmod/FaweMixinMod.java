package com.synkra.fawemixinmod;

import com.mojang.logging.LogUtils;
import com.synkra.fawemixinmod.config.ClientConfig;
import com.synkra.fawemixinmod.config.CommonConfig;
import com.synkra.fawemixinmod.tacz.GunRarity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import com.synkra.fawemixinmod.tacz.init.ModCreativeTabs;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FaweMixinMod.MODID)
public class FaweMixinMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "fawemixinmod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    public FaweMixinMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        // Register configs
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC,"fawemixinmod-Client.toml");
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC,"fawemixinmod-Common.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }
}

