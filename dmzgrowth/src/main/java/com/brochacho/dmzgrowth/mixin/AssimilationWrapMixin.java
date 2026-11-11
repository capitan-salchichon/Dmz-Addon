package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.AssimilationSequenceClient;
import com.brochacho.dmzgrowth.client.WhiteSource;
import com.dragonminez.client.render.effects.NamekAssimilationEffect;
import com.dragonminez.client.render.layer.DMZSkinLayer;
import com.dragonminez.client.render.util.IrisCompat;
import com.dragonminez.client.render.util.ModRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NamekAssimilationEffect.class, remap = false)
public abstract class AssimilationWrapMixin {
    /** DMZ llama a wrap() al dibujar cada entidad; si hay una secuencia nuestra, usa su progreso con el mismo shader. */
    @Inject(method = "wrap", at = @At("HEAD"), cancellable = true)
    private static void dmzgrowth$wrap(Entity entity, MultiBufferSource source, float partialTick,
                                       CallbackInfoReturnable<MultiBufferSource> cir) {
        if (!AssimilationSequenceClient.isActive(entity.getId())) return;
        if (DMZSkinLayer.PREVIEW_MODE || IrisCompat.isRenderingShadowPass()) return;
        if (!ModRenderTypes.hasAssimilationSketchShader() || IrisCompat.isShaderPackInUse()) return;
        float progress = AssimilationSequenceClient.progress(entity.getId(), partialTick);
        if (progress <= 0.001f) return;
        cir.setReturnValue(new WhiteSource(source, Math.round(progress * 1000.0f)));
    }
}
