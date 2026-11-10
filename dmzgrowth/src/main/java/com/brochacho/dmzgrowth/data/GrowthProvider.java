package com.brochacho.dmzgrowth.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrowthProvider implements ICapabilitySerializable<CompoundTag> {
    private final GrowthData data = new GrowthData();
    private final LazyOptional<GrowthData> optional = LazyOptional.of(() -> data);

    @NotNull @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == GrowthCapability.INSTANCE ? optional.cast() : LazyOptional.empty();
    }

    @Override public CompoundTag serializeNBT() { return data.save(); }
    @Override public void deserializeNBT(CompoundTag nbt) { data.load(nbt); }
    public void invalidate() { optional.invalidate(); }
}
