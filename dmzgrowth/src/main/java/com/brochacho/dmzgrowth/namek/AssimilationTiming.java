package com.brochacho.dmzgrowth.namek;

/**
 * Cronologia de la asimilacion (ticks, 20 = 1 s), compartida por servidor y cliente.
 *  Asimilado : 0-10 se vuelve blanco | 10-50 queda blanco, inmovil | 50-60 desaparece | 60-80 invisible y se reinicia | 80 vuelve.
 *  Asimilador: desde 1 s antes de que desaparezca el asimilado (t=40) se vuelve blanco en 0,5 s, queda blanco hasta t=60
 *              y vuelve a la normalidad en 0,5 s (t=70).
 */
public final class AssimilationTiming {
    public static final int WHITE_IN = 10;
    public static final int HOLD = 40;
    public static final int DISSOLVE = 10;
    public static final int GONE = 20;

    /** Tick en que el asimilado termina de desaparecer: aqui ocurren el reinicio y la suma de poderes. */
    public static final int RESET_AT = WHITE_IN + HOLD + DISSOLVE;       // 60
    public static final int TARGET_END = RESET_AT + GONE;                // 80

    public static final int ASSIM_START = RESET_AT - 20;                 // 40: 1 s antes de desaparecer
    public static final int ASSIM_FADE = 10;
    public static final int ASSIM_END = RESET_AT + ASSIM_FADE;           // 70

    /** Blanco maximo antes de que el shader de DMZ empiece a disolver el modelo. */
    public static final float WHITE_CAP = 0.85f;

    private AssimilationTiming() {}
}
