package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.BasePower;
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
    private static final ThreadLocal<Double> BEFORE = new ThreadLocal<>();

    @Inject(method = "increaseStat", at = @At("HEAD"))
    private static void dmzgrowth$before(StatsData data, ServerPlayer player, String statName, int amount, CallbackInfo ci) {
        BEFORE.set(BasePower.of(data));
    }

    /** Despues de subir la stat: se descuenta del Potencial el PODER que realmente se gano. */
    @Inject(method = "increaseStat", at = @At("RETURN"))
    private static void dmzgrowth$after(StatsData data, ServerPlayer player, String statName, int amount, CallbackInfo ci) {
        Double before = BEFORE.get();
        BEFORE.remove();
        if (before == null) return;
        double gained = BasePower.of(data) - before;
        if (gained <= 0) return;
        player.getCapability(GrowthCapability.INSTANCE).ifPresent(g -> {
            g.consumePower(gained);
            GrowthNetwork.sync(player);
        });
    }
}
