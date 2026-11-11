package com.brochacho.dmzgrowth.namek;

import java.util.Locale;
import java.util.Set;

/** Habilidades que son exclusivas de una raza (vienen por defecto con ella) y nunca se compran. */
public final class ExclusiveSkills {
    /** Ids con los que una regeneracion namekiana podria aparecer como skill de tienda. */
    private static final Set<String> NAMEK_REGEN = Set.of(
            "regeneration", "regen", "namekian_regen", "namek_regen", "namekian_regeneration", "namek_regeneration");

    private ExclusiveSkills() {}

    public static boolean isNamekRegen(String skillName) {
        return skillName != null && NAMEK_REGEN.contains(skillName.toLowerCase(Locale.ROOT));
    }
}
