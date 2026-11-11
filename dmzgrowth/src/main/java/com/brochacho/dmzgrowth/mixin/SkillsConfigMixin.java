package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.namek.ExclusiveSkills;
import com.dragonminez.common.config.SkillsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SkillsConfig.class, remap = false)
public abstract class SkillsConfigMixin {
    /**
     * Punto central de DMZ para "esta raza puede tener/comprar esta skill" (tienda, mejora, equipar, fusion).
     * La regeneracion namekiana no se vende a ninguna raza: el namekiano la trae por defecto como habilidad racial.
     */
    @Inject(method = "isSkillAllowedForRace(Ljava/lang/String;Ljava/lang/String;Z)Z", at = @At("HEAD"), cancellable = true)
    private void dmzgrowth$noRegenShop(String skillName, String raceName, boolean androidUpgraded, CallbackInfoReturnable<Boolean> cir) {
        if (ExclusiveSkills.isNamekRegen(skillName)) cir.setReturnValue(false);
    }
}
