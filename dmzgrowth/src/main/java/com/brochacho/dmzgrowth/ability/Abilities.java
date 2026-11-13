package com.brochacho.dmzgrowth.ability;

/**
 * Punto unico donde se registran las habilidades. Se llama desde el constructor del mod (antes de que se
 * registren las teclas). Aqui se registran las habilidades que defines tu (por ahora, las que el Bio-Android obtiene al absorber).
 *
 * Ejemplo de pasiva (clase aparte, solo sobreescribes lo que reacciona):
 *   public class MiPasiva implements Ability {
 *       public String id() { return "mi_pasiva"; }
 *       public boolean appliesTo(StatsData d) { return d.getCharacter().getRaceName().equals("saiyan"); }
 *       public double modifyDamageTaken(RacialContext c, double dmg, DamageSource s) { return dmg * 0.9; }
 *   }
 * Ejemplo de activa: isActive() = true, cooldownTicks(), energyCost(), canUse(ctx) y use(ctx).
 * Registro:  AbilityRegistry.register(new MiPasiva());
 */
public final class Abilities {
    private Abilities() {}

    public static void registerAll() {
        AbilityRegistry.register(new com.brochacho.dmzgrowth.bioandroid.AcquiredRegeneration());
        AbilityRegistry.register(new com.brochacho.dmzgrowth.bioandroid.AcquiredZenkai());
    }
}
