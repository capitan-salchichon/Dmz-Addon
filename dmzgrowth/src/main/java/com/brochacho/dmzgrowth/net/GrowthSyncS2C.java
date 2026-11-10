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

    public GrowthSyncS2C(GrowthData data) { this.tag = data.save(); }
    private GrowthSyncS2C(CompoundTag tag) { this.tag = tag; }

    public static void encode(GrowthSyncS2C m, FriendlyByteBuf b) { b.writeNbt(m.tag); }
    public static GrowthSyncS2C decode(FriendlyByteBuf b) { return new GrowthSyncS2C(b.readNbt()); }

    public static void handle(GrowthSyncS2C m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var player = Minecraft.getInstance().player;
            if (player != null && m.tag != null)
                player.getCapability(GrowthCapability.INSTANCE).ifPresent(d -> d.load(m.tag));
        }));
        ctx.get().setPacketHandled(true);
    }
}
