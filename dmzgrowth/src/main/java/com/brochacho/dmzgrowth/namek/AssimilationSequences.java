package com.brochacho.dmzgrowth.namek;

import com.brochacho.dmzgrowth.net.AssimilationSequenceS2C;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.init.MainEffects;
import com.dragonminez.common.stats.StatsCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Secuencia de asimilacion en el servidor: inmoviliza e hace invulnerable al asimilado, y al final aplica el reinicio y la suma. */
public class AssimilationSequences {
    private static final class Seq {
        final UUID namek;
        int age;
        Seq(UUID namek) { this.namek = namek; }
    }

    private static final Map<UUID, Seq> ACTIVE = new HashMap<>(); // clave: asimilado

    public static void start(ServerPlayer namek, ServerPlayer target) {
        if (ACTIVE.containsKey(target.getUUID())) return;
        ACTIVE.put(target.getUUID(), new Seq(namek.getUUID()));
        freeze(target, AssimilationTiming.TARGET_END + 5);
        var msg = new AssimilationSequenceS2C(target.getId(), namek.getId());
        GrowthNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), msg);
        GrowthNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> namek), msg);
    }

    private static void freeze(ServerPlayer target, int ticks) {
        target.addEffect(new MobEffectInstance(MainEffects.STUN.get(), ticks, 0, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 128, false, false, false)); // sin saltar
    }

    private static void release(ServerPlayer target) {
        target.removeEffect(MainEffects.STUN.get());
        target.removeEffect(MobEffects.JUMP);
    }

    /** Invulnerable durante la secuencia por EVENTO (la bandera de invulnerabilidad de Minecraft se guarda en disco). */
    @SubscribeEvent
    public void onAttack(LivingAttackEvent e) {
        if (!ACTIVE.isEmpty() && e.getEntity() instanceof ServerPlayer sp && ACTIVE.containsKey(sp.getUUID())) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        Iterator<Map.Entry<UUID, Seq>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            Seq s = en.getValue();
            ServerPlayer target = server.getPlayerList().getPlayer(en.getKey());
            ServerPlayer namek = server.getPlayerList().getPlayer(s.namek);

            // Si alguno se desconecta o muere antes del reinicio, se cancela sin cambios.
            if (target == null) { it.remove(); continue; }
            if (s.age < AssimilationTiming.RESET_AT && (namek == null || !namek.isAlive() || !target.isAlive())) {
                release(target);
                it.remove();
                continue;
            }

            s.age++;
            if (s.age == AssimilationTiming.RESET_AT && namek != null) {
                namek.getCapability(StatsCapability.INSTANCE).ifPresent(data -> PlayerAssimilation.apply(namek, data, target));
                freeze(target, AssimilationTiming.TARGET_END - AssimilationTiming.RESET_AT + 5); // el reinicio puede limpiar efectos
            }
            if (s.age >= AssimilationTiming.TARGET_END) {
                release(target);
                it.remove();
            }
        }
    }
}
