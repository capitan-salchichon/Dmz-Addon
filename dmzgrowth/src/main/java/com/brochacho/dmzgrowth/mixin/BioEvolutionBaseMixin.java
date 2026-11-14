package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.GrowthConfig;
import com.dragonminez.common.network.C2S.ExecuteActionC2S;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExecuteActionC2S.class, remap = false)
public abstract class BioEvolutionBaseMixin {
    /**
     * La evolucion del Bio-Android (grupo "bioevolution") se vuelve su forma base: no se puede bajar de ella.
     * No es permanente: DMZ ya reinicia la forma activa al morir. Solo afecta a esa linea y a esa raza.
     * (ignoreEvolutionFloor = descensos forzados por el propio DMZ: se respetan.)
     */
    @Inject(method = "descendForm", at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$evolutionIsBase(ServerPlayer player, StatsData data, boolean ignoreEvolutionFloor, CallbackInfo ci) {
        if (ignoreEvolutionFloor || !GrowthConfig.BIO_EVOLUTION_IS_BASE.get()) return;
        var c = data.getCharacter();
        if ("bioandroid".equals(c.getRaceName()) && "bioevolution".equalsIgnoreCase(c.getActiveFormGroup()) && c.hasActiveForm())
            ci.cancel();
    }
}
