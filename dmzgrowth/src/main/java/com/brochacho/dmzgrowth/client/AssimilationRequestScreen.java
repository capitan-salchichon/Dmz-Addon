package com.brochacho.dmzgrowth.client;

import com.dragonminez.common.network.C2S.RacialRequestReplyC2S;
import com.dragonminez.common.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pantalla de confirmacion: "X quiere asimilarte". Responde con el mismo paquete que usa el comando de DMZ. */
public class AssimilationRequestScreen extends Screen {
    private final String requester;
    private final long expiresAtMs;

    public AssimilationRequestScreen(String requester, int timeoutSeconds) {
        super(Component.literal("Asimilacion"));
        this.requester = requester;
        this.expiresAtMs = System.currentTimeMillis() + timeoutSeconds * 1000L;
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 2 + 30;
        addRenderableWidget(Button.builder(Component.literal("Aceptar"), b -> reply(true)).bounds(cx - 105, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Rechazar"), b -> reply(false)).bounds(cx + 5, y, 100, 20).build());
    }

    private void reply(boolean accepted) {
        NetworkHandler.sendToServer(new RacialRequestReplyC2S(accepted));
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2, y = height / 2 - 40;
        g.drawCenteredString(font, Component.literal(requester + " quiere asimilarte"), cx, y, 0xFFFFFF);
        g.drawCenteredString(font, Component.literal("Si aceptas, tu personaje se reiniciara desde 0."), cx, y + 14, 0xFF5555);
        g.drawCenteredString(font, Component.literal("Tus poderes de pelea se sumaran a los suyos."), cx, y + 26, 0xFFFF55);
        long left = Math.max(0, (expiresAtMs - System.currentTimeMillis()) / 1000);
        g.drawCenteredString(font, Component.literal("Tiempo restante: " + left + " s"), cx, y + 44, 0xAAAAAA);
        if (left <= 0) onClose();
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
    /** Cerrar con Esc equivale a no responder: la solicitud caduca sola. */
    @Override public boolean shouldCloseOnEsc() { return true; }
}
