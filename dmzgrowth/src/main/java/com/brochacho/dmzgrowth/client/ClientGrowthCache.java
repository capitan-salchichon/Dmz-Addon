package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Ultima copia de los datos del servidor. Al cambiar de dimension el cliente crea un jugador NUEVO (sin datos); esta cache se
 * los vuelve a poner. Tambien guarda los valores de configuracion del servidor que muestra la interfaz.
 */
public final class ClientGrowthCache {
    private static volatile CompoundTag last;
    public static volatile double factor = 5.0;
    public static volatile double overload = 6.0;
    public static volatile int secondsRequired = 300;

    private ClientGrowthCache() {}

    public static void store(CompoundTag tag, double f, double o, int seconds) {
        last = tag;
        factor = f;
        overload = o;
        secondsRequired = seconds;
    }

    public static void clear() { last = null; }

    /** Eleccion de accion racial ("" o "regeneration"), tal como la ve el cliente. */
    public static String choice(Player p) {
        GrowthData d = ensure(p);
        return d == null ? "" : d.getRacialChoice();
    }

    public static void setChoiceLocal(Player p, String c) {
        GrowthData d = ensure(p);
        if (d != null) d.setRacialChoice(c);
    }

    public static void applyTo(Player p) {
        CompoundTag t = last;
        if (p == null || t == null) return;
        p.getCapability(GrowthCapability.INSTANCE).ifPresent(d -> d.load(t));
    }

    /** Devuelve los datos del jugador; si estan vacios (jugador recien creado) los rellena desde la cache. */
    public static GrowthData ensure(Player p) {
        if (p == null) return null;
        var opt = p.getCapability(GrowthCapability.INSTANCE).resolve();
        if (opt.isEmpty()) return null;
        GrowthData d = opt.get();
        if (!d.isRolled() && last != null) d.load(last);
        return d;
    }
}
