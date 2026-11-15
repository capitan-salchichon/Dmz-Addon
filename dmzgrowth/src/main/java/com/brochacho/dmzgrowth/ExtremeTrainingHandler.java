package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import com.brochacho.dmzgrowth.mixin.MinigameSessionsAccessor;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.server.util.GravityLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Entrenamiento Extremo = estar en un minijuego de TPs con carga efectiva entre 5 x peso ideal y el umbral de sobrecarga
 * (mas alla pierde progreso) (GravityLogic.getLoadRatio). Es el UNICO punto del addon que sube Limite y Potencial.
 * Ademas informa al cliente de lo que detecta (minijuego si/no y carga actual) para mostrarlo en la barra.
 */
public class ExtremeTrainingHandler {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 20 != 0) return; // 1 comprobacion por segundo

        GrowthData g = sp.getCapability(GrowthCapability.INSTANCE).resolve().orElse(null);
        if (g == null) return;

        boolean inSession = MinigameSessionsAccessor.dmzgrowth$getSessions().containsKey(sp.getUUID());
        double ratio = -1;
        if (inSession && GravityLogic.getIdealWeight(sp) > 0) ratio = GravityLogic.getLoadRatio(sp);

        // Estado informativo para la barra: se envia al entrar/salir del minijuego y cada 2 s mientras dura.
        boolean changed = inSession != g.isLiveInSession();
        g.setLive(inSession, ratio);
        if (changed || (inSession && sp.tickCount % 40 == 0)) GrowthNetwork.sync(sp);
        if (!inSession) return; // el progreso queda guardado al salir

        // Entrenar tambien recupera la eficacia del Zenkai (menos Zenkais seguidos).
        g.trainingSecond();

        if (GrowthConfig.EXTREME_REQUIRE_WEIGHT.get()) {
            if (ratio < 0) return; // sin peso ideal (TPs desactivados en DMZ) no avanza
            if (ratio < GrowthConfig.EXTREME_WEIGHT_FACTOR.get()) return;
            if (ratio > GrowthConfig.EXTREME_OVERLOAD_RATIO.get()) {
                // Contraproducente: demasiado peso hace perder progreso en vez de ganarlo.
                g.penalizeExtreme(GrowthConfig.EXTREME_OVERLOAD_PENALTY_SECONDS.get());
                GrowthNetwork.sync(sp);
                return;
            }
        }

        long gain = g.tickExtremeSecond(sp.getRandom());
        if (gain > 0 || g.getExtremeSeconds() % 5 == 0) GrowthNetwork.sync(sp); // para que la barra avance en pantalla
    }
}
