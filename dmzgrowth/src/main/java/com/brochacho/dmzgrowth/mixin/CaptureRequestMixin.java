package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.namek.ConsentFlag;
import com.dragonminez.common.racial.capture.CaptureRequest;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CaptureRequest.class, remap = false)
public abstract class CaptureRequestMixin {
    /** Marca que lo que sigue es una respuesta ACEPTADA del objetivo. */
    @Inject(method = "reply", at = @At("HEAD"))
    private static void dmzgrowth$begin(ServerPlayer target, boolean accepted, CallbackInfo ci) {
        ConsentFlag.ACCEPTED.set(accepted);
    }

    @Inject(method = "reply", at = @At("RETURN"))
    private static void dmzgrowth$end(ServerPlayer target, boolean accepted, CallbackInfo ci) {
        ConsentFlag.ACCEPTED.set(false);
    }
}
