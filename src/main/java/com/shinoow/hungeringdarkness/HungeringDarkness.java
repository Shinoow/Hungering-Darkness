package com.shinoow.hungeringdarkness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.logging.log4j.Level;

import com.shinoow.hungeringdarkness.common.CommonProxy;
import com.shinoow.hungeringdarkness.common.cap.DarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.cap.DarknessTimerCapabilityStorage;
import com.shinoow.hungeringdarkness.common.cap.IDarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.handlers.HungeringDarknessEventHandler;

import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = HungeringDarkness.modid, name = HungeringDarkness.name, version = HungeringDarkness.version, dependencies = "required-after:forge@[forgeversion,)", acceptedMinecraftVersions = "[1.12.2]", guiFactory = "com.shinoow.hungeringdarkness.client.config.HungeringDarknessGuiFactory", updateJSON = "https://raw.githubusercontent.com/Shinoow/Hungering-Darkness/master/version.json", useMetadata = false, certificateFingerprint = "cert_fingerprint")
public class HungeringDarkness {

	public static final String version = "hd_version";
	public static final String modid = "hungeringdarkness";
	public static final String name = "Hungering Darkness";

	@Metadata(modid)
	public static ModMetadata metadata;

	@Instance(modid)
	public static HungeringDarkness instance;

	@SidedProxy(clientSide = "com.shinoow.hungeringdarkness.client.ClientProxy",
			serverSide = "com.shinoow.hungeringdarkness.common.CommonProxy")
	public static CommonProxy proxy;

	public static Configuration cfg;

	public static int damageFrequency, damage, delay, light_level, total_darkness;
	public static int[] dimWhitelist;

	public static DamageSource darkness = new DamageSource("darkness").setDamageBypassesArmor().setDamageIsAbsolute();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){

		metadata = event.getModMetadata();
		metadata.description = metadata.description +"\n\n\u00a76Supporters: "+getSupporterList()+"\u00a7r";
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new HungeringDarknessEventHandler());

		cfg = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();

		CapabilityManager.INSTANCE.register(IDarknessTimerCapability.class, DarknessTimerCapabilityStorage.instance, DarknessTimerCapability::new);

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		proxy.postInit();
	}

	@EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		FMLLog.log("Hungering Darkness", Level.WARN, "Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(modid))
			syncConfig();
	}

	private static void syncConfig(){

		cfg.setCategoryComment(Configuration.CATEGORY_GENERAL, "CONFIG ALL THE THINGS! Any changes take effect immediately (unless you're on a server, then the server has to restart).");

		dimWhitelist = cfg.get(Configuration.CATEGORY_GENERAL, "Dimension Whitelist", new int[]{0}, "Add dimension IDs to this list if you want the darkness to damage players here.").getIntList();
		damageFrequency = cfg.getInt("Damage Frequency", Configuration.CATEGORY_GENERAL, 5, 1, 10, "The amount of time (in seconds) between each hit when the darkness damages you.");
		damage = cfg.getInt("Damage Amount", Configuration.CATEGORY_GENERAL, 2, 2, 20, "The amount of damage (half hearts) you take from the darkness.");
		delay = cfg.getInt("Damage Delay", Configuration.CATEGORY_GENERAL, 10, 1, 60, "The amount of time (in seconds) you have to spend in darkness before you begin to receive damage.");
		light_level = cfg.getInt("Light Level", Configuration.CATEGORY_GENERAL, 4, 0, 15, "The light level where it's considered dark enough that you begin to receive damage.");
		total_darkness = cfg.getInt("Total Darkness Light Level", Configuration.CATEGORY_GENERAL, 0, -1, 15, "The light level that's considered total darkness. Being in this light level halves the time it takes before you start to receive damage, while also doubling the damage receieved. Disable by setting it to -1.");

		if(cfg.hasChanged())
			cfg.save();
	}

	private String getSupporterList(){
		BufferedReader nameFile;
		String names = "";
		try {
			nameFile = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Shinoow/AbyssalCraft/master/supporters.txt").openStream()));

			names = nameFile.readLine();
			nameFile.close();

		} catch (IOException e) {
			FMLLog.log("Hungering Darkness", Level.ERROR, "Failed to fetch supporter list, using local version!");
			names = "Tedyhere";
		}

		return names;
	}
}