package com.brochacho.dmzgrowth.client;

import com.dragonminez.client.gui.radial.AbstractRadialNode;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Nodo del menu X junto al de Liberacion: muestra "Presencia: XX%" (poder manifestado ahora). Solo lectura. */
public class PresenceNode extends AbstractRadialNode {
    @Override public Component label(StatsData stats) { return Component.literal("Presencia"); }
    @Override public ResourceLocation icon(StatsData stats) { return null; }
    @Override public String faceText(StatsData stats) { return stats.getResources().getPowerRelease() + "%"; }
    @Override public boolean visible(StatsData stats) {
        return stats.getCharacter() != null && stats.getCharacter().getRaceName() != null && !stats.getCharacter().getRaceName().isEmpty();
    }
    @Override public boolean interactive(StatsData stats) { return false; }
    @Override public boolean expandable(StatsData stats) { return false; }
}
