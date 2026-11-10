package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.client.PresenceClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Presencias detectables (poder visible, no oculto) de jugadores lejanos, para el scouter. */
public class PresenceSyncS2C {
    public record Entry(String name, double x, double y, double z, float presenceBp, int releasePercent) {}

    private final List<Entry> entries;

    public PresenceSyncS2C(List<Entry> entries) { this.entries = entries; }

    public static void encode(PresenceSyncS2C m, FriendlyByteBuf b) {
        b.writeVarInt(m.entries.size());
        for (Entry e : m.entries) {
            b.writeUtf(e.name(), 64);
            b.writeDouble(e.x()); b.writeDouble(e.y()); b.writeDouble(e.z());
            b.writeFloat(e.presenceBp());
            b.writeVarInt(e.releasePercent());
        }
    }

    public static PresenceSyncS2C decode(FriendlyByteBuf b) {
        int n = b.readVarInt();
        List<Entry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            list.add(new Entry(b.readUtf(64), b.readDouble(), b.readDouble(), b.readDouble(), b.readFloat(), b.readVarInt()));
        return new PresenceSyncS2C(list);
    }

    public static void handle(PresenceSyncS2C m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PresenceClientCache.set(m.entries)));
        ctx.get().setPacketHandled(true);
    }
}
