package com.brochacho.dmzgrowth.ability;

import com.dragonminez.common.racial.RacialContext;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registro de habilidades del addon. Sin limite de activas ni pasivas. Registrar en el constructor del mod. */
public final class AbilityRegistry {
    private static final Map<String, Ability> ABILITIES = new LinkedHashMap<>();

    private AbilityRegistry() {}

    public static void register(Ability ability) {
        if (ABILITIES.putIfAbsent(ability.id(), ability) != null)
            throw new IllegalStateException("Habilidad duplicada: " + ability.id());
    }

    public static Collection<Ability> all() { return Collections.unmodifiableCollection(ABILITIES.values()); }

    public static boolean isEmpty() { return ABILITIES.isEmpty(); }

    /** Habilidades (pasivas y activas) que aplican a este jugador ahora mismo. */
    public static List<Ability> forPlayer(StatsData data) {
        if (ABILITIES.isEmpty()) return List.of();
        List<Ability> out = new ArrayList<>(ABILITIES.size());
        for (Ability a : ABILITIES.values()) if (a.appliesTo(data)) out.add(a);
        return out;
    }

    /** Uso de una activa, validado en el servidor: existe, aplica, sin cooldown, con energia y requisitos. */
    public static boolean use(ServerPlayer player, StatsData data, String id) {
        Ability a = ABILITIES.get(id);
        if (a == null || !a.isActive() || !data.getStatus().isHasCreatedCharacter() || !a.appliesTo(data)) return false;
        var cooldowns = data.getCooldowns();
        if (cooldowns.hasCooldown(a.cooldownKey())) return false;
        int cost = a.energyCost();
        if (cost > 0 && data.getResources().getCurrentEnergy() < cost) return false;
        RacialContext ctx = new RacialContext(player, data);
        if (!a.canUse(ctx) || !a.use(ctx)) return false;
        if (cost > 0) data.getResources().addEnergy(-cost);
        if (a.cooldownTicks() > 0) cooldowns.setCooldown(a.cooldownKey(), a.cooldownTicks());
        return true;
    }
}
