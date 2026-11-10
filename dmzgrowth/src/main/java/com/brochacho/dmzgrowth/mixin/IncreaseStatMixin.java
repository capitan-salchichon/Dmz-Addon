package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.dragonminez.common.network.C2S.IncreaseStatC2S;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IncreaseStatC2S.class, remap = false)
public abstract class IncreaseStatMixin {
    /** Antes de subir la stat: consume Potencial (el aumento ya viene limitado por StatsDataMixin). */
    @Inject(method = "increaseStat", at = @At("HEAD"))
    private static void dmzgrowth$consume(StatsData data, ServerPlayer player, String statName, int amount, CallbackInfo ci) {
        player.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
            g.consumeForPoints(amount);
            GrowthNetwork.sync(player);
        });
    }
}
