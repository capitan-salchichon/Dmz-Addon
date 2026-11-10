package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.DMZGrowth;
import com.dragonminez.common.util.CuriosUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Lineas de texto con las presencias lejanas detectadas. Solo se dibuja con scouter puesto. */
@Mod.EventBusSubscriber(modid = DMZGrowth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PresenceOverlay {
    private PresenceOverlay() {}

    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("presence", (gui, g, partialTick, w, h) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.renderDebug) return;
            if (CuriosUtil.getFirstStackForItem(mc.player, "head_tech", "scouter").isEmpty()) return;
            var list = PresenceClientCache.get();
            int y = 6;
            for (var en : list) {
                double dist = Math.sqrt(mc.player.distanceToSqr(en.x(), en.y(), en.z()));
                String line = String.format("%s  %,.0f BP  %d%%  %.0f m  (%.0f, %.0f, %.0f)",
                        en.name(), en.presenceBp(), en.releasePercent(), dist, en.x(), en.y(), en.z());
                g.drawString(mc.font, line, 6, y, 0x55FF55, true);
                y += 10;
            }
        });
    }
}
