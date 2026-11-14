package com.brochacho.dmzgrowth.data;

import com.brochacho.dmzgrowth.GrowthConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
    private int unitsVersion; // 2 = Potencial y Limite en PODER (antes: puntos de stat)
    private String bioFormGroup = "";
    private String bioForm = "";
    private final java.util.Set<String> acquired = new java.util.LinkedHashSet<>();

    public boolean isRolled() { return rolled; }
    public long getInitialPotential() { return initialPotential; }
    public long getPotential() { return potential; }
    public long getLimit() { return limit; }

    /** Se genera UNA sola vez; si ya existe no hace nada. */
    public boolean rollInitial(RandomSource random, double basePower) {
        if (rolled) return false;
        int min = Math.min(GrowthConfig.POTENTIAL_MIN.get(), GrowthConfig.POTENTIAL_MAX.get());
        int max = Math.max(GrowthConfig.POTENTIAL_MIN.get(), GrowthConfig.POTENTIAL_MAX.get());
        initialPotential = min + (max > min ? random.nextInt(max - min + 1) : 0);
        potential = initialPotential;
        limit = Math.max(GrowthConfig.INITIAL_LIMIT.get(), (long) Math.ceil(basePower));
        unitsVersion = 2;
        rolled = true;
        return true;
    }

    /** Consume Potencial por poder ganado (nunca crea Potencial). */
    public void consumePower(double powerGained) {
        if (powerGained <= 0) return;
        potential = Math.max(0L, potential - (long) Math.ceil(powerGained));
    }

    /** Personajes anteriores al cambio de unidades: el Limite sube hasta su poder actual para no quedar incoherente. */
    public boolean migrateToPowerUnits(double basePower) {
        if (!rolled || unitsVersion >= 2) return false;
        limit = Math.max(limit, (long) Math.ceil(basePower));
        unitsVersion = 2;
        return true;
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
        unitsVersion = 2;
        acquired.clear(); // el reinicio borra el progreso racial
        bioFormGroup = "";
        bioForm = "";
    }

    /** Habilidades raciales ajenas obtenidas (p. ej. el Bio-Android al absorber): "regeneration", "zenkai". */
    public boolean hasAcquired(String id) { return acquired.contains(id); }
    public boolean acquire(String id) { return acquired.add(id); }

    /** Ultima forma de evolucion del Bio-Android (para que sobreviva a la muerte). */
    public void rememberBioForm(String group, String form) { bioFormGroup = group; bioForm = form; }
    public String getBioFormGroup() { return bioFormGroup; }
    public String getBioForm() { return bioForm; }

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
        t.putInt("UnitsVersion", unitsVersion);
        t.putString("BioFormGroup", bioFormGroup);
        t.putString("BioForm", bioForm);
        ListTag list = new ListTag();
        for (String a : acquired) list.add(StringTag.valueOf(a));
        t.put("Acquired", list);
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
        unitsVersion = t.getInt("UnitsVersion");
        bioFormGroup = t.getString("BioFormGroup");
        bioForm = t.getString("BioForm");
        acquired.clear();
        for (var tag : t.getList("Acquired", 8)) acquired.add(tag.getAsString());
    }

    public void copyFrom(GrowthData o) { load(o.save()); }
}
