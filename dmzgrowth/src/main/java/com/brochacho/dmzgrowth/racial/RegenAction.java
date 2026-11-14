package com.brochacho.dmzgrowth.racial;

import com.brochacho.dmzgrowth.bioandroid.AcquiredRegeneration;
import com.dragonminez.common.racial.RacialAbility;
import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.racial.impl.NamekAssimilation;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.character.Cooldowns;

/**
 * Accion con tecla "Regeneracion": se selecciona en el menu X igual que la asimilacion y se ejecuta con la tecla de accion
 * (sin carga). Namekiano: usa la regeneracion de DMZ. Bio-Android (si la absorbio): usa el canal propio del addon.
 */
public final class RegenAction implements RacialAbility {
    public static final RegenAction INSTANCE = new RegenAction();

    private RegenAction() {}

    @Override public String id() { return "regeneration_action"; }
    @Override public boolean hasActiveAction() { return true; }
    @Override public boolean hasActiveAction(StatsData data) { return true; }
    @Override public boolean isSustainedAction(RacialContext ctx) { return false; }
    @Override public int chargeSeconds(RacialContext ctx) { return 0; }

    @Override
    public boolean canActivate(RacialContext ctx) {
        StatsData data = ctx.data();
        if (data.getStatus().isStunned() || !ctx.config().getNamekian().getEnabled()) return false;
        var cd = data.getCooldowns();
        return !cd.hasCooldown(Cooldowns.NAMEK_REGEN) && !cd.hasCooldown(Cooldowns.NAMEK_REGEN_ACTIVE);
    }

    @Override
    public boolean onActivate(RacialContext ctx) {
        if (!canActivate(ctx)) return false;
        if ("namekian".equals(ctx.data().getCharacter().getRaceName())) {
            NamekAssimilation.startRegen(ctx.player(), ctx.data());
            return true;
        }
        return AcquiredRegeneration.start(ctx);
    }
}
