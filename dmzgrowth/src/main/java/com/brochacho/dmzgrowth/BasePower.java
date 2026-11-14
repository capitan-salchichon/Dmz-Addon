package com.brochacho.dmzgrowth;

import com.dragonminez.common.stats.StatsData;

/**
 * "Poder" con el que se miden el Potencial y el Limite: el poder de batalla de DMZ SIN formas y con la liberacion al maximo
 * permitido (es decir, el Poder Real base). Replica la formula de StatsData.getBattlePowerExact (constantes privadas de DMZ:
 * K=100, REF=1200, EXP=1.2, soporte 0.5) para poder simular "que pasaria si subo N puntos" SIN modificar al jugador.
 * Si DMZ cambia esa formula, hay que actualizar las constantes de aqui.
 */
public final class BasePower {
    private static final double K = 100.0, REF = 1200.0, EXP = 1.2, SUPPORT = 0.5;
    private static final ThreadLocal<Boolean> IGNORE_FORMS = ThreadLocal.withInitial(() -> false);

    private BasePower() {}

    /** Mientras es true, getFormMultiplier devuelve 1.0 (ver ProgressiveTransformation.adjust). */
    public static boolean ignoringForms() { return IGNORE_FORMS.get(); }

    public static double of(StatsData d) { return of(d, "", 0); }

    /** Poder base si la stat indicada tuviera `delta` puntos mas. stat: STR, SKP, RES (o DEF), VIT, PWR, ENE. */
    public static double of(StatsData d, String stat, int delta) {
        if (d.getStatus().isAndroidUpgraded()) return 0.0; // DMZ devuelve un valor "infinito" para androides mejorados
        IGNORE_FORMS.set(true);
        try {
            String s = stat == null ? "" : stat.toUpperCase();
            double str = d.getStats().getStrength() + ("STR".equals(s) ? delta : 0);
            double skp = d.getStats().getStrikePower() + ("SKP".equals(s) ? delta : 0);
            double res = d.getStats().getResistance() + (("RES".equals(s) || "DEF".equals(s)) ? delta : 0);
            double vit = d.getStats().getVitality() + ("VIT".equals(s) ? delta : 0);
            double pwr = d.getStats().getKiPower() + ("PWR".equals(s) ? delta : 0);
            double ene = d.getStats().getEnergy() + ("ENE".equals(s) ? delta : 0);

            double raw = term(d, "STR", "STR", str) + term(d, "SKP", "SKP", skp) + term(d, "DEF", "RES", res) + term(d, "PWR", "PWR", pwr)
                    + SUPPORT * (term(d, "VIT", "VIT", vit) + term(d, "ENE", "ENE", ene));
            if (Double.isNaN(raw) || raw <= 0) return 0.0;
            double release = Math.max(1, d.getMaxPowerRelease()) / 100.0;
            double bp = REF * Math.pow(raw / K, EXP) * release;
            return Double.isNaN(bp) || bp <= 0 ? 0.0 : bp;
        } finally {
            IGNORE_FORMS.set(false);
        }
    }

    /** Un termino de la formula de DMZ: (valor + bono mult) * escala * multiplicador total + bono plano * escala. */
    private static double term(StatsData d, String scalingKey, String multKey, double value) {
        var bonus = d.getBonusStats();
        int v = (int) Math.round(value);
        double multBonus = bonus.calculateBonus(scalingKey, v, true);
        double flatBonus = bonus.calculateBonus(scalingKey, v, false);
        double scaling = d.getStatScaling(scalingKey);
        return (value + multBonus) * scaling * d.getTotalMultiplier(multKey) + flatBonus * scaling;
    }
}
