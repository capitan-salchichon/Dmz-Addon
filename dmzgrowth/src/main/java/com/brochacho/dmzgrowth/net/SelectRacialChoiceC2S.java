package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.racial.RacialChoice;
import com.dragonminez.common.stats.StatsCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectRacialChoiceC2S {
    private final String choice;

    public SelectRacialChoiceC2S(String choice) { this.choice = choice; }

    public static void encode(SelectRacialChoiceC2S m, FriendlyByteBuf b) { b.writeUtf(m.choice, 32); }
    public static SelectRacialChoiceC2S decode(FriendlyByteBuf b) { return new SelectRacialChoiceC2S(b.readUtf(32)); }

    /** El servidor valida: solo "" o "regeneration", y esta ultima solo si el jugador puede usarla. */
    public static void handle(SelectRacialChoiceC2S m, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var player = c.getSender();
            if (player == null) return;
            player.getCapability(StatsCapability.INSTANCE).ifPresent(data ->
                    player.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
                        if (m.choice.isEmpty()) g.setRacialChoice("");
                        else if (RacialChoice.REGENERATION.equals(m.choice) && RacialChoice.regenEligible(data, g)) g.setRacialChoice(m.choice);
                        else return;
                        GrowthNetwork.sync(player);
                    }));
        });
        c.setPacketHandled(true);
    }
}
