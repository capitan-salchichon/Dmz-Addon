package com.brochacho.dmzgrowth.mixin;

import com.dragonminez.common.racial.impl.NamekAssimilation;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NamekAssimilation.class, remap = false)
public abstract class NamekRegenGuardMixin {
    /** La regeneracion namekiana solo se puede iniciar si el jugador ES namekiano (validado en el servidor). */
    @Inject(method = "startRegen", at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$onlyNamekians(ServerPlayer player, StatsData data, CallbackInfo ci) {
        if (!"namekian".equals(data.getCharacter().getRaceName())) ci.cancel();
    }
}
