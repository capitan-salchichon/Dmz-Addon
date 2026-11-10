package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;

/**
 * Zenkai "regalado": convierte una fraccion ALEATORIA (poco o todo) del Potencial restante en puntos de stat
 * gratis (sin TPs). No crea poder de la nada: el Potencial se consume y el Limite se respeta.
 */
public final class ZenkaiGift {
    private ZenkaiGift() {}

    /** @return true si el regalo se aplico (entonces se omite el buff porcentual original de DMZ). */
    public static boolean apply(ServerPlayer player, StatsData data, String[] stats) {
        GrowthData g = player.getCapability(GrowthCapability.INSTANCE).resolve().orElse(null);
        if (g == null || !g.isRolled() || stats == null || stats.length == 0) return false;

        double lo = Math.min(GrowthConfig.ZENKAI_FRACTION_MIN.get(), GrowthConfig.ZENKAI_FRACTION_MAX.get());
        double hi = Math.max(GrowthConfig.ZENKAI_FRACTION_MIN.get(), GrowthConfig.ZENKAI_FRACTION_MAX.get());
        double effectiveness = g.zenkaiEffectiveness();
        double fraction = (lo + player.getRandom().nextDouble() * (hi - lo)) * effectiveness;
        g.registerZenkai();

        double perPoint = GrowthConfig.POTENTIAL_PER_STAT_POINT.get();
        long budget = perPoint <= 0.0 ? 0L : (long) Math.floor(g.getPotential() * fraction / perPoint);
        long granted = 0L;

        // Reparto en rondas iguales entre las stats del Zenkai de DMZ, respetando Limite y topes de DMZ.
        boolean progress = true;
        while (budget > 0 && progress) {
            progress = false;
            int share = (int) Math.max(1L, budget / stats.length);
            for (String stat : stats) {
                if (budget <= 0) break;
                int want = (int) Math.min(share, budget);
                int allowed = data.getMaxAllowedIncreaseForStat(stat, want);
                if (allowed <= 0) continue;
                data.getStats().addStat(stat, allowed);
                g.consumeForPoints(allowed);
                budget -= allowed;
                granted += allowed;
                progress = true;
            }
        }

        GrowthNetwork.sync(player);
        return true;
    }
}
