package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.data.GrowthData;
import com.dragonminez.common.stats.StatsData;

/** Reglas de Potencial y Limite, medidas en PODER (BasePower). */
public final class GrowthMath {
    /** Tope temporal de Potencial gastable (lo usa el Zenkai para gastar solo una fraccion). */
    public static final ThreadLocal<Double> BUDGET_CAP = ThreadLocal.withInitial(() -> Double.POSITIVE_INFINITY);

    private GrowthMath() {}

    /** Mayor numero de puntos (<= requested) que se pueden subir a `stat` sin pasar el Limite ni el Potencial. */
    public static int maxPoints(StatsData d, String stat, int requested, GrowthData g) {
        if (!g.isRolled() || requested <= 0) return 0;
        double p0 = BasePower.of(d);
        double available = Math.min((double) g.getPotential(), BUDGET_CAP.get());
        double cap = Math.min(g.getLimit() - p0, available);
        if (cap <= 0) return 0;
        if (BasePower.of(d, stat, requested) - p0 <= cap) return requested;
        int lo = 0, hi = requested; // lo siempre cumple, hi nunca cumple
        while (hi - lo > 1) {
            int mid = lo + (hi - lo) / 2;
            if (BasePower.of(d, stat, mid) - p0 <= cap) lo = mid; else hi = mid;
        }
        return lo;
    }
}
