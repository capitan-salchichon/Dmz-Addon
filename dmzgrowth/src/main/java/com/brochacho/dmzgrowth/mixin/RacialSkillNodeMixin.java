package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.AcquiredRegenNode;
import com.brochacho.dmzgrowth.client.ClientGrowthCache;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.SelectRacialChoiceC2S;
import com.dragonminez.client.gui.radial.RadialNode;
import com.dragonminez.client.gui.radial.nodes.RacialSkillNode;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RacialSkillNode.class, remap = false)
public abstract class RacialSkillNodeMixin {
    /**
     * Si venias con la regeneracion elegida, pulsar la accion racial normal (asimilacion, vaciado vital...) vuelve a ella y la
     * deja SELECCIONADA (sin alternar la accion, que la apagaria).
     */
    @Inject(method = "onSelect", at = @At("HEAD"), cancellable = true)
    private void dmzgrowth$backToNormal(StatsData stats, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (ClientGrowthCache.choice(player).isEmpty()) return;
        GrowthNetwork.CHANNEL.sendToServer(new SelectRacialChoiceC2S(""));
        ClientGrowthCache.setChoiceLocal(player, "");
        if (stats.getStatus().getSelectedAction() != ActionMode.RACIAL)
            NetworkHandler.sendToServer(new SwitchActionC2S(ActionMode.RACIAL));
        ci.cancel();
    }

    /** Con la regeneracion elegida, la accion racial normal NO se ve activa. */
    @Inject(method = "active", at = @At("RETURN"), cancellable = true)
    private void dmzgrowth$notActiveWhenRegen(StatsData stats, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && "regeneration".equals(ClientGrowthCache.choice(Minecraft.getInstance().player)))
            cir.setReturnValue(false);
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
