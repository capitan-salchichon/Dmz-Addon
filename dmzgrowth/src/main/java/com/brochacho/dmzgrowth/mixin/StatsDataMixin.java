package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.GrowthEvents;
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
        int current = getCurrentStatValue(statName);
        player.getCapability(GrowthCapability.INSTANCE).ifPresent(g ->
                cir.setReturnValue(Math.min(base, g.maxPointsAllowed(current))));
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
}
