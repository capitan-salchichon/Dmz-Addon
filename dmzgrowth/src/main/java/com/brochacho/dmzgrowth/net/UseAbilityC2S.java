package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.ability.AbilityRegistry;
import com.dragonminez.common.stats.StatsCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseAbilityC2S {
    private final String id;

    public UseAbilityC2S(String id) { this.id = id; }

    public static void encode(UseAbilityC2S m, FriendlyByteBuf b) { b.writeUtf(m.id, 64); }
    public static UseAbilityC2S decode(FriendlyByteBuf b) { return new UseAbilityC2S(b.readUtf(64)); }

    public static void handle(UseAbilityC2S m, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var player = c.getSender();
            if (player == null) return;
            player.getCapability(StatsCapability.INSTANCE).ifPresent(data -> AbilityRegistry.use(player, data, m.id));
        });
        c.setPacketHandled(true);
    }
}
