package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import net.minecraft.client.Minecraft;
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

    public GrowthSyncS2C(GrowthData data) {
        this.tag = data.save();
        this.factor = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_WEIGHT_FACTOR.get();
        this.overload = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_OVERLOAD_RATIO.get();
        this.seconds = com.brochacho.dmzgrowth.GrowthConfig.EXTREME_SECONDS_REQUIRED.get();
    }
    private GrowthSyncS2C(CompoundTag tag, double factor, double overload, int seconds) {
        this.tag = tag; this.factor = factor; this.overload = overload; this.seconds = seconds;
    }

    public static void encode(GrowthSyncS2C m, FriendlyByteBuf b) {
        b.writeNbt(m.tag);
        b.writeDouble(m.factor);
        b.writeDouble(m.overload);
        b.writeVarInt(m.seconds);
    }
    public static GrowthSyncS2C decode(FriendlyByteBuf b) { return new GrowthSyncS2C(b.readNbt(), b.readDouble(), b.readDouble(), b.readVarInt()); }

    public static void handle(GrowthSyncS2C m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (m.tag == null) return;
            com.brochacho.dmzgrowth.client.ClientGrowthCache.store(m.tag, m.factor, m.overload, m.seconds);
            com.brochacho.dmzgrowth.client.ClientGrowthCache.applyTo(Minecraft.getInstance().player);
        }));
        ctx.get().setPacketHandled(true);
    }
}
