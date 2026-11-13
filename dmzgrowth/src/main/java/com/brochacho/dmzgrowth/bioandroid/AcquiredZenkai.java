package com.brochacho.dmzgrowth.bioandroid;

import com.brochacho.dmzgrowth.ability.Ability;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.dragonminez.common.racial.LethalContext;
import com.dragonminez.common.racial.RacialAbility;
import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.racial.RacialRegistry;
import com.dragonminez.common.stats.StatsData;

/**
 * Zenkai obtenido al absorber a un saiyan con la cola. Reenvia a la habilidad saiyan de DMZ (ya integrada con el
 * Potencial por el addon) solo los hooks propios del Zenkai: golpe letal, cuenta atras, dano y bono de liberacion.
 */
public class AcquiredZenkai implements Ability {
    public static final String KEY = "zenkai";

    @Override public String id() { return "acquired_zenkai"; }

    @Override
    public boolean appliesTo(StatsData data) {
        return data.getPlayer() != null && data.getPlayer().getCapability(GrowthCapability.INSTANCE)
                .map(g -> g.hasAcquired(KEY)).orElse(false);
    }

    private static RacialAbility saiyan() { return RacialRegistry.get("saiyan").orElse(null); }

    @Override
    public boolean onLethalDamage(RacialContext ctx, LethalContext lc) {
        RacialAbility s = saiyan();
        return s != null && s.onLethalDamage(ctx, lc);
    }

    @Override
    public void onDamageTakenPost(RacialContext ctx, float rawDamage) {
        RacialAbility s = saiyan();
        if (s != null) s.onDamageTakenPost(ctx, rawDamage);
    }

    @Override
    public void onSecond(RacialContext ctx) {
        RacialAbility s = saiyan();
        if (s != null) s.onSecond(ctx);
    }

    @Override
    public double maxReleaseMultiplier(StatsData data) {
        RacialAbility s = saiyan();
        return s == null ? 1.0 : s.maxReleaseMultiplier(data);
    }
}
