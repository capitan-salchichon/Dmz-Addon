package com.brochacho.dmzgrowth.data;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class GrowthCapability {
    public static final Capability<GrowthData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    private GrowthCapability() {}
}
