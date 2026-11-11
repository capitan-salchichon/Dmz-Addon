package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.AssimilationRequestScreen;
import com.dragonminez.common.network.S2C.RacialRequestS2C;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RacialRequestS2C.class, remap = false)
public abstract class RacialRequestScreenMixin {
    /** En vez del mensaje de chat con enlaces, abre una pantalla de confirmacion. */
    @Inject(method = "show", at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$screen(String requesterName, int timeoutSeconds, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new AssimilationRequestScreen(requesterName, timeoutSeconds));
        ci.cancel();
    }
}
