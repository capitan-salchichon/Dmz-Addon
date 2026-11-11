package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.DMZGrowth;
import com.brochacho.dmzgrowth.namek.AssimilationTiming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Linea de tiempo visual de la asimilacion (reloj propio del cliente). Entrega, por entidad, un "progreso de blanco"
 * que el shader de DMZ ya sabe dibujar: 0..0.85 = se vuelve blanco, 0.85..1 = se disuelve hasta desaparecer.
 */
@Mod.EventBusSubscriber(modid = DMZGrowth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AssimilationSequenceClient {
    private record Entry(int start, boolean target) {}

    private static final Map<Integer, Entry> ACTIVE = new HashMap<>();
    private static int clock;

    private AssimilationSequenceClient() {}

    public static void start(int targetId, int assimilatorId) {
        ACTIVE.putIfAbsent(targetId, new Entry(clock, true));
        ACTIVE.putIfAbsent(assimilatorId, new Entry(clock, false));
    }

    public static boolean isActive(int entityId) { return ACTIVE.containsKey(entityId); }

    /** 0..1; mayor que 0.85 significa "disolviendose". */
    public static float progress(int entityId, float partialTick) {
        Entry en = ACTIVE.get(entityId);
        if (en == null) return 0f;
        float age = clock - en.start() + partialTick;
        return en.target() ? targetProgress(age) : assimilatorProgress(age);
    }

    private static float targetProgress(float age) {
        float cap = AssimilationTiming.WHITE_CAP;
        int in = AssimilationTiming.WHITE_IN, hold = AssimilationTiming.HOLD, dis = AssimilationTiming.DISSOLVE;
        if (age < in) return cap * age / in;
        if (age < in + hold) return cap;
        if (age < in + hold + dis) return cap + (1f - cap) * (age - in - hold) / dis;
        return 1f; // invisible hasta que termina (el servidor lo reinicia en este tramo)
    }

    private static float assimilatorProgress(float age) {
        float cap = AssimilationTiming.WHITE_CAP;
        int s = AssimilationTiming.ASSIM_START, f = AssimilationTiming.ASSIM_FADE, reset = AssimilationTiming.RESET_AT;
        if (age < s) return 0f;
        if (age < s + f) return cap * (age - s) / f;
        if (age < reset) return cap;
        if (age < reset + f) return cap * (1f - (age - reset) / f);
        return 0f;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) { ACTIVE.clear(); return; }
        if (mc.isPaused() || ACTIVE.isEmpty()) return;
        clock++;

        Iterator<Map.Entry<Integer, Entry>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            Entry v = en.getValue();
            int age = clock - v.start();
            int end = v.target() ? AssimilationTiming.TARGET_END : AssimilationTiming.ASSIM_END;
            Entity entity = level.getEntity(en.getKey());
            if (age >= end || entity == null) { it.remove(); continue; }

            // "Luz blanca": particulas brillantes alrededor mientras esta blanco (no hay luz dinamica real en el mundo).
            float p = progress(en.getKey(), 0f);
            if (p > 0.3f && p < 1f) {
                AABB box = entity.getBoundingBox();
                for (int i = 0; i < 3; i++) {
                    level.addParticle(ParticleTypes.END_ROD,
                            Mth.lerp(level.random.nextDouble(), box.minX, box.maxX),
                            Mth.lerp(level.random.nextDouble(), box.minY, box.maxY),
                            Mth.lerp(level.random.nextDouble(), box.minZ, box.maxZ), 0.0, 0.03, 0.0);
                }
            }
        }
    }
}
