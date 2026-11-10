package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.ZenkaiGift;
import com.dragonminez.common.config.GeneralServerConfig;
import com.dragonminez.common.racial.RacialData;
import com.dragonminez.common.racial.impl.SaiyanZenkai;
import com.dragonminez.common.stats.character.Cooldowns;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SaiyanZenkai.class, remap = false)
public abstract class ZenkaiMixin {
    /** Reemplaza el buff porcentual de DMZ por el regalo basado en Potencial; conserva el bono de liberacion. */
    @Inject(method = "applyBuff", at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$gift(StatsData data, GeneralServerConfig.SaiyanRacialConfig config, double buffPct,
                                       boolean isMaxBuff, double potency, long gameTime, CallbackInfo ci) {
        if (!(((StatsDataAccessor) (Object) data).dmzgrowth$getPlayer() instanceof ServerPlayer sp)) return;
        if (!ZenkaiGift.apply(sp, data, config.getBuffStats())) return; // sin datos de Potencial: comportamiento original

        if (isMaxBuff) { // mismo bono de liberacion que aplicaba DMZ
            RacialData rd = data.getRacialData();
            rd.setZenkaiReleaseBonus(Math.min(config.getReleaseBonusCap(), rd.getZenkaiReleaseBonus() + config.getReleaseBonusPerZenkai()));
        }
        ci.cancel();
    }

    /** Sin cooldown de Zenkai: ya no bloquea un nuevo Zenkai (la eficacia decreciente lo regula). */
    @Redirect(method = "blockingReason", at = @At(value = "INVOKE",
            target = "Lcom/dragonminez/common/stats/character/Cooldowns;hasCooldown(Ljava/lang/String;)Z"))
    private static boolean dmzgrowth$noZenkaiCooldownCheck(Cooldowns cooldowns, String key) {
        return !Cooldowns.ZENKAI.equals(key) && cooldowns.hasCooldown(key);
    }

    /** No se programa el cooldown del Zenkai; el knockout y la invulnerabilidad de DMZ se mantienen. */
    @Redirect(method = "trigger", at = @At(value = "INVOKE",
            target = "Lcom/dragonminez/common/stats/character/Cooldowns;setCooldown(Ljava/lang/String;I)V"))
    private static void dmzgrowth$noZenkaiCooldownSet(Cooldowns cooldowns, String key, int ticks) {
        if (!Cooldowns.ZENKAI.equals(key)) cooldowns.setCooldown(key, ticks);
    }
}
