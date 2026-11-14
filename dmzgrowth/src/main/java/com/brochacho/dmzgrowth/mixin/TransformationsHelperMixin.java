package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.GrowthConfig;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.util.TransformationsHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TransformationsHelper.class, remap = false)
public abstract class TransformationsHelperMixin {
    /**
     * Punto unico por el que DMZ devuelve a un jugador a su forma base (quedarse sin ki, fin de duracion, rabia...).
     * La evolucion del Bio-Android es su forma base: esas salidas NO la quitan. Las salidas forzadas (respectFloor = false:
     * fusion, rituales, comandos) se respetan.
     */
    @Inject(method = "revertToBaseForm(Lnet/minecraft/server/level/ServerPlayer;Lcom/dragonminez/common/stats/StatsData;Z)Z",
            at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$evolutionStays(ServerPlayer player, StatsData data, boolean respectFloor, CallbackInfoReturnable<Boolean> cir) {
        if (!respectFloor || !GrowthConfig.BIO_EVOLUTION_IS_BASE.get()) return;
        var c = data.getCharacter();
        if ("bioandroid".equals(c.getRaceName()) && "bioevolution".equalsIgnoreCase(c.getActiveFormGroup()) && c.hasActiveForm())
            cir.setReturnValue(false);
    }
}
