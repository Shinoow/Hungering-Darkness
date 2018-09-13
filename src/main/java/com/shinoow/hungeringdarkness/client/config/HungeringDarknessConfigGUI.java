package com.shinoow.hungeringdarkness.client.config;

import com.shinoow.hungeringdarkness.HungeringDarkness;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class HungeringDarknessConfigGUI extends GuiConfig {

	public HungeringDarknessConfigGUI(GuiScreen parent) {
		super(parent, new ConfigElement(HungeringDarkness.cfg.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), "hungeringdarkness", false, false, "Hungering Darkness");
	}
}