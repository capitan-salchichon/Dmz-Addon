package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GrowthSyncS2C {
    private final CompoundTag tag;
    private final double factor, overload;
    private final int seconds;
    private final boolean inSession;
    private final double ratio;
    private final double maxUnspent;

    public GrowthSyncS2C(GrowthData data) {
        this.tag = data.save();
        this.factor = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_WEIGHT_FACTOR.get();
        this.overload = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_OVERLOAD_RATIO.get();
        this.seconds = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_SECONDS_REQUIRED.get();
        this.inSession = data.isLiveInSession();
        this.ratio = data.getLiveRatio();
        this.maxUnspent = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_MAX_UNSPENT.get();
    }
    private GrowthSyncS2C(CompoundTag tag, double factor, double overload, int seconds, boolean inSession, double ratio, double maxUnspent) {
        this.tag = tag; this.factor = factor; this.overload = overload; this.seconds = seconds;
        this.inSession = inSession; this.ratio = ratio; this.maxUnspent = maxUnspent;
    }

    public static void encode(GrowthSyncS2C m, FriendlyByteBuf b) {
        b.writeNbt(m.tag);
        b.writeDouble(m.factor);
        b.writeDouble(m.overload);
        b.writeVarInt(m.seconds);
        b.writeBoolean(m.inSession);
        b.writeDouble(m.ratio);
        b.writeDouble(m.maxUnspent);
    }
    public static GrowthSyncS2C decode(FriendlyByteBuf b) { return new GrowthSyncS2C(b.readNbt(), b.readDouble(), b.readDouble(), b.readVarInt(), b.readBoolean(), b.readDouble(), b.readDouble()); }

    public static void handle(GrowthSyncS2C m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.brochacho.dmzgrowth.client.ClientSyncHandler.handle(m.tag, m.factor, m.overload, m.seconds, m.inSession, m.ratio, m.maxUnspent)));
        ctx.get().setPacketHandled(true);
    }
}
