package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.DMZGrowth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DMZGrowth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientGrowthEvents {
    private ClientGrowthEvents() {}

    /** El jugador del cliente se reemplaza al cambiar de dimension: se le copian los datos guardados. */
    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone e) { ClientGrowthCache.applyTo(e.getNewPlayer()); }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) { ClientGrowthCache.clear(); }
}
