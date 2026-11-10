package com.brochacho.dmzgrowth;

import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.PresenceSyncS2C;
import com.dragonminez.common.init.MainItems;
import com.dragonminez.common.racial.impl.GlindDivinity;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import com.dragonminez.common.util.CuriosUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Deteccion por Presencia a gran alcance. El scouter de DMZ solo ve entidades cargadas en el cliente (50 bloques);
 * aqui el SERVIDOR calcula que presencias detecta cada portador de scouter (misma dimension, hasta el alcance
 * configurado) y las envia. Poder detectable = getBattlePower de DMZ, que ya escala con el % de liberacion
 * (Presencia). Respeta el Anti-Ki Cloak y la ocultacion de Glind Divinity, igual que el scouter original.
 */
public class PresenceBroadcaster {
    private int counter;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (++counter < GrowthConfig.SCOUTER_INTERVAL_TICKS.get()) return;
        counter = 0;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.size() < 2) return;

        double range = GrowthConfig.SCOUTER_RANGE.get();
        double rangeSq = range * range;
        int max = GrowthConfig.SCOUTER_MAX_ENTRIES.get();

        for (ServerPlayer viewer : players) {
            if (CuriosUtil.getFirstStackForItem(viewer, "head_tech", "scouter").isEmpty()) continue;

            List<PresenceSyncS2C.Entry> found = new ArrayList<>();
            for (ServerPlayer target : players) {
                if (target == viewer || target.level() != viewer.level() || !target.isAlive() || target.isSpectator()) continue;
                if (target.distanceToSqr(viewer) > rangeSq) continue;
                if (CuriosUtil.getFirstStackForItem(target, "head_tech", "anti_ki_cloak").getItem() == MainItems.ANTI_KI_CLOAK.get()) continue;
                if (GlindDivinity.isHiddenFrom(target, viewer)) continue;

                StatsData data = StatsProvider.get(StatsCapability.INSTANCE, target).resolve().orElse(null);
                if (data == null || !data.getStatus().isHasCreatedCharacter()) continue;
                float bp = data.getBattlePower();
                if (bp <= 0f) continue;
                found.add(new PresenceSyncS2C.Entry(target.getGameProfile().getName(), target.getX(), target.getY(), target.getZ(),
                        bp, data.getResources().getPowerRelease()));
            }
            found.sort(Comparator.comparingDouble(PresenceSyncS2C.Entry::presenceBp).reversed());
            if (found.size() > max) found = new ArrayList<>(found.subList(0, max));
            // Se envia tambien la lista vacia para que el cliente borre presencias que ya no detecta.
            GrowthNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), new PresenceSyncS2C(found));
        }
    }
}
