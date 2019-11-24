package com.shinoow.hungeringdarkness.common.handlers;

import java.util.Arrays;

import com.shinoow.darknesslib.api.DarknessLibAPI;
import com.shinoow.hungeringdarkness.HungeringDarkness;
import com.shinoow.hungeringdarkness.common.cap.DarknessCapabilityProvider;
import com.shinoow.hungeringdarkness.common.cap.IDarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.integrations.gamestages.GameStagesHandler;
import com.shinoow.hungeringdarkness.common.util.DimensionData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HungeringDarknessEventHandler {

	@SubscribeEvent
	public void darkness(LivingUpdateEvent event){
		if(event.getEntityLiving().world.isRemote || event.getEntityLiving().isDead) return;
		if(event.getEntityLiving() instanceof EntityPlayer && isWhitelisted(event.getEntityLiving().world.provider.getDimension())
				&& isWhitelisted(event.getEntityLiving().world.getBiome(event.getEntityLiving().getPosition()))){
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			if(player.capabilities.isCreativeMode) return;
			if(player.isSpectator()) return;
			if(player.isRiding() && DarknessLibAPI.getInstance().isVehicle(player.getRidingEntity())) return;
			DimensionData data = HungeringDarkness.getDimensionConfig(player.dimension);
			if(player.posY >= data.getHeight()) return;
			IDarknessTimerCapability cap = player.getCapability(DarknessCapabilityProvider.DARKNESS_TIMER, null);
			if(player.isInWater() && player.getAir() == 300 && player.world.getBlockState(player.getPosition().up()) == Blocks.AIR.getDefaultState() || !player.isInWater()) {
				int light = HungeringDarkness.unrealisticLight ? DarknessLibAPI.getInstance().getLight(player, true) : DarknessLibAPI.getInstance().getLightWithDynLights(player, true);
				if(light <= data.getLightLevel() && dynamicLightsCheck(player) && GameStagesHandler.shouldDarknessHurt(player)) {
					boolean totalDarkness = light <= data.getTotalDarkness();
					if(cap.getTimer() < data.getDelay() * 20) {
						cap.incrementTimer();
						if(totalDarkness)
							cap.incrementTimer();
					}
					else if(player.ticksExisted % (data.getDamageFrequency() * 20) / (totalDarkness ? 2 : 1) == 0)
						player.attackEntityFrom(HungeringDarkness.darkness, data.getDamage() * (totalDarkness ? 2 : 1));
				} else if(cap.getTimer() > 0)
					cap.decrementTimer();
			}
		}
	}

	private boolean dynamicLightsCheck(EntityPlayer player) {
		return HungeringDarkness.unrealisticLight ? !DarknessLibAPI.getInstance().isIlluminatedDynamically(player) : !HungeringDarkness.unrealisticLight;
	}

	private boolean isWhitelisted(int dim){
		if(!HungeringDarkness.useBlacklist)
			return Arrays.stream(HungeringDarkness.dimWhitelist).anyMatch(id -> id == dim);
		else
			return Arrays.stream(HungeringDarkness.dimWhitelist).noneMatch(id -> id == dim);
	}

	private boolean isWhitelisted(Biome biome) {
		return HungeringDarkness.biome_whitelist.isEmpty() || HungeringDarkness.useBiomeBlacklist ? !HungeringDarkness.biome_whitelist.contains(biome) : HungeringDarkness.biome_whitelist.contains(biome);
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
