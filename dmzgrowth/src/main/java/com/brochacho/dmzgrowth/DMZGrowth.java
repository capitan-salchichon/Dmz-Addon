package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.net.GrowthNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DMZGrowth.MOD_ID)
public class DMZGrowth {
    public static final String MOD_ID = "dmzgrowth";

    public DMZGrowth() {
        com.brochacho.dmzgrowth.ability.Abilities.registerAll();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GrowthConfig.SPEC);
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(GrowthNetwork::register));
        bus.addListener(GrowthEvents::registerCaps);
        MinecraftForge.EVENT_BUS.register(new GrowthEvents());
        MinecraftForge.EVENT_BUS.register(new ExtremeTrainingHandler());
        MinecraftForge.EVENT_BUS.register(new PresenceBroadcaster());
        MinecraftForge.EVENT_BUS.register(new com.brochacho.dmzgrowth.namek.AssimilationSequences());
    }
}
