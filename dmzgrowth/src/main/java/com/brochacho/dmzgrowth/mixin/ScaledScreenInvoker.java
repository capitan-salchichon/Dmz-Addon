package com.brochacho.dmzgrowth.mixin;

import com.dragonminez.client.gui.character.util.ScaledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** getUiWidth/getUiHeight viven en la clase padre de la pantalla V; se acceden con un invoker. */
@Mixin(value = ScaledScreen.class, remap = false)
public interface ScaledScreenInvoker {
    @Invoker(value = "getUiWidth", remap = false) int dmzgrowth$getUiWidth();
    @Invoker(value = "getUiHeight", remap = false) int dmzgrowth$getUiHeight();
}
