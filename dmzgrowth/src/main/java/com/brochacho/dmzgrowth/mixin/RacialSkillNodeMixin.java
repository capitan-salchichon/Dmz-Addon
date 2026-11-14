package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.AcquiredRegenNode;
import com.brochacho.dmzgrowth.client.ClientGrowthCache;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.SelectRacialChoiceC2S;
import com.dragonminez.client.gui.radial.RadialNode;
import com.dragonminez.client.gui.radial.nodes.RacialSkillNode;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RacialSkillNode.class, remap = false)
public abstract class RacialSkillNodeMixin {
    /** Elegir la accion racial normal (asimilacion, vaciado vital...) quita la eleccion de regeneracion. */
    @Inject(method = "onSelect", at = @At("HEAD"))
    private void dmzgrowth$resetChoice(StatsData stats, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (!ClientGrowthCache.choice(player).isEmpty()) {
            GrowthNetwork.CHANNEL.sendToServer(new SelectRacialChoiceC2S(""));
            ClientGrowthCache.setChoiceLocal(player, "");
        }
    }

    /** Bio-Android: anade "Regeneracion" a las opciones de su accion racial (solo visible si ya absorbio a un namekiano). */
    @Inject(method = "buildChildren", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$addRegen(StatsData stats, CallbackInfoReturnable<List<RadialNode>> cir) {
        if (!"bioandroid".equals(stats.getCharacter().getRaceName())) return;
        List<RadialNode> list = new ArrayList<>(cir.getReturnValue());
        list.add(new AcquiredRegenNode());
        cir.setReturnValue(list);
    }
}
