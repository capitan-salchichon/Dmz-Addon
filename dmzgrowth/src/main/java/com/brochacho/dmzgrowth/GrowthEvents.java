package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import com.brochacho.dmzgrowth.data.GrowthProvider;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.stats.StatsCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GrowthEvents {
    private static final java.util.Map<java.util.UUID, Integer> PENDING = new java.util.HashMap<>();

    /** El cliente puede reemplazar su jugador unos ticks despues: se reenvia la sincronizacion una vez mas. */
    private static void schedule(ServerPlayer sp) { PENDING.put(sp.getUUID(), 20); }

    @SubscribeEvent
    public void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END || PENDING.isEmpty()) return;
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        var it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            if (en.setValue(en.getValue() - 1) > 1) continue;
            ServerPlayer sp = server.getPlayerList().getPlayer(en.getKey());
            if (sp != null) GrowthNetwork.sync(sp);
            it.remove();
        }
    }

    private static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath(DMZGrowth.MOD_ID, "growth");

    public static void registerCaps(RegisterCapabilitiesEvent e) { e.register(GrowthData.class); }

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<Entity> e) {
        if (!(e.getObject() instanceof Player)) return;
        GrowthProvider p = new GrowthProvider();
        e.addCapability(KEY, p);
        e.addListener(p::invalidate);
    }

    /** Muerte y cambio de dimension: se COPIA, nunca se vuelve a tirar el Potencial. */
    @SubscribeEvent
    public void clone(PlayerEvent.Clone e) {
        e.getOriginal().reviveCaps();
        e.getOriginal().getCapability(GrowthCapability.INSTANCE).ifPresent(old ->
                e.getEntity().getCapability(GrowthCapability.INSTANCE).ifPresent(n -> n.copyFrom(old)));
        e.getOriginal().invalidateCaps();
    }

    /** Personajes ya creados antes del addon (sin tirada): se tira una vez al entrar. */
    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        rollIfCreatedAndMissing(sp);
        GrowthNetwork.sync(sp);
        schedule(sp);
    }

    @SubscribeEvent
    public void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) { GrowthNetwork.sync(sp); schedule(sp); }
    }

    @SubscribeEvent
    public void dim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) { GrowthNetwork.sync(sp); schedule(sp); }
    }

    public static void rollIfCreatedAndMissing(ServerPlayer sp) {
        sp.getCapability(StatsCapability.INSTANCE).ifPresent(stats -> {
            if (!stats.getStatus().isHasCreatedCharacter()) return;
            sp.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
                double power = BasePower.of(stats);
                if (!g.rollInitial(sp.getRandom(), power)) g.migrateToPowerUnits(power);
            });
        });
    }
}
