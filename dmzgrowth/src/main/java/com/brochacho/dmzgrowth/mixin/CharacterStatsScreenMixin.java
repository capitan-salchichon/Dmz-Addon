package com.brochacho.dmzgrowth.mixin;

import com.brochacho.dmzgrowth.client.ClientGrowthCache;
import com.brochacho.dmzgrowth.client.KiAspects;
import com.dragonminez.client.render.shader.ClientGravityState;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
        GrowthData gr = ClientGrowthCache.ensure(Minecraft.getInstance().player);
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
        dmzgrowth$extremeBar(g, font, getUiWidth() - 137, centerY + 30, mouseX, mouseY);
    }

    /** Vista de lista: la barra del Entrenamiento Extremo en el hueco libre bajo la fila de velocidad. */
    @Inject(method = "renderStatisticsInfoList", at = @At("TAIL"))
    private void dmzgrowth$barInList(GuiGraphics g, int mouseX, int mouseY, CallbackInfo ci) {
        dmzgrowth$extremeBar(g, Minecraft.getInstance().font, getUiWidth() - 137, getUiHeight() / 2 + 30, mouseX, mouseY);
    }

    /** Barra de progreso: lo que falta para completar el Entrenamiento Extremo (sube Limite y Potencial). */
    private void dmzgrowth$extremeBar(GuiGraphics g, Font font, int x, int y, int mouseX, int mouseY) {
        GrowthData gd = ClientGrowthCache.ensure(Minecraft.getInstance().player);
        if (gd == null || !gd.isRolled()) return;
        int required = Math.max(1, ClientGrowthCache.secondsRequired);
        int done = Math.min(gd.getExtremeSeconds(), required);
        float frac = done / (float) required;

        TextUtil.drawStringWithBorder(g, font, Component.literal("Entrenamiento Extremo: " + Math.round(frac * 100) + "%"), x, y, 0xC77DFF, 0x000000);
        int bw = 110, by = y + 11;
        g.fill(x - 1, by - 1, x + bw + 1, by + 7, 0xFF000000);
        g.fill(x, by, x + bw, by + 6, 0xFF2B2B2B);
        g.fill(x, by, x + Math.round(bw * frac), by + 6, 0xFFB16CFF);

        if (mouseX >= x && mouseX <= x + bw && mouseY >= y && mouseY <= by + 7) {
            List<Component> lines = new java.util.ArrayList<>();
            lines.add(Component.literal(done + " / " + required + " s").withStyle(ChatFormatting.YELLOW));
            lines.add(Component.literal("Al completarlo se sube el Limite y el Potencial.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Cuenta en un minijuego de TPs con carga entre " + fmt(ClientGrowthCache.factor)
                    + "x y " + fmt(ClientGrowthCache.overload) + "x tu peso ideal.").withStyle(ChatFormatting.GRAY));
            TextUtil.renderAdvancedTooltip(g, font, mouseX, mouseY, getUiWidth(), getUiHeight(),
                    Component.literal("Entrenamiento Extremo").withStyle(ChatFormatting.LIGHT_PURPLE), lines, null, 0xC77DFF);
        }
    }

    private static String fmt(double v) { return v == Math.rint(v) ? String.valueOf((long) v) : String.format(java.util.Locale.ROOT, "%.1f", v); }

    /** Tooltip de la gravedad (donde DMZ muestra el peso ideal): se anade el peso del Entrenamiento Extremo y el progreso. */
    @ModifyArg(method = "renderGravityInfo",
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/dragonminez/client/util/TextUtil;renderAdvancedTooltip(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/network/chat/Component;Ljava/util/List;Ljava/util/List;I)V"),
            index = 8)
    private List<Component> dmzgrowth$extremeWeightTooltip(List<Component> extras) {
        int ideal = ClientGravityState.getIdealWeight();
        if (ideal > 0) {
            extras.add(Component.literal("Entrenamiento Extremo: " + NumberFormattingUtil.formatNumber(Math.round(ideal * ClientGrowthCache.factor))
                    + " - " + NumberFormattingUtil.formatNumber(Math.round(ideal * ClientGrowthCache.overload))).withStyle(ChatFormatting.LIGHT_PURPLE));
            GrowthData gd = ClientGrowthCache.ensure(Minecraft.getInstance().player);
            if (gd != null && gd.isRolled())
                extras.add(Component.literal("Progreso: " + gd.getExtremeSeconds() + " / " + ClientGrowthCache.secondsRequired + " s").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return extras;
    }
}
