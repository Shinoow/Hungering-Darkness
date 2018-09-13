package com.shinoow.hungeringdarkness.common.cap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class DarknessCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTBase> {

	@CapabilityInject(IDarknessTimerCapability.class)
	public static final Capability<IDarknessTimerCapability> DARKNESS_TIMER = null;

	private IDarknessTimerCapability capability;

	public DarknessCapabilityProvider(){
		capability = new DarknessTimerCapability();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

		return capability == DARKNESS_TIMER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

		if(capability == DARKNESS_TIMER)
			return (T) this.capability;

		return null;
	}

	@Override
	public NBTBase serializeNBT() {
		return DarknessTimerCapabilityStorage.instance.writeNBT(DARKNESS_TIMER, capability, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		DarknessTimerCapabilityStorage.instance.readNBT(DARKNESS_TIMER, capability, null, nbt);
	}
}