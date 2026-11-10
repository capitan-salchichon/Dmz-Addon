package com.brochacho.dmzgrowth;

import net.minecraftforge.common.ForgeConfigSpec;

/** Valores PROVISIONALES: el prompt maestro no define rangos. Todo es editable en config/dmzgrowth-common.toml. */
public final class GrowthConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue POTENTIAL_MIN;
    public static final ForgeConfigSpec.IntValue POTENTIAL_MAX;
    public static final ForgeConfigSpec.IntValue INITIAL_LIMIT;
    public static final ForgeConfigSpec.DoubleValue POTENTIAL_PER_STAT_POINT;
    public static final ForgeConfigSpec.DoubleValue EXTREME_WEIGHT_FACTOR;
    public static final ForgeConfigSpec.IntValue EXTREME_SECONDS_REQUIRED;
    public static final ForgeConfigSpec.DoubleValue EXTREME_GAIN_MULT_MIN;
    public static final ForgeConfigSpec.DoubleValue EXTREME_GAIN_MULT_MAX;
    public static final ForgeConfigSpec.DoubleValue EXTREME_POTENTIAL_RATIO;
    public static final ForgeConfigSpec.BooleanValue EXTREME_REQUIRE_WEIGHT;
    public static final ForgeConfigSpec.IntValue SCOUTER_RANGE;
    public static final ForgeConfigSpec.IntValue SCOUTER_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue SCOUTER_MAX_ENTRIES;
    public static final ForgeConfigSpec.BooleanValue TRANSFORM_PROGRESSIVE;
    public static final ForgeConfigSpec.DoubleValue TRANSFORM_CURVE;
    public static final ForgeConfigSpec.DoubleValue ZENKAI_FRACTION_MIN;
    public static final ForgeConfigSpec.DoubleValue ZENKAI_FRACTION_MAX;
    public static final ForgeConfigSpec.DoubleValue ZENKAI_REPEAT_DECAY;
    public static final ForgeConfigSpec.IntValue ZENKAI_TRAINING_SECONDS_PER_RECOVERY;
    public static final ForgeConfigSpec.DoubleValue EXTREME_OVERLOAD_RATIO;
    public static final ForgeConfigSpec.IntValue EXTREME_OVERLOAD_PENALTY_SECONDS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("potential");
        POTENTIAL_MIN = b.comment("Potencial inicial minimo (definido por el usuario: 50) al crear personaje.")
                .defineInRange("initialPotentialMin", 50, 0, Integer.MAX_VALUE);
        POTENTIAL_MAX = b.comment("Potencial inicial maximo (definido por el usuario: 1000) al crear personaje.")
                .defineInRange("initialPotentialMax", 1000, 0, Integer.MAX_VALUE);
        POTENTIAL_PER_STAT_POINT = b.comment("PROVISIONAL. Potencial que cuesta cada punto de stat (1.0 = como el ejemplo del prompt).")
                .defineInRange("potentialPerStatPoint", 1.0, 0.0, 1000.0);
        b.pop();
        b.push("limit");
        INITIAL_LIMIT = b.comment("PROVISIONAL. Limite inicial (techo de cada stat).")
                .defineInRange("initialLimit", 1000, 1, Integer.MAX_VALUE);
        b.pop();
        b.push("extremeTraining");
        EXTREME_WEIGHT_FACTOR = b.comment("Peso del Entrenamiento Extremo = peso ideal de DMZ x este factor (definido por el usuario: 5).")
                .defineInRange("weightFactor", 5.0, 1.0, 100.0);
        EXTREME_OVERLOAD_RATIO = b.comment("PROVISIONAL. Carga (x peso ideal) por encima de la cual el entrenamiento es contraproducente.")
                .defineInRange("overloadRatio", 6.0, 1.0, 100.0);
        EXTREME_OVERLOAD_PENALTY_SECONDS = b.comment("PROVISIONAL. Segundos de progreso que se pierden por cada segundo en sobrecarga dentro del minijuego.")
                .defineInRange("overloadPenaltySeconds", 2, 0, 1000);
        EXTREME_REQUIRE_WEIGHT = b.comment("Ademas del minijuego, exigir carga >= factor x peso ideal (prompt maestro). Pon false para contar solo el minijuego.")
                .define("requireWeight", true);
        EXTREME_SECONDS_REQUIRED = b.comment("Segundos acumulados EN UN MINIJUEGO DE TPS (con la carga requerida) para completar un Entrenamiento Extremo. Definido: 5 min.")
                .defineInRange("secondsRequired", 300, 1, Integer.MAX_VALUE);
        EXTREME_GAIN_MULT_MIN = b.comment("Ganancia aleatoria = Limite actual x [min..max]. Basado en tu ejemplo (1000 -> +4000 .. +25000). PROVISIONAL.")
                .defineInRange("gainMultiplierMin", 4.0, 0.0, 1000.0);
        EXTREME_GAIN_MULT_MAX = b.defineInRange("gainMultiplierMax", 25.0, 0.0, 1000.0);
        EXTREME_POTENTIAL_RATIO = b.comment("Potencial ganado = ganancia de Limite x este ratio (1.0 = igual). PROVISIONAL.")
                .defineInRange("potentialGainRatio", 1.0, 0.0, 100.0);
        b.pop();
        b.push("scouter");
        SCOUTER_RANGE = b.comment("Alcance de deteccion de Presencia del scouter, en bloques (objetivo del prompt: ~3000).")
                .defineInRange("detectionRange", 3000, 50, 30000000);
        SCOUTER_INTERVAL_TICKS = b.comment("Cada cuantos ticks el servidor envia las presencias a quien lleva scouter.")
                .defineInRange("intervalTicks", 40, 5, 1200);
        SCOUTER_MAX_ENTRIES = b.comment("Maximo de presencias enviadas por jugador (las mas fuertes).")
                .defineInRange("maxEntries", 5, 1, 50);
        b.pop();
        b.push("transformation");
        TRANSFORM_PROGRESSIVE = b.comment("La Transformacion Normal sube el poder progresivamente mientras se carga. La Instant Transformation no cambia.")
                .define("progressive", true);
        TRANSFORM_CURVE = b.comment("PROVISIONAL. Curva del progreso: 1 = lineal, >1 = sube poco al inicio y mucho al final (como tu ejemplo 10k..100k).")
                .defineInRange("curveExponent", 1.5, 0.1, 10.0);
        b.pop();
        b.push("zenkai");
        ZENKAI_FRACTION_MIN = b.comment("Fraccion minima del Potencial disponible que desbloquea un Zenkai (0 = casi nada).")
                .defineInRange("fractionMin", 0.0, 0.0, 1.0);
        ZENKAI_FRACTION_MAX = b.comment("Fraccion maxima (1 = todo el Potencial disponible).")
                .defineInRange("fractionMax", 1.0, 0.0, 1.0);
        ZENKAI_REPEAT_DECAY = b.comment("PROVISIONAL. Cada Zenkai seguido sin entrenar multiplica la eficacia por este valor (0.5: 100%, 50%, 25%...).")
                .defineInRange("repeatDecay", 0.5, 0.0, 1.0);
        ZENKAI_TRAINING_SECONDS_PER_RECOVERY = b.comment("PROVISIONAL. Segundos en un minijuego de TPs para recuperar un escalon de eficacia.")
                .defineInRange("trainingSecondsPerRecovery", 60, 1, Integer.MAX_VALUE);
        b.pop();
        SPEC = b.build();
    }

    private GrowthConfig() {}
}
