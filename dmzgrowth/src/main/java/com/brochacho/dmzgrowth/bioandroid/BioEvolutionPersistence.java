package com.brochacho.dmzgrowth.bioandroid;

import com.brochacho.dmzgrowth.GrowthConfig;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * La evolucion del Bio-Android es permanente: DMZ borra la forma activa al morir, asi que se recuerda la ultima forma de la
 * linea "bioevolution" (una vez por segundo) y se restaura al reaparecer. Solo esa raza y esa linea.
 */
public class BioEvolutionPersistence {
    private static final String RACE = "bioandroid";
    private static final String GROUP = "bioevolution";

    @SubscribeEvent
    public void mirror(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp) || sp.tickCount % 20 != 0) return;
        if (!GrowthConfig.BIO_EVOLUTION_SURVIVES_DEATH.get()) return;
        sp.getCapability(StatsCapability.INSTANCE).ifPresent(data -> {
            var c = data.getCharacter();
            if (!RACE.equals(c.getRaceName()) || !GROUP.equalsIgnoreCase(c.getActiveFormGroup()) || !c.hasActiveForm()) return;
            sp.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
                if (!c.getActiveForm().equals(g.getBioForm())) g.rememberBioForm(c.getActiveFormGroup(), c.getActiveForm());
            });
        });
    }

    @SubscribeEvent
    public void restore(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || !GrowthConfig.BIO_EVOLUTION_SURVIVES_DEATH.get()) return;
        sp.getCapability(StatsCapability.INSTANCE).ifPresent(data -> {
            var c = data.getCharacter();
            if (!RACE.equals(c.getRaceName()) || c.hasActiveForm()) return;
            sp.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
                String group = g.getBioFormGroup(), form = g.getBioForm();
                if (group.isEmpty() || form.isEmpty()) return;
                var cfg = ConfigManager.getFormGroup(RACE, group);
                if (cfg == null || cfg.getForm(form) == null) return; // la forma ya no existe (p. ej. fue eliminada)
                c.setActiveForm(group, form);
                NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(sp), sp);
            });
        });
    }
}
