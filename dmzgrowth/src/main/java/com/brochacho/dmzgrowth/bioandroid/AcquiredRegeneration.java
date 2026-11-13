package com.brochacho.dmzgrowth.bioandroid;

import com.brochacho.dmzgrowth.ability.Ability;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.dragonminez.common.config.GeneralServerConfig;
import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.character.Cooldowns;

/**
 * Regeneracion obtenida al absorber a un namekiano con la cola. Usa los mismos cooldowns y valores de DMZ
 * (canal, curacion, coste de ki y stamina) pero solo la regeneracion: sin asimilacion ni afinidad con el agua.
 */
public class AcquiredRegeneration implements Ability {
    public static final String KEY = "regeneration";

    @Override public String id() { return "acquired_regeneration"; }
    @Override public boolean isActive() { return true; }

    @Override
    public boolean appliesTo(StatsData data) {
        return data.getPlayer() != null && data.getPlayer().getCapability(GrowthCapability.INSTANCE)
                .map(g -> g.hasAcquired(KEY)).orElse(false);
    }

    @Override
    public boolean use(RacialContext ctx) {
        GeneralServerConfig.NamekianRacialConfig cfg = ctx.config().getNamekian();
        if (!cfg.getEnabled()) return false;
        var cd = ctx.data().getCooldowns();
        if (cd.hasCooldown(Cooldowns.NAMEK_REGEN) || cd.hasCooldown(Cooldowns.NAMEK_REGEN_ACTIVE)) return false;
        cd.setCooldown(Cooldowns.NAMEK_REGEN_ACTIVE, cfg.getRegenChannelSeconds() * 20);
        cd.setCooldown(Cooldowns.NAMEK_REGEN, cfg.getRegenCooldownSeconds() * 20);
        return true;
    }

    /** Canal de curacion: una pulsacion por segundo, igual que el namekiano. */
    @Override
    public void onSecond(RacialContext ctx) {
        StatsData data = ctx.data();
        if (!data.getCooldowns().hasCooldown(Cooldowns.NAMEK_REGEN_ACTIVE)) return;
        GeneralServerConfig.NamekianRacialConfig cfg = ctx.config().getNamekian();
        int seconds = Math.max(1, cfg.getRegenChannelSeconds());
        var player = ctx.player();
        player.heal((float) (player.getMaxHealth() * cfg.getRegenHealthRatio() / seconds));
        data.getResources().removeEnergy((float) (data.getMaxEnergy() * cfg.getRegenEnergyCost() / seconds));
        data.getResources().removeStamina((float) (data.getMaxStamina() * cfg.getRegenStaminaCost() / seconds));
    }

    /** Recibir dano interrumpe la regeneracion (mismo castigo de cooldown que en DMZ). */
    @Override
    public void onDamageTakenPost(RacialContext ctx, float rawDamage) {
        StatsData data = ctx.data();
        if (!data.getCooldowns().hasCooldown(Cooldowns.NAMEK_REGEN_ACTIVE)) return;
        data.getCooldowns().removeCooldown(Cooldowns.NAMEK_REGEN_ACTIVE);
        data.getCooldowns().setCooldown(Cooldowns.NAMEK_REGEN, ctx.config().getNamekian().getRegenCooldownSeconds() * 20 / 2);
    }
}
