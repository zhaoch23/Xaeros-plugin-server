package com.zhaoch23.xaerospatch;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = "xaerospatch",
        name = "Xaeros' Minimap Patch",
        version = "1.0",
        dependencies = "required-after:xaerominimap@[25.2.6,)"
)
public class XaerosPatch {

    private static Logger logger;

    public XaerosPatch() {
        // Constructor
    }

    public static Logger getLogger() {
        return logger;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Xaeros' Minimap Patch is initializing");
    }
}
