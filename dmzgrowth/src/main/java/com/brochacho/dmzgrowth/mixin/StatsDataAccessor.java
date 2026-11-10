package com.brochacho.dmzgrowth.mixin;

import com.dragonminez.common.stats.StatsData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StatsData.class, remap = false)
public interface StatsDataAccessor {
    @Accessor(value = "player", remap = false)
    Player dmzgrowth$getPlayer();
}
