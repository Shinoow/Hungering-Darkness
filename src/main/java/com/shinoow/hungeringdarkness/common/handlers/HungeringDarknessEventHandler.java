package com.shinoow.hungeringdarkness.common.handlers;

import com.shinoow.hungeringdarkness.HungeringDarkness;
import com.shinoow.hungeringdarkness.common.cap.DarknessCapabilityProvider;
import com.shinoow.hungeringdarkness.common.cap.IDarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.integrations.gamestages.GameStagesHandler;
import com.shinoow.hungeringdarkness.common.network.PacketDispatcher;
import com.shinoow.hungeringdarkness.common.network.server.DynamicLightsMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

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
					player.getEntityWorld().getLightFor(EnumSkyBlock.BLOCK, player.getPosition()) <= HungeringDarkness.light_level &&
					dynamicLightsCheck(player, cap) && GameStagesHandler.shouldDarknessHurt(player)) {
				boolean totalDarkness = player.getEntityWorld().getLightFor(EnumSkyBlock.SKY, player.getPosition()) <= HungeringDarkness.total_darkness &&
						player.getEntityWorld().getLightFor(EnumSkyBlock.BLOCK, player.getPosition()) <= HungeringDarkness.total_darkness;
				if(cap.getTimer() < HungeringDarkness.delay * 20) {
					cap.incrementTimer();
					if(totalDarkness)
						cap.incrementTimer();
				}
				else if(player.ticksExisted % (HungeringDarkness.damageFrequency * 20) / (totalDarkness ? 2 : 1) == 0)
					player.attackEntityFrom(HungeringDarkness.darkness, HungeringDarkness.damage * (totalDarkness ? 2 : 1));
			} else if(cap.getTimer() > 0)
				cap.decrementTimer();
		}
	}

	private boolean isWhitelisted(int dim){
		if(!HungeringDarkness.useBlacklist) {
			for(int id : HungeringDarkness.dimWhitelist)
				if(id == dim)
					return true;
			return false;
		} else {
			for(int id : HungeringDarkness.dimWhitelist)
				if(id == dim)
					return false;
			return true;
		}
	}

	private boolean dynamicLightsCheck(EntityPlayer player, IDarknessTimerCapability cap) {
		if(HungeringDarkness.dynamicLightsMode && cap.hasDynamicLights())
			return isNotLightSource(player.getHeldItemMainhand())  && isNotLightSource(player.getHeldItemOffhand());
		return true;
	}

	private boolean isNotLightSource(ItemStack stack){
		if(stack.isEmpty()) return true;
		for(ItemStack stack1 : HungeringDarkness.dynamic_lights_list)
			if(stack1.getItem() == stack.getItem() && (stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE
			|| stack1.getItemDamage() == stack.getItemDamage()))
				return false;
		return true;
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

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && event.getWorld().isRemote){
			if(!HungeringDarkness.dynamicLightsMode) return;
			PacketDispatcher.sendToServer(new DynamicLightsMessage(Loader.isModLoaded("dynamiclights")));
		}
	}
}
