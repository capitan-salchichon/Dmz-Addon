package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.PresenceNode;
import com.dragonminez.client.gui.radial.RadialNode;
import com.dragonminez.client.gui.radial.nodes.ActionsNode;
import com.dragonminez.client.gui.radial.nodes.ReleaseNode;
import com.dragonminez.common.stats.StatsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ActionsNode.class, remap = false)
public abstract class ActionsNodeMixin {
    /** Menu X: anade "Presencia: XX%" justo despues del nodo de Liberacion existente. */
    @Inject(method = "buildChildren", at = @At("RETURN"))
    private void dmzgrowth$addPresence(StatsData stats, CallbackInfoReturnable<List<RadialNode>> cir) {
        List<RadialNode> list = cir.getReturnValue();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof ReleaseNode) { list.add(i + 1, new PresenceNode()); return; }
        }
        list.add(new PresenceNode());
    }
}
