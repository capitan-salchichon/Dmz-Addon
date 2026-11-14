package com.brochacho.dmzgrowth;

import com.dragonminez.common.config.FormConfig;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.extras.ActionMode;
import com.dragonminez.common.util.TransformationsHelper;

/**
 * Transformacion Normal progresiva: mientras DMZ carga la accion FORM (actionCharge 0..100) el multiplicador de la
 * forma interpola desde el actual hacia el de la forma objetivo. Al llegar a 100 DMZ aplica la forma real.
 * La Instant Transformation (ExecuteActionC2S) no carga, asi que no pasa por aqui.
 */
public final class ProgressiveTransformation {
    private ProgressiveTransformation() {}

    public static double adjust(StatsData data, String stat, double current) {
        if (BasePower.ignoringForms()) return 1.0; // el poder base (Limite/Potencial) no cuenta las formas
        if (!GrowthConfig.TRANSFORM_PROGRESSIVE.get()) return current;
        var status = data.getStatus();
        if (!status.isActionCharging() || status.getSelectedAction() != ActionMode.FORM) return current;

        int charge = data.getResources().getActionCharge();
        if (charge <= 0 || charge >= 100) return current;

        FormConfig.FormData next = TransformationsHelper.getNextAvailableForm(data);
        if (next == null) return current;

        double target = baseMultiplier(next, stat);
        if (target <= current) return current;

        double t = Math.pow(charge / 100.0, GrowthConfig.TRANSFORM_CURVE.get());
        return current + (target - current) * t;
    }

    // Misma correspondencia stat -> multiplicador que StatsData.getFormMultiplier (sin bonos de maestria).
    private static double baseMultiplier(FormConfig.FormData f, String stat) {
        return switch (stat.toUpperCase()) {
            case "STR" -> f.getStrMultiplier();
            case "SKP" -> f.getSkpMultiplier();
            case "STM" -> f.getStmMultiplier();
            case "DEF" -> f.getDefMultiplier();
            case "RES" -> (f.getDefMultiplier() + f.getStmMultiplier()) / 2.0;
            case "VIT" -> f.getVitMultiplier();
            case "PWR" -> f.getPwrMultiplier();
            case "ENE" -> f.getEneMultiplier();
            case "SPEED" -> f.getSpeedMultiplier();
            default -> 1.0;
        };
    }
}
