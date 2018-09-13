package com.shinoow.hungeringdarkness.common.cap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class DarknessTimerCapabilityStorage implements IStorage<IDarknessTimerCapability> {

	public static IStorage<IDarknessTimerCapability> instance = new DarknessTimerCapabilityStorage();

	@Override
	public NBTBase writeNBT(Capability<IDarknessTimerCapability> capability, IDarknessTimerCapability instance, EnumFacing side) {
		NBTTagCompound properties = new NBTTagCompound();

		properties.setInteger("DarknessTimer", instance.getTimer());

		return properties;
	}

	@Override
	public void readNBT(Capability<IDarknessTimerCapability> capability, IDarknessTimerCapability instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound properties = (NBTTagCompound)nbt;

		instance.setTimer(properties.getInteger("DarknessTimer"));
	}
}