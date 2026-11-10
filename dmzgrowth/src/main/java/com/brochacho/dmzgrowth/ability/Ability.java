package com.brochacho.dmzgrowth.ability;

import com.dragonminez.common.racial.RacialAbility;
import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.stats.StatsData;

/**
 * Habilidad modular. Extiende RacialAbility de DMZ, asi que una pasiva solo sobreescribe los hooks que necesite
 * (onTick, modifyDamageTaken, onLethalDamage, modifyHealReceived, onDamageTakenPost...) y DMZ ya los llama.
 * Una activa ademas define use(...), cooldown, coste y tecla; el servidor valida todo (ver AbilityRegistry.use).
 */
public interface Ability extends RacialAbility {
    /** Condicion para que la habilidad este activa en este jugador (raza, forma, desbloqueo, estado...). */
    default boolean appliesTo(StatsData data) { return true; }

    /** true = se activa con una tecla; false = pasiva (siempre en escucha de eventos). */
    default boolean isActive() { return false; }

    default int cooldownTicks() { return 0; }

    /** Energia (ki actual de DMZ) que consume cada uso. */
    default int energyCost() { return 0; }

    /** Requisitos extra para usarla (se comprueba en el servidor antes de gastar nada). */
    default boolean canUse(RacialContext ctx) { return true; }

    /** Efecto de la habilidad activa. Devuelve true si se uso (entonces se cobra coste y cooldown). */
    default boolean use(RacialContext ctx) { return false; }

    /** Clave de cooldown dentro del sistema de cooldowns de DMZ (se guarda y avanza sola). */
    default String cooldownKey() { return "DMZG_" + id(); }
}
