package com.brochacho.dmzgrowth.racial;

import com.brochacho.dmzgrowth.bioandroid.AcquiredRegeneration;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import com.dragonminez.common.stats.StatsData;

/** Eleccion de la accion racial que ejecuta la tecla de accion (ademas de la racial normal de DMZ). */
public final class RacialChoice {
    public static final String REGENERATION = "regeneration";

    private RacialChoice() {}

    /** El namekiano la tiene de serie; el Bio-Android solo si absorbio a un namekiano. */
    public static boolean regenEligible(StatsData data, GrowthData g) {
        String race = data.getCharacter().getRaceName();
        return "namekian".equals(race) || ("bioandroid".equals(race) && g.hasAcquired(AcquiredRegeneration.KEY));
    }

    public static boolean regenSelected(StatsData data) {
        var player = data.getPlayer();
        if (player == null) return false;
        GrowthData g = player.getCapability(GrowthCapability.INSTANCE).resolve().orElse(null);
        return g != null && REGENERATION.equals(g.getRacialChoice()) && regenEligible(data, g);
    }
}
