package com.circulation.only_one_item;

import com.circulation.only_one_item.handler.InitHandler;
import com.circulation.only_one_item.handler.MatchItemHandler;
import com.circulation.only_one_item.handler.OreDictionaryHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = OnlyOneItem.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
    dependencies = "required-after:mixinbooter@[8.0,);"
)
public class OnlyOneItem {
    public static final String MOD_ID = Tags.MOD_ID;

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static OnlyOneItem instance = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new OreDictionaryHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        OOIConfig.readConfig();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        InitHandler.allPreInit();
        if (!Loader.isModLoaded("unidict") && OOIConfig.clearDuplicateRecipes) {
            MatchItemHandler.clearRecipe();
        } else if (!OOIConfig.clearDuplicateRecipes) {
            LOGGER.info("[OOI] Duplicate recipe cleanup is disabled by config.");
        }
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        MatchItemHandler.postODProcess();
    }

}
