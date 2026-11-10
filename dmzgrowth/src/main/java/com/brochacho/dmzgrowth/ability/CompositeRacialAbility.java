package com.brochacho.dmzgrowth.ability;

import com.dragonminez.common.racial.HealSource;
import com.dragonminez.common.racial.LethalContext;
import com.dragonminez.common.racial.RacialAbility;
import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.world.damagesource.DamageSource;

import java.util.List;

/**
 * Hace que DMZ (que solo admite UNA habilidad racial por jugador) despache cada hook a TODAS las habilidades:
 * la racial original de DMZ (primary, si existe) + las del addon. Eventos: se llaman todas. Modificadores: se
 * encadenan. La accion con tecla propia de DMZ (activar/cargar) sigue siendo solo de la racial original.
 */
public final class CompositeRacialAbility implements RacialAbility {
    private final RacialAbility primary;
    private final List<RacialAbility> parts;

    public CompositeRacialAbility(RacialAbility primary, List<? extends RacialAbility> extras) {
        this.primary = primary;
        var all = new java.util.ArrayList<RacialAbility>(extras.size() + 1);
        if (primary != null) all.add(primary);
        all.addAll(extras);
        this.parts = List.copyOf(all);
    }

    @Override public String id() { return primary != null ? primary.id() : parts.get(0).id(); }

    // --- Accion con tecla de DMZ: solo la racial original ---
    @Override public boolean canActivate(RacialContext c) { return primary != null && primary.canActivate(c); }
    @Override public boolean hasActiveAction() { return primary != null && primary.hasActiveAction(); }
    @Override public boolean hasActiveAction(StatsData d) { return primary != null && primary.hasActiveAction(d); }
    @Override public boolean isSustainedAction(RacialContext c) { return primary != null && primary.isSustainedAction(c); }
    @Override public int chargeSeconds(RacialContext c) { return primary == null ? 0 : primary.chargeSeconds(c); }
    @Override public boolean onActivate(RacialContext c) { return primary != null && primary.onActivate(c); }
    @Override public boolean onSecondaryActivate(RacialContext c) { return primary != null && primary.onSecondaryActivate(c); }

    // --- Eventos: se despachan a todas ---
    @Override public void onTick(RacialContext c) { for (var p : parts) p.onTick(c); }
    @Override public void onSecond(RacialContext c) { for (var p : parts) p.onSecond(c); }
    @Override public void onDamageTakenPost(RacialContext c, float dmg) { for (var p : parts) p.onDamageTakenPost(c, dmg); }
    @Override public void onDeath(RacialContext c) { for (var p : parts) p.onDeath(c); }
    @Override public void onLogin(RacialContext c) { for (var p : parts) p.onLogin(c); }
    @Override public void onLogout(RacialContext c) { for (var p : parts) p.onLogout(c); }
    @Override public void onRespawn(RacialContext c) { for (var p : parts) p.onRespawn(c); }
    @Override public void onDimensionChange(RacialContext c) { for (var p : parts) p.onDimensionChange(c); }

    /** El golpe letal lo consume la primera habilidad que lo maneja. */
    @Override public boolean onLethalDamage(RacialContext c, LethalContext lc) {
        for (var p : parts) if (p.onLethalDamage(c, lc)) return true;
        return false;
    }

    // --- Modificadores: se encadenan ---
    @Override public double modifyDamageTaken(RacialContext c, double dmg, DamageSource s) {
        for (var p : parts) dmg = p.modifyDamageTaken(c, dmg, s);
        return dmg;
    }
    @Override public double modifyDamageTaken(RacialContext c, double dmg, double raw, DamageSource s) {
        for (var p : parts) dmg = p.modifyDamageTaken(c, dmg, raw, s);
        return dmg;
    }
    @Override public double modifyStatXpGain(RacialContext c, String stat, double a) {
        for (var p : parts) a = p.modifyStatXpGain(c, stat, a);
        return a;
    }
    @Override public double modifyTechniqueXpGain(RacialContext c, double a) {
        for (var p : parts) a = p.modifyTechniqueXpGain(c, a);
        return a;
    }
    @Override public double modifyMasteryGain(RacialContext c, double a) {
        for (var p : parts) a = p.modifyMasteryGain(c, a);
        return a;
    }
    @Override public double modifyHealReceived(RacialContext c, double a, HealSource s) {
        for (var p : parts) a = p.modifyHealReceived(c, a, s);
        return a;
    }
    @Override public double modifyHealDealt(RacialContext c, double a, HealSource s) {
        for (var p : parts) a = p.modifyHealDealt(c, a, s);
        return a;
    }
    @Override public int consumeFormUpkeep(RacialContext c, int drain) {
        for (var p : parts) drain = p.consumeFormUpkeep(c, drain);
        return drain;
    }
    @Override public double modifyFormStatMultiplier(StatsData d, String group, double m) {
        for (var p : parts) m = p.modifyFormStatMultiplier(d, group, m);
        return m;
    }
    @Override public double maxReleaseMultiplier(StatsData d) {
        double m = 1.0;
        for (var p : parts) m *= p.maxReleaseMultiplier(d);
        return m;
    }
}
