package com.shinoow.hungeringdarkness.common.util;

import org.apache.logging.log4j.Level;

import com.shinoow.hungeringdarkness.HungeringDarkness;

import net.minecraft.util.math.MathHelper;

public class DimensionData {
	private int damageFrequency, damage, delay, light_level, total_darkness, height;

	public DimensionData() {}

	public DimensionData(String...data) {
		damageFrequency = parseOrDef(data[1], HungeringDarkness.damageFrequency, 1, 10);
		damage = parseOrDef(data[2], HungeringDarkness.damage, 2, 20);
		delay = parseOrDef(data[3], HungeringDarkness.delay, 1, 60);
		light_level = parseOrDef(data[4], HungeringDarkness.light_level, 0, 15);
		total_darkness = parseOrDef(data[5], HungeringDarkness.total_darkness, -1, 15);
		height = parseOrDef(data[6], HungeringDarkness.height, 0, 256);
	}

	private int parseOrDef(String str, int def, int min, int max) {
		if(str.equalsIgnoreCase("def"))
			return def;
		int i;
		try {
			i = Integer.parseInt(str);
		} catch(Exception e) {
			HungeringDarkness.LOGGER.log(Level.ERROR, "Failed to parse integer {}, using default instead", str);
			i = def;
		}
		return MathHelper.clamp(i, min, max);
	}

	public int getDamageFrequency() {
		return damageFrequency;
	}

	public int getDamage() {
		return damage;
	}

	public int getDelay() {
		return delay;
	}

	public int getLightLevel() {
		return light_level;
	}

	public int getTotalDarkness() {
		return total_darkness;
	}

	public int getHeight() {
		return height;
	}
}
