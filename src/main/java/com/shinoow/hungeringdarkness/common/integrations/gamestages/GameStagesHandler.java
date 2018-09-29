package com.shinoow.hungeringdarkness.common.integrations.gamestages;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.shinoow.hungeringdarkness.HungeringDarkness;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;

public class GameStagesHandler {

	private static final Map<String, Integer> DARKNESS_HURTS = Maps.newHashMap();
	private static final Map<String, Integer> DARKNESS_DOESNT_HURT = Maps.newHashMap();

	public static void init() {
		for(String str : HungeringDarkness.hurt_stages) {
			String[] stuff = str.split(":");
			DARKNESS_HURTS.put(stuff[0], Integer.valueOf(stuff[1]));
		}
		for(String str : HungeringDarkness.nohurt_stages) {
			String[] stuff = str.split(":");
			DARKNESS_DOESNT_HURT.put(stuff[0], Integer.valueOf(stuff[1]));
		}
	}

	public static boolean shouldDarknessHurt(EntityPlayer player) {
		if(!Loader.isModLoaded("gamestages")) return true;

		int hurt_priority = 0;
		int doesnthurt_priority = 0;
		Collection<String> stages = GameStageHelper.getPlayerData(player).getStages();
		for(String stage : stages) {
			Integer hurt_value = DARKNESS_HURTS.get(stage);
			if(hurt_value != null && hurt_value > hurt_priority)
				hurt_priority = hurt_value;

			Integer doesnthurt_value = DARKNESS_DOESNT_HURT.get(stage);
			if(doesnthurt_value != null && doesnthurt_value > doesnthurt_priority)
				doesnthurt_priority = doesnthurt_value;
		}

		return hurt_priority > doesnthurt_priority || hurt_priority == 0 && doesnthurt_priority == 0;
	}
}
