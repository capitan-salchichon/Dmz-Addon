package com.brochacho.dmzgrowth.mixin;

import com.dragonminez.common.config.FormConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Linea de evolucion del Bio-Android (grupo "bioevolution"), SOLO esa linea:
 *  - se eliminan Super Perfecto y Ultra Perfecto;
 *  - las formas que quedan no gastan ki ni stamina (los multiplicadores de stats siguen en la configuracion).
 * Otras lineas (legendarias/Xeno o futuras como Ultra Ego) no se tocan.
 */
@Mixin(value = FormConfig.class, remap = false)
public abstract class FormConfigMixin {
    private static final String EVOLUTION_GROUP = "bioevolution";
    private static final Set<String> REMOVED = Set.of("superperfect", "ultraperfect");

    private boolean dmzgrowth$isEvolution() {
        return EVOLUTION_GROUP.equals(((FormConfig) (Object) this).getGroupName());
    }

    private static boolean removed(String name) {
        return name != null && REMOVED.contains(name.toLowerCase(Locale.ROOT));
    }

    private static FormConfig.FormData noUpkeep(FormConfig.FormData fd) {
        if (fd != null) {
            fd.setEnergyDrain(0.0);
            fd.setStaminaDrain(0.0);
            fd.setStaminaDrainMultiplier(0.0);
        }
        return fd;
    }

    @Inject(method = "getForms", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$forms(CallbackInfoReturnable<Map<String, FormConfig.FormData>> cir) {
        if (!dmzgrowth$isEvolution()) return;
        Map<String, FormConfig.FormData> out = new LinkedHashMap<>();
        for (var e : cir.getReturnValue().entrySet())
            if (!removed(e.getKey())) out.put(e.getKey(), noUpkeep(e.getValue()));
        cir.setReturnValue(out);
    }

    @Inject(method = "getForm", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$form(String formName, CallbackInfoReturnable<FormConfig.FormData> cir) {
        if (!dmzgrowth$isEvolution()) return;
        cir.setReturnValue(removed(formName) ? null : noUpkeep(cir.getReturnValue()));
    }

    @Inject(method = "getFormByKey", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$formByKey(String key, CallbackInfoReturnable<FormConfig.FormData> cir) {
        if (!dmzgrowth$isEvolution()) return;
        cir.setReturnValue(removed(key) ? null : noUpkeep(cir.getReturnValue()));
    }
}
