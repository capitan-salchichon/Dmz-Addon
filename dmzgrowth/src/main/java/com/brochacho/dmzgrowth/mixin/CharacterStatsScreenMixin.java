package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.KiAspects;
import com.brochacho.dmzgrowth.data.GrowthCapability;
import com.brochacho.dmzgrowth.data.GrowthData;
import com.dragonminez.client.gui.character.CharacterStatsScreen;
import com.dragonminez.client.util.NumberFormattingUtil;
import com.dragonminez.client.util.TextUtil;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Pantalla V: la vista del grafico con forma (hexagono) pasa a ser una lista de numeros con los aspectos del Ki:
 *   Ki de Combate, Ki Completo, Soporte (RES+VIT), Poder Real (= suma de los tres), Presencia (N%), Potencial, Limite
 * Mismo estilo y posicion que las filas de la vista lista de DMZ. El resto de la pantalla no se toca.
 */
@Mixin(value = CharacterStatsScreen.class, remap = false)
public abstract class CharacterStatsScreenMixin {
    @Shadow private StatsData statsData;
    private int getUiWidth() { return ((ScaledScreenInvoker) (Object) this).dmzgrowth$getUiWidth(); }
    private int getUiHeight() { return ((ScaledScreenInvoker) (Object) this).dmzgrowth$getUiHeight(); }

    private static final int LABEL = 0x7CFDD6, VALUE = 0xFFE593;

    @Inject(method = "renderStatisticsInfoHexagon", at = @At("HEAD"), cancellable = true)
    private void dmzgrowth$kiNumbers(GuiGraphics g, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
        if (statsData == null) return;
        Font font = Minecraft.getInstance().font;
        int centerY = getUiHeight() / 2;
        TextUtil.drawCenteredStringWithBorder(g, font, Component.translatable("gui.dragonminez.character_stats.statistics")
                .withStyle(style -> style.withBold(true)), getUiWidth() - 85, centerY - 88, 0xF91E64, 0x000000);

        boolean hide = statsData.getStatus().isAndroidUpgraded();
        KiAspects k = hide ? null : KiAspects.of(statsData);
        GrowthData gr = Minecraft.getInstance().player == null ? null
                : Minecraft.getInstance().player.getCapability(GrowthCapability.INSTANCE).resolve().orElse(null);
        boolean hasGrowth = gr != null && gr.isRolled();

        String[] labels = {"Ki de Combate", "Ki Completo", "Soporte (RES+VIT)", "Poder Real", "Presencia", "Potencial", "Limite"};
        double[] exact = {
                hide ? -1 : k.combatKi, hide ? -1 : k.fullKi, hide ? -1 : k.supportKi, hide ? -1 : k.realPower,
                hide ? -1 : k.presenceBp, hasGrowth ? gr.getPotential() : -1, hasGrowth ? gr.getLimit() : -1};
        String[] values = new String[7];
        for (int i = 0; i < 7; i++) values[i] = exact[i] < 0 ? (i >= 5 ? "---" : "???") : NumberFormattingUtil.formatLargeNumber(exact[i]);
        if (!hide) values[4] += " (" + k.presencePercent + "%)";

        int x = getUiWidth() - 137;
        int y0 = centerY - 64;
        for (int i = 0; i < 7; i++) {
            int y = y0 + i * 12;
            Component l = Component.literal(labels[i]), sep = Component.literal(": ");
            TextUtil.drawStringWithBorder(g, font, l, x, y, LABEL, 0x000000);
            int sx = x + font.width(l);
            TextUtil.drawStringWithBorder(g, font, sep, sx, y, LABEL, 0x000000);
            Component v = Component.literal(values[i]);
            TextUtil.drawStringWithBorder(g, font, v, sx + font.width(sep), y, VALUE, 0x000000);

            int w = font.width(l) + font.width(sep) + font.width(v);
            if (exact[i] >= 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + font.lineHeight) {
                TextUtil.renderAdvancedTooltip(g, font, mouseX, mouseY, getUiWidth(), getUiHeight(), null,
                        List.of(Component.literal(NumberFormattingUtil.formatNumber(exact[i])).withStyle(ChatFormatting.YELLOW)),
                        null, 0xFFFF00);
            }
        }
    }
}
