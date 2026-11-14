package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.ClientGrowthCache;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.SelectRacialChoiceC2S;
import com.dragonminez.client.gui.radial.nodes.NamekRegenNode;
import com.dragonminez.common.network.C2S.SwitchActionC2S;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.extras.ActionMode;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NamekRegenNode.class, remap = false)
public abstract class NamekRegenNodeMixin {
    /** Seleccionar la regeneracion ya no la dispara al instante: queda elegida y se usa con la tecla de accion (como la asimilacion). */
    @Inject(method = "onSelect", at = @At("HEAD"), cancellable = true)
    private void dmzgrowth$select(StatsData stats, CallbackInfo ci) {
        if (stats.getStatus().getSelectedAction() != ActionMode.RACIAL)
            NetworkHandler.sendToServer(new SwitchActionC2S(ActionMode.RACIAL));
        GrowthNetwork.CHANNEL.sendToServer(new SelectRacialChoiceC2S("regeneration"));
        ClientGrowthCache.setChoiceLocal(Minecraft.getInstance().player, "regeneration");
        ci.cancel();
    }

    /** Se ve activa tanto mientras cura como mientras esta elegida. */
    @Inject(method = "active", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$active(StatsData stats, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (stats.getStatus().getSelectedAction() == ActionMode.RACIAL
                && "regeneration".equals(ClientGrowthCache.choice(Minecraft.getInstance().player))) cir.setReturnValue(true);
    }
}
