package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.bioandroid.AcquiredRegeneration;
import com.brochacho.dmzgrowth.bioandroid.AcquiredZenkai;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.init.entities.namek.NamekVillagerEntity;
import com.dragonminez.common.init.entities.namek.NamekWarriorEntity;
import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.racial.impl.BioAndroidEvolution;
import com.dragonminez.common.stats.StatsCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(value = BioAndroidEvolution.class, remap = false)
public abstract class BioAndroidDrainMixin {
    private static final ThreadLocal<Entity> DRAINING = new ThreadLocal<>();

    /** Antes del segundo de drenaje: recuerda quien es el objetivo actual. */
    @Inject(method = "onSecond", at = @At("HEAD"))
    private void dmzgrowth$before(RacialContext ctx, CallbackInfo ci) {
        int id = ctx.data().getStatus().getDrainingTargetId();
        DRAINING.set(id == -1 ? null : ctx.player().level().getEntity(id));
    }

    /**
     * Despues: si DMZ termino la absorcion en este segundo (limpio el objetivo y lo mato), el objetivo era namekiano o
     * saiyan y la absorbedora es una bio-android, se otorga la habilidad correspondiente.
     */
    @Inject(method = "onSecond", at = @At("RETURN"))
    private void dmzgrowth$after(RacialContext ctx, CallbackInfo ci) {
        Entity target = DRAINING.get();
        DRAINING.remove();
        if (target == null || target.isAlive() || ctx.data().getStatus().getDrainingTargetId() != -1) return;

        String key = race(target);
        if (key == null) return;
        ServerPlayer player = ctx.player();
        player.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
            if (g.acquire(key)) GrowthNetwork.sync(player);
        });
    }

    /** "regeneration" si el absorbido es namekiano, "zenkai" si es saiyan, null en otro caso. */
    private static String race(Entity target) {
        if (target instanceof ServerPlayer tp) {
            String race = tp.getCapability(StatsCapability.INSTANCE).map(d -> d.getCharacter().getRaceName()).orElse("");
            if ("namekian".equals(race)) return AcquiredRegeneration.KEY;
            if ("saiyan".equals(race)) return AcquiredZenkai.KEY;
            return null;
        }
        String name = target.getName().getString().toLowerCase(Locale.ROOT);
        if (target instanceof NamekWarriorEntity || target instanceof NamekVillagerEntity || name.contains("piccolo"))
            return AcquiredRegeneration.KEY;
        // NPC saiyan: DMZ no tiene una clase propia, se reconoce por el nombre (aproximado).
        if (name.contains("saiyan") || name.contains("raditz") || name.contains("nappa") || name.contains("vegeta") || name.contains("bardock"))
            return AcquiredZenkai.KEY;
        return null;
    }
}
