package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.ability.AbilityRegistry;
import com.brochacho.dmzgrowth.ability.CompositeRacialAbility;
import com.dragonminez.common.racial.RacialAbility;
import com.dragonminez.common.racial.RacialRegistry;
import com.dragonminez.common.stats.StatsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = RacialRegistry.class, remap = false)
public abstract class RacialRegistryMixin {
    /** Todos los puntos donde DMZ consulta "la" racial pasan a consultar la racial original + las del addon. */
    @Inject(method = "forPlayer", at = @At("RETURN"), cancellable = true)
    private static void dmzgrowth$composite(StatsData data, CallbackInfoReturnable<Optional<RacialAbility>> cir) {
        if (AbilityRegistry.isEmpty()) return; // sin habilidades del addon: costo cero
        var extras = AbilityRegistry.forPlayer(data);
        if (extras.isEmpty()) return;
        cir.setReturnValue(Optional.of(new CompositeRacialAbility(cir.getReturnValue().orElse(null), extras)));
    }
}
