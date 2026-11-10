package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.DMZGrowth;
import com.brochacho.dmzgrowth.ability.Ability;
import com.brochacho.dmzgrowth.ability.AbilityRegistry;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.UseAbilityC2S;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.Map;

/** Una tecla configurable por cada habilidad activa registrada (sin limite). Solo envia la peticion; valida el servidor. */
public final class AbilityKeys {
    private static final Map<String, KeyMapping> KEYS = new LinkedHashMap<>();

    private AbilityKeys() {}

    @Mod.EventBusSubscriber(modid = DMZGrowth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Register {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent e) {
            for (Ability a : AbilityRegistry.all()) {
                if (!a.isActive()) continue;
                KeyMapping km = new KeyMapping("key.dmzgrowth.ability." + a.id(), InputConstants.UNKNOWN.getValue(), "key.categories.dmzgrowth");
                KEYS.put(a.id(), km);
                e.register(km);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = DMZGrowth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class Tick {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END || KEYS.isEmpty()) return;
            for (var en : KEYS.entrySet())
                while (en.getValue().consumeClick()) GrowthNetwork.CHANNEL.sendToServer(new UseAbilityC2S(en.getKey()));
        }
    }
}
