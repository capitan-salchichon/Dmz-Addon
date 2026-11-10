package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.mixin.MinigameSessionsAccessor;
import com.dragonminez.server.util.GravityLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Entrenamiento Extremo = estar en un minijuego de TPs con carga efectiva entre 5 x peso ideal y el umbral de sobrecarga (mas alla pierde progreso) (GravityLogic.getLoadRatio).
 * Es el UNICO punto del addon que sube Limite y Potencial.
 */
public class ExtremeTrainingHandler {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 20 != 0) return; // 1 comprobacion por segundo

        // Solo cuenta mientras haya una sesion de minijuego de TPs activa; al salir, el progreso queda guardado.
        if (!MinigameSessionsAccessor.dmzgrowth$getSessions().containsKey(sp.getUUID())) return;

        // Entrenar tambien recupera la eficacia del Zenkai (menos Zenkais seguidos).
        sp.getCapability(GrowthCapability.INSTANCE).ifPresent(com.brochacho.dmzgrowth.data.GrowthData::trainingSecond);

        if (GrowthConfig.EXTREME_REQUIRE_WEIGHT.get()) {
            // getIdealWeight devuelve 0 si tpEnabled esta desactivado en DMZ -> no avanza.
            if (GravityLogic.getIdealWeight(sp) <= 0) return;
            double ratio = GravityLogic.getLoadRatio(sp);
            if (ratio < GrowthConfig.EXTREME_WEIGHT_FACTOR.get()) return;
            if (ratio > GrowthConfig.EXTREME_OVERLOAD_RATIO.get()) {
                // Contraproducente: demasiado peso hace perder progreso en vez de ganarlo.
                sp.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
                    g.penalizeExtreme(GrowthConfig.EXTREME_OVERLOAD_PENALTY_SECONDS.get());
                    GrowthNetwork.sync(sp);
                });
                return;
            }
        }

        sp.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
            long gain = g.tickExtremeSecond(sp.getRandom());
            if (gain > 0) {
                GrowthNetwork.sync(sp);
            }
        });
    }
}
