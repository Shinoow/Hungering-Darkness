package com.shinoow.hungeringdarkness.common.handlers;

import com.shinoow.hungeringdarkness.HungeringDarkness;
import com.shinoow.hungeringdarkness.common.cap.DarknessCapabilityProvider;
import com.shinoow.hungeringdarkness.common.cap.IDarknessTimerCapability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HungeringDarknessEventHandler {

	@SubscribeEvent
	public void darkness(LivingUpdateEvent event){
		if(event.getEntityLiving().world.isRemote) return;
		if(event.getEntityLiving() instanceof EntityPlayer && isWhitelisted(event.getEntityLiving().world.provider.getDimension())){
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			if(player.capabilities.isCreativeMode) return;
			if(player.isSpectator()) return;
			IDarknessTimerCapability cap = player.getCapability(DarknessCapabilityProvider.DARKNESS_TIMER, null);
			if(player.getEntityWorld().getLightFor(EnumSkyBlock.SKY, player.getPosition()) <= HungeringDarkness.light_level &&
					player.getEntityWorld().getLightFor(EnumSkyBlock.BLOCK, player.getPosition()) <= HungeringDarkness.light_level) {
				boolean totalDarkness = player.getEntityWorld().getLightFor(EnumSkyBlock.SKY, player.getPosition()) <= HungeringDarkness.total_darkness &&
						player.getEntityWorld().getLightFor(EnumSkyBlock.BLOCK, player.getPosition()) <= HungeringDarkness.total_darkness;
				if(cap.getTimer() < HungeringDarkness.delay * 20) {
					cap.incrementTimer();
					if(totalDarkness)
						cap.incrementTimer();
				}
				else if(player.ticksExisted % (HungeringDarkness.damageFrequency * 20) / (totalDarkness ? 2 : 1) == 0){
					player.attackEntityFrom(HungeringDarkness.darkness, HungeringDarkness.damage * (totalDarkness ? 2 : 1));
				}
			} else if(cap.getTimer() > 0)
				cap.decrementTimer();
		}
	}

	private boolean isWhitelisted(int dim){
		for(int id : HungeringDarkness.dimWhitelist)
			if(id == dim)
				return true;
		return false;
	}

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event){
		if(event.getObject() instanceof EntityPlayer)
			event.addCapability(new ResourceLocation("hungeringdarkness", "darknesstimer"), new DarknessCapabilityProvider());
	}

	@SubscribeEvent
	public void onClonePlayer(PlayerEvent.Clone event) {
		if(event.isWasDeath())
			event.getEntityPlayer().getCapability(DarknessCapabilityProvider.DARKNESS_TIMER, null).copy(event.getOriginal().getCapability(DarknessCapabilityProvider.DARKNESS_TIMER, null));
	}
}
