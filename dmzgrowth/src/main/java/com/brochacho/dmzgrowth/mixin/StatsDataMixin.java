package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.GrowthConfig;
import com.brochacho.dmzgrowth.GrowthEvents;
import com.brochacho.dmzgrowth.GrowthMath;
import com.brochacho.dmzgrowth.ProgressiveTransformation;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StatsData.class, remap = false)
public abstract class StatsDataMixin {
    @Shadow @Final private Player player;
    @Shadow public abstract int getCurrentStatValue(String statName);

    /** Limite + Potencial: ambos lados (el cliente usa el mismo metodo para mostrar/ocultar botones). */
    @Inject(method = "getMaxAllowedIncreaseForStat", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$limitIncrease(String statName, int requested, CallbackInfoReturnable<Integer> cir) {
        if (player == null) return;
        int base = cir.getReturnValue();
        if (base <= 0) return;
        StatsData self = (StatsData) (Object) this;
        player.getCapability(GrowthCapability.INSTANCE).ifPresent(g ->
                cir.setReturnValue(Math.min(base, GrowthMath.maxPoints(self, statName, base, g))));
    }

    /** Potencial inicial: se tira solo al crear el personaje (rollInitial ignora si ya existe). */
    @Inject(method = "initializeWithRaceAndClass", at = @At("RETURN"))
    private void dmzgrowth$rollOnCreate(CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) {
            GrowthEvents.rollIfCreatedAndMissing(sp);
            GrowthNetwork.sync(sp);
        }
    }

    /** Transformacion Normal progresiva (afecta Poder, stats efectivas y todo lo que lea getFormMultiplier). */
    @Inject(method = "getFormMultiplier", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$progressiveForm(String statName, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(ProgressiveTransformation.adjust((StatsData) (Object) this, statName, cir.getReturnValue()));
    }

    // ---- Tecnicas de expulsion de ki: el dano pasa a usar el Ki Completo (PWR + ENE) ----

    /** Termino de ENE que se suma al calculo original de DMZ (que solo usaba PWR). */
    private double dmzgrowth$kiEnergyTerm(boolean withFormMult, boolean withRelease) {
        double w = GrowthConfig.ENERGY_KI_WEIGHT.get();
        if (w <= 0.0) return 0.0;
        StatsData self = (StatsData) (Object) this;
        double t = w * self.getStats().getEnergy() * self.getStatScaling("ENE");
        if (withFormMult) t *= self.getTotalMultiplier("ENE");
        if (withRelease) t *= self.getResources().getPowerRelease() / 100.0;
        return t;
    }

    @Inject(method = "getKiDamage", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$kiDamage(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() + dmzgrowth$kiEnergyTerm(true, true));
    }

    @Inject(method = "getMaxKiDamage", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$maxKiDamage(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() + dmzgrowth$kiEnergyTerm(true, false));
    }

    @Inject(method = "getKiDamageNoForms", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$kiDamageNoForms(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() + dmzgrowth$kiEnergyTerm(false, true));
    }

    /** Reinicio completo de personaje (Dende, asimilacion): el Potencial y el Limite vuelven a su estado inicial. */
    @Inject(method = "resetPlayerProgress", at = @At("RETURN"))
    private void dmzgrowth$resetGrowth(ServerPlayer p, Integer keepPercentage, boolean keepSkills, boolean forceSaiyanTail, CallbackInfo ci) {
        if (keepPercentage != null) return; // reinicio parcial: no se toca
        p.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
            g.resetToInitial();
            GrowthNetwork.sync(p);
        });
    }
}
