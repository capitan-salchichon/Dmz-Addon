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
    }

    @SubscribeEvent
    public void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) GrowthNetwork.sync(sp);
    }

    @SubscribeEvent
    public void dim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) GrowthNetwork.sync(sp);
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
