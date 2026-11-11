package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.namek.ConsentFlag;
import com.brochacho.dmzgrowth.namek.PlayerAssimilation;
import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.racial.capture.CaptureRequest;
import com.dragonminez.common.racial.impl.NamekAssimilation;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NamekAssimilation.class, remap = false)
public abstract class NamekAssimilationMixin {
    /**
     * Objetivo JUGADOR: nunca se asimila sin que haya aceptado (DMZ lo saltaba si estaba derribado); al aceptar,
     * no muere: se reinicia y sus poderes se suman. Objetivos no jugador: comportamiento original de DMZ.
     */
    @Inject(method = "applyAssimilation", at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$playerTargets(ServerPlayer namek, StatsData data, LivingEntity target, CallbackInfo ci) {
        if (!(target instanceof ServerPlayer targetPlayer)) return;
        ci.cancel();
        if (!ConsentFlag.ACCEPTED.get()) {
            int timeout = ConfigManager.getServerConfig().getRacialSkills().getNamekian().getAssimilationRequestTimeoutSeconds();
            CaptureRequest.send(namek, targetPlayer, timeout);
            return;
        }
        ConsentFlag.ACCEPTED.set(false);
        PlayerAssimilation.apply(namek, data, targetPlayer);
    }
}
