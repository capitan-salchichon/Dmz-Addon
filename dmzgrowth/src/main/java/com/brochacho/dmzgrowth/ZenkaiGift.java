package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;

/**
 * Zenkai "regalado": convierte una fraccion ALEATORIA (poco o todo) del Potencial restante en puntos de stat gratis
 * (sin TPs). El Potencial es PODER: se gasta tanto como poder gane el personaje, y el Limite se respeta.
 */
public final class ZenkaiGift {
    private static final int CHUNK = 16;        // puntos por stat y ronda
    private static final int MAX_ROUNDS = 4000; // tope de seguridad

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

        double budget = g.getPotential() * fraction;
        GrowthMath.BUDGET_CAP.set(budget);
        try {
            for (int round = 0; round < MAX_ROUNDS && budget > 0; round++) {
                boolean progress = false;
                for (String stat : stats) {
                    int allowed = data.getMaxAllowedIncreaseForStat(stat, CHUNK); // ya respeta Limite, Potencial y el tope
                    if (allowed <= 0) continue;
                    double before = BasePower.of(data);
                    data.getStats().addStat(stat, allowed);
                    double spent = Math.max(0.0, BasePower.of(data) - before);
                    g.consumePower(spent);
                    budget -= spent;
                    GrowthMath.BUDGET_CAP.set(budget);
                    progress = true;
                    if (budget <= 0) break;
                }
                if (!progress) break;
            }
        } finally {
            GrowthMath.BUDGET_CAP.remove();
        }
        GrowthNetwork.sync(player);
        return true;
    }
}
