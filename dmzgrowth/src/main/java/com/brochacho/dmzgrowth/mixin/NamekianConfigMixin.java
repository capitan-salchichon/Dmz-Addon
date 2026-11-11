package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.GrowthConfig;
import com.dragonminez.common.config.GeneralServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GeneralServerConfig.NamekianRacialConfig.class, remap = false)
public abstract class NamekianConfigMixin {
    /** Regeneracion namekiana: cura TODA la vida (DMZ: 25%); sigue gastando ki y stamina como en DMZ. */
    @Inject(method = "getRegenHealthRatio", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$fullRegen(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(GrowthConfig.NAMEK_REGEN_HEALTH_RATIO.get());
    }
}
