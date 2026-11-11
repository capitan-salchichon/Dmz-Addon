package com.brochacho.dmzgrowth.client;

import com.dragonminez.client.render.util.ModRenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/** Mismo mecanismo que usa DMZ (shader "assimilation sketch"): el progreso viaja en las coordenadas de overlay. */
public record WhiteSource(MultiBufferSource delegate, int encodedProgress) implements MultiBufferSource {
    @Override
    public VertexConsumer getBuffer(RenderType type) {
        RenderType sketch = ModRenderTypes.assimilationSketch(type);
        if (sketch == null) return delegate.getBuffer(type);
        return new Consumer(delegate.getBuffer(sketch), encodedProgress);
    }

    private record Consumer(VertexConsumer d, int progress) implements VertexConsumer {
        @Override public VertexConsumer vertex(double x, double y, double z) { d.vertex(x, y, z); return this; }
        @Override public VertexConsumer color(int r, int g, int b, int a) { d.color(r, g, b, a); return this; }
        @Override public VertexConsumer uv(float u, float v) { d.uv(u, v); return this; }
        @Override public VertexConsumer overlayCoords(int u, int v) { d.overlayCoords(progress, 0); return this; }
        @Override public VertexConsumer uv2(int u, int v) { d.uv2(u, v); return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { d.normal(x, y, z); return this; }
        @Override public void endVertex() { d.endVertex(); }
        @Override public void defaultColor(int r, int g, int b, int a) { d.defaultColor(r, g, b, a); }
        @Override public void unsetDefaultColor() { d.unsetDefaultColor(); }
    }
}
