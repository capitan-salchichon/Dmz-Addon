package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.client.AssimilationSequenceClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AssimilationSequenceS2C {
    private final int targetId, assimilatorId;

    public AssimilationSequenceS2C(int targetId, int assimilatorId) { this.targetId = targetId; this.assimilatorId = assimilatorId; }

    public static void encode(AssimilationSequenceS2C m, FriendlyByteBuf b) { b.writeInt(m.targetId); b.writeInt(m.assimilatorId); }
    public static AssimilationSequenceS2C decode(FriendlyByteBuf b) { return new AssimilationSequenceS2C(b.readInt(), b.readInt()); }

    public static void handle(AssimilationSequenceS2C m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> AssimilationSequenceClient.start(m.targetId, m.assimilatorId)));
        ctx.get().setPacketHandled(true);
    }
}
