package com.brochacho.dmzgrowth.net;

import com.brochacho.dmzgrowth.DMZGrowth;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class GrowthNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(DMZGrowth.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private GrowthNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, GrowthSyncS2C.class, GrowthSyncS2C::encode, GrowthSyncS2C::decode, GrowthSyncS2C::handle);
        CHANNEL.registerMessage(2, UseAbilityC2S.class, UseAbilityC2S::encode, UseAbilityC2S::decode, UseAbilityC2S::handle);
        CHANNEL.registerMessage(3, AssimilationSequenceS2C.class, AssimilationSequenceS2C::encode, AssimilationSequenceS2C::decode, AssimilationSequenceS2C::handle);
        CHANNEL.registerMessage(1, PresenceSyncS2C.class, PresenceSyncS2C::encode, PresenceSyncS2C::decode, PresenceSyncS2C::handle);
    }

    public static void sync(ServerPlayer player) {
        player.getCapability(GrowthCapability.INSTANCE).ifPresent(d ->
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GrowthSyncS2C(d)));
    }
}
