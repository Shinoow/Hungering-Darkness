package com.shinoow.hungeringdarkness.common.network.server;

import java.io.IOException;

import com.shinoow.hungeringdarkness.common.cap.DarknessCapabilityProvider;
import com.shinoow.hungeringdarkness.common.cap.IDarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.network.AbstractMessage.AbstractServerMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class DynamicLightsMessage extends AbstractServerMessage<DynamicLightsMessage> {

	private boolean modPresent;

	public DynamicLightsMessage() {}

	public DynamicLightsMessage(boolean modPresent){
		this.modPresent = modPresent;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		modPresent = buffer.readBoolean();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeBoolean(modPresent);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		IDarknessTimerCapability capability = player.getCapability(DarknessCapabilityProvider.DARKNESS_TIMER, null);
		capability.setHasDynamicLights(modPresent);
	}
}