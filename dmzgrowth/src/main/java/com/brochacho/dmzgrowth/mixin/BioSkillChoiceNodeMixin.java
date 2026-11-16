package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.ClientGrowthCache;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.SelectRacialChoiceC2S;
import com.dragonminez.client.gui.radial.nodes.BioSkillChoiceNode;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vaciado vital / autodestruccion del Bio-Android: elegir una quita la eleccion de regeneracion y no se ven activas con ella. */
@Mixin(value = BioSkillChoiceNode.class, remap = false)
public abstract class BioSkillChoiceNodeMixin {
    @Inject(method = "onSelect", at = @At("HEAD"))
    private void dmzgrowth$resetChoice(StatsData stats, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (!ClientGrowthCache.choice(player).isEmpty()) {
            GrowthNetwork.CHANNEL.sendToServer(new SelectRacialChoiceC2S(""));
            ClientGrowthCache.setChoiceLocal(player, "");
        }
    }

    @Inject(method = "active", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$notActiveWhenRegen(StatsData stats, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && "regeneration".equals(ClientGrowthCache.choice(Minecraft.getInstance().player)))
            cir.setReturnValue(false);
    }
}
