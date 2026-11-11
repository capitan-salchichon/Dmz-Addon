package com.brochacho.dmzgrowth.data;

import com.brochacho.dmzgrowth.GrowthConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

/**
 * Potencial = capacidad de crecimiento disponible (se consume al subir stats).
 * Limite = techo de cada stat. Solo el Entrenamiento Extremo los sube (checkpoint 5).
 */
public class GrowthData {
    private boolean rolled;
    private long initialPotential;
    private long potential;
    private long limit;
    private int extremeSeconds;
    private int zenkaiStreak;
    private int trainingAccum;

    public boolean isRolled() { return rolled; }
    public long getInitialPotential() { return initialPotential; }
    public long getPotential() { return potential; }
    public long getLimit() { return limit; }

    /** Se genera UNA sola vez; si ya existe no hace nada. */
    public boolean rollInitial(RandomSource random, int highestCurrentStat) {
        if (rolled) return false;
        int min = Math.min(GrowthConfig.POTENTIAL_MIN.get(), GrowthConfig.POTENTIAL_MAX.get());
        int max = Math.max(GrowthConfig.POTENTIAL_MIN.get(), GrowthConfig.POTENTIAL_MAX.get());
        initialPotential = min + (max > min ? random.nextInt(max - min + 1) : 0);
        potential = initialPotential;
        limit = Math.max(GrowthConfig.INITIAL_LIMIT.get(), highestCurrentStat);
        rolled = true;
        return true;
    }

    /** Cuantos puntos de stat se pueden anadir a una stat con valor actual, segun Limite y Potencial. */
    public int maxPointsAllowed(int currentStat) {
        if (!rolled) return 0;
        long byLimit = Math.max(0L, limit - currentStat);
        double perPoint = GrowthConfig.POTENTIAL_PER_STAT_POINT.get();
        long byPotential = perPoint <= 0.0 ? Long.MAX_VALUE : (long) Math.floor(potential / perPoint);
        return (int) Math.min(Integer.MAX_VALUE, Math.min(byLimit, byPotential));
    }

    /** Consume Potencial por puntos de stat ya validados. Nunca crea Potencial. */
    public void consumeForPoints(int points) {
        if (points <= 0) return;
        long cost = (long) Math.ceil(points * GrowthConfig.POTENTIAL_PER_STAT_POINT.get());
        potential = Math.max(0L, potential - cost);
    }

    /** Suma 1 segundo de Entrenamiento Extremo; devuelve la ganancia de Limite si se completo, o 0. */
    public long tickExtremeSecond(RandomSource random) {
        if (!rolled) return 0L;
        if (++extremeSeconds < GrowthConfig.EXTREME_SECONDS_REQUIRED.get()) return 0L;
        extremeSeconds = 0;
        double lo = Math.min(GrowthConfig.EXTREME_GAIN_MULT_MIN.get(), GrowthConfig.EXTREME_GAIN_MULT_MAX.get());
        double hi = Math.max(GrowthConfig.EXTREME_GAIN_MULT_MIN.get(), GrowthConfig.EXTREME_GAIN_MULT_MAX.get());
        double mult = lo + random.nextDouble() * (hi - lo);
        long gain = Math.max(1L, Math.round(limit * mult));
        limit += gain;
        potential += Math.round(gain * GrowthConfig.EXTREME_POTENTIAL_RATIO.get());
        return gain;
    }

    /** Entrenar con demasiado peso es contraproducente: se pierde progreso acumulado (nunca baja de 0). */
    public void penalizeExtreme(int seconds) {
        if (seconds > 0) extremeSeconds = Math.max(0, extremeSeconds - seconds);
    }

    /** Eficacia del proximo Zenkai: decay^racha (Zenkais seguidos sin entrenar). */
    public double zenkaiEffectiveness() {
        return Math.pow(GrowthConfig.ZENKAI_REPEAT_DECAY.get(), zenkaiStreak);
    }

    public void registerZenkai() { zenkaiStreak++; }

    /** Un segundo entrenando (minijuego de TPs): cada N segundos recupera un escalon de eficacia. */
    public void trainingSecond() {
        if (zenkaiStreak <= 0) { trainingAccum = 0; return; }
        if (++trainingAccum >= GrowthConfig.ZENKAI_TRAINING_SECONDS_PER_RECOVERY.get()) {
            trainingAccum = 0;
            zenkaiStreak--;
        }
    }

    /** Reinicio de personaje (Dende / asimilacion): conserva el Potencial inicial de ESTE personaje y vuelve al Limite inicial. */
    public void resetToInitial() {
        if (!rolled) return;
        potential = initialPotential;
        limit = GrowthConfig.INITIAL_LIMIT.get();
        extremeSeconds = 0;
        zenkaiStreak = 0;
        trainingAccum = 0;
    }

    public int getExtremeSeconds() { return extremeSeconds; }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("Rolled", rolled);
        t.putLong("InitialPotential", initialPotential);
        t.putLong("Potential", potential);
        t.putLong("Limit", limit);
        t.putInt("ExtremeSeconds", extremeSeconds);
        t.putInt("ZenkaiStreak", zenkaiStreak);
        t.putInt("TrainingAccum", trainingAccum);
        return t;
    }

    public void load(CompoundTag t) {
        rolled = t.getBoolean("Rolled");
        initialPotential = t.getLong("InitialPotential");
        potential = t.getLong("Potential");
        limit = t.getLong("Limit");
        extremeSeconds = t.getInt("ExtremeSeconds");
        zenkaiStreak = t.getInt("ZenkaiStreak");
        trainingAccum = t.getInt("TrainingAccum");
    }

    public void copyFrom(GrowthData o) { load(o.save()); }
}
