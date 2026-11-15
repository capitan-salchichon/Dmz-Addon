package com.brochacho.dmzgrowth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

/**
 * Codigo SOLO de cliente (usa Minecraft.getInstance().player). Debe vivir aparte de los paquetes: un servidor dedicado carga la
 * clase del paquete al registrarla, y si esta referenciara LocalPlayer el servidor se caeria.
 */
public final class ClientSyncHandler {
    private ClientSyncHandler() {}

    public static void handle(CompoundTag tag, double factor, double overload, int seconds) {
        if (tag == null) return;
        ClientGrowthCache.store(tag, factor, overload, seconds);
        ClientGrowthCache.applyTo(Minecraft.getInstance().player);
    }
}
