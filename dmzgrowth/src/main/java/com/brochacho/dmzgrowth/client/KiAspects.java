package com.brochacho.dmzgrowth.client;

import com.dragonminez.common.stats.StatsData;

/**
 * Aspectos del Ki como lecturas derivadas de las stats ORIGINALES de DMZ (sin formulas nuevas de poder):
 *  - Poder Real   = getBattlePowerExact de DMZ con la liberacion al maximo permitido (incluye todas las stats,
 *                   forma, multiplicadores y bonos; RES pesa 1.0 y VIT/ENE 0.5, como en DMZ).
 *  - Se reparte entre tres bloques segun el valor de cada stat (valor x escala x multiplicador de forma):
 *      Ki de Combate = STR + SKP        (quien se especializa en pegar)
 *      Ki Completo   = PWR + ENE        (quien se especializa en ki)
 *      Soporte       = RES + VIT        (aparte, pero suma al poder final)
 *    Ki de Combate + Ki Completo + Soporte = Poder Real.
 *  - Presencia    = poder manifestado ahora (getBattlePowerExact actual) y su % de liberacion.
 * Para cambiar que stats entran en cada bloque, edita los arrays COMBAT / FULL / SUPPORT.
 * Pesos y nombres de escala: DMZ usa escala "DEF" para RES. ENE y VIT pesan 0.5 en el poder de DMZ.
 */
public final class KiAspects {
    private static final String[] COMBAT = {"STR", "SKP"};
    private static final String[] FULL = {"PWR", "ENE"};
    private static final String[] SUPPORT = {"RES", "VIT"};
    private static final double SUPPORT_WEIGHT = 0.5; // mismo valor que SUPPORT_STAT_BP_WEIGHT de DMZ (privado)

    public final double presenceBp;
    public final int presencePercent;
    public final double realPower;
    public final double combatKi;
    public final double fullKi;
    public final double supportKi;

    private KiAspects(double presenceBp, int presencePercent, double realPower, double combatKi, double fullKi, double supportKi) {
        this.presenceBp = presenceBp;
        this.presencePercent = presencePercent;
        this.realPower = realPower;
        this.combatKi = combatKi;
        this.fullKi = fullKi;
        this.supportKi = supportKi;
    }

    public static KiAspects of(StatsData d) {
        double presence = d.getBattlePowerExact();
        int release = d.getResources().getPowerRelease();

        double real;
        int maxRelease = Math.max(1, d.getMaxPowerRelease());
        try { // solo lectura: se restaura siempre la liberacion original
            d.getResources().setPowerRelease(maxRelease);
            real = d.getBattlePowerExact();
        } finally {
            d.getResources().setPowerRelease(release);
        }

        double c = sum(d, COMBAT), f = sum(d, FULL), s = sum(d, SUPPORT);
        double total = c + f + s;
        if (total <= 0) return new KiAspects(presence, release, real, 0, 0, 0);
        return new KiAspects(presence, release, real, real * c / total, real * f / total, real * s / total);
    }

    private static double sum(StatsData d, String[] stats) {
        double t = 0;
        for (String stat : stats) t += term(d, stat);
        return t;
    }

    private static double term(StatsData d, String stat) {
        double value, scaling, mult = d.getTotalMultiplier(stat);
        double weight = 1.0;
        switch (stat) {
            case "STR" -> { value = d.getStats().getStrength(); scaling = d.getStatScaling("STR"); }
            case "SKP" -> { value = d.getStats().getStrikePower(); scaling = d.getStatScaling("SKP"); }
            case "PWR" -> { value = d.getStats().getKiPower(); scaling = d.getStatScaling("PWR"); }
            case "ENE" -> { value = d.getStats().getEnergy(); scaling = d.getStatScaling("ENE"); weight = SUPPORT_WEIGHT; }
            case "VIT" -> { value = d.getStats().getVitality(); scaling = d.getStatScaling("VIT"); weight = SUPPORT_WEIGHT; }
            case "RES" -> { value = d.getStats().getResistance(); scaling = d.getStatScaling("DEF"); }
            default -> { return 0; }
        }
        return weight * value * scaling * mult;
    }
}
