package com.brochacho.dmzgrowth.namek;

import com.brochacho.dmzgrowth.GrowthConfig;
import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.GeneralServerConfig;
import com.dragonminez.common.init.MainSounds;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.racial.RacialData;
import com.dragonminez.common.racial.RacialStatUtil;
import com.dragonminez.common.racial.capture.RacialCapture;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

/**
 * Asimilacion de un JUGADOR (ya aceptada): el objetivo NO muere; su personaje se reinicia desde 0 con la misma
 * funcion que usa Dende (StatsData.resetPlayerProgress) y sus stats de pelea se SUMAN al namekiano como bonos
 * (mismo mecanismo y mismo limite de espacios que la asimilacion de DMZ: Assimilation_n, se pierde el ultimo al morir).
 */
public final class PlayerAssimilation {
    private PlayerAssimilation() {}

    public static void apply(ServerPlayer namek, StatsData namekData, ServerPlayer target) {
        StatsData targetData = target.getCapability(StatsCapability.INSTANCE).resolve().orElse(null);
        if (targetData == null) return;
        GeneralServerConfig.NamekianRacialConfig cfg = ConfigManager.getServerConfig().getRacialSkills().getNamekian();
        int maxBonus = ConfigManager.getServerConfig().getGameplay().getMaxValue();
        double ratio = GrowthConfig.NAMEK_ASSIMILATION_RATIO.get();

        // 1) Sumar los poderes de pelea del objetivo (valores ANTES de reiniciarlo).
        String bonusName = "Assimilation_" + (namekData.getRacialData().getAssimilations().size() + 1);
        Map<String, Integer> granted = new HashMap<>();
        for (String stat : GrowthConfig.NAMEK_ASSIMILATION_STATS.get().split(",")) {
            String key = stat.trim().toUpperCase();
            if (key.isEmpty()) continue;
            int bonus = (int) Math.max(0, Math.min(maxBonus, RacialStatUtil.getStat(targetData, key) * ratio));
            if (bonus <= 0) continue;
            namekData.getBonusStats().addBonusSplit(key, bonusName, "+", bonus, true);
            granted.put(key, bonus);
        }
        namekData.getRacialData().addOwnedBonusName(bonusName);
        namekData.getRacialData().addAssimilation(new RacialData.AssimilationSlot(
                bonusName, target.getUUID(), target.getGameProfile().getName(), granted, namek.level().getGameTime()));

        // 2) Reiniciar al objetivo como lo hace Dende (sin matarlo).
        targetData.resetPlayerProgress(target, null, false, true);
        sendToSpawn(target); // el reinicio de DMZ no mueve al jugador: aqui se le envia a su punto de aparicion

        // 3) Efectos, curacion y sincronizacion.
        namek.level().playSound(null, namek.getX(), namek.getY(), namek.getZ(), MainSounds.STACK_FORM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        RacialCapture.heal(namek, cfg.getAssimilationHealthRegen());
        NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(namek), namek);
        NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(target), target);
    }

    /** Cama o ancla de reaparicion si tiene una valida; si no, el punto de aparicion del mundo. Cruza dimensiones si hace falta. */
    private static void sendToSpawn(ServerPlayer target) {
        var server = target.getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(target.getRespawnDimension());
        BlockPos spawn = target.getRespawnPosition();
        float angle = target.getRespawnAngle();
        if (level != null && spawn != null) {
            // false = no consumir carga de ancla de reaparicion
            Optional<Vec3> pos = Player.findRespawnPositionAndUseSpawnBlock(level, spawn, angle, target.isRespawnForced(), false);
            if (pos.isPresent()) {
                target.teleportTo(level, pos.get().x, pos.get().y, pos.get().z, angle, 0.0F);
                return;
            }
        }
        ServerLevel world = server.overworld();
        BlockPos top = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, world.getSharedSpawnPos());
        target.teleportTo(world, top.getX() + 0.5, top.getY(), top.getZ() + 0.5, world.getSharedSpawnAngle(), 0.0F);
    }
}
