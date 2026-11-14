package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.bioandroid.AcquiredRegeneration;
import com.brochacho.dmzgrowth.net.GrowthNetwork;
import com.brochacho.dmzgrowth.net.SelectRacialChoiceC2S;
import com.dragonminez.client.gui.radial.AbstractRadialNode;
import com.dragonminez.common.network.C2S.SwitchActionC2S;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.extras.ActionMode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Nodo del menu X del Bio-Android: aparece cuando ya absorbio a un namekiano; se selecciona y se usa con la tecla de accion. */
public class AcquiredRegenNode extends AbstractRadialNode {
    @Override public Component label(StatsData stats) { return Component.literal("Regeneracion"); }
    @Override public ResourceLocation icon(StatsData stats) { return icon("regen"); }

    @Override
    public boolean visible(StatsData stats) {
        var g = ClientGrowthCache.ensure(Minecraft.getInstance().player);
        return "bioandroid".equals(stats.getCharacter().getRaceName()) && g != null && g.hasAcquired(AcquiredRegeneration.KEY);
    }

    @Override
    public boolean active(StatsData stats) {
        return stats.getStatus().getSelectedAction() == ActionMode.RACIAL
                && "regeneration".equals(ClientGrowthCache.choice(Minecraft.getInstance().player));
    }

    @Override public int labelColor(StatsData stats) { return active(stats) ? GREEN : 0xFFFFFF; }

    @Override
    public void onSelect(StatsData stats) {
        select(stats);
    }

    static void select(StatsData stats) {
        if (stats.getStatus().getSelectedAction() != ActionMode.RACIAL)
            NetworkHandler.sendToServer(new SwitchActionC2S(ActionMode.RACIAL));
        GrowthNetwork.CHANNEL.sendToServer(new SelectRacialChoiceC2S("regeneration"));
        ClientGrowthCache.setChoiceLocal(Minecraft.getInstance().player, "regeneration");
    }
}
