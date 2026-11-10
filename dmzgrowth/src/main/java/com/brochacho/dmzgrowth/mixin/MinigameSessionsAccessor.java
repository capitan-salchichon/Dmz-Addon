package com.brochacho.dmzgrowth.mixin;

import com.dragonminez.common.training.MinigameSessionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/** DMZ no expone si un jugador esta en minijuego; SESSIONS es privado, asi que se lee con un accessor. */
@Mixin(value = MinigameSessionManager.class, remap = false)
public interface MinigameSessionsAccessor {
    @Accessor(value = "SESSIONS", remap = false)
    static Map<UUID, ?> dmzgrowth$getSessions() {
        throw new AssertionError();
    }
}
