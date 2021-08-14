package com.shinoow.hungeringdarkness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.shinoow.hungeringdarkness.common.CommonProxy;
import com.shinoow.hungeringdarkness.common.cap.DarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.cap.DarknessTimerCapabilityStorage;
import com.shinoow.hungeringdarkness.common.cap.IDarknessTimerCapability;
import com.shinoow.hungeringdarkness.common.handlers.HungeringDarknessEventHandler;
import com.shinoow.hungeringdarkness.common.integrations.gamestages.GameStagesHandler;
import com.shinoow.hungeringdarkness.common.util.DimensionData;

import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod(modid = HungeringDarkness.modid, name = HungeringDarkness.name, version = HungeringDarkness.version, dependencies = "required-after:forge@[forgeversion,);required-after:darknesslib@[1.1.0,)", acceptedMinecraftVersions = "[1.12.2]", guiFactory = "com.shinoow.hungeringdarkness.client.config.HungeringDarknessGuiFactory", updateJSON = "https://raw.githubusercontent.com/Shinoow/Hungering-Darkness/master/version.json", useMetadata = false, certificateFingerprint = "cert_fingerprint")
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

	public static int damageFrequency, damage, delay, light_level, total_darkness, height;
	public static int[] dimWhitelist;
	public static String[] hurt_stages, nohurt_stages, biomeWhitelist;
	public static boolean useBlacklist, unrealisticLight, useBiomeBlacklist;

	private static DimensionData config_default;

	public static final List<Biome> biome_whitelist = new ArrayList<>();

	public static Map<Integer, DimensionData> dimension_configs = new HashMap<>();

	public static DamageSource darkness = new DamageSource("darkness").setDamageBypassesArmor().setDamageIsAbsolute();

	public static Logger LOGGER = LogManager.getLogger("Hungering Darkness");

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){

		metadata = event.getModMetadata();
		getSupporterList();
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new HungeringDarknessEventHandler());

		cfg = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
		config_default = new ConfigDimData();

		CapabilityManager.INSTANCE.register(IDarknessTimerCapability.class, DarknessTimerCapabilityStorage.instance, DarknessTimerCapability::new);

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		proxy.init();
		if(Loader.isModLoaded("gamestages"))
			GameStagesHandler.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		proxy.postInit();
		Arrays.stream(biomeWhitelist).map(b -> new ResourceLocation(b)).forEach(b -> addBiomeToList(b));
	}

	@EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		LOGGER.log(Level.WARN, "Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(modid)) {
			syncConfig();
			biome_whitelist.clear();
			Arrays.stream(biomeWhitelist).map(b -> new ResourceLocation(b)).forEach(b -> addBiomeToList(b));
		}
	}

	private static void syncConfig(){

		cfg.setCategoryComment(Configuration.CATEGORY_GENERAL, "CONFIG ALL THE THINGS! Any changes take effect immediately (unless you're on a server, then the server has to restart).");

		dimWhitelist = cfg.get(Configuration.CATEGORY_GENERAL, "Dimension Whitelist", new int[]{0}, "Add dimension IDs to this list if you want the darkness to damage players here.").getIntList();
		damageFrequency = cfg.getInt("Damage Frequency", Configuration.CATEGORY_GENERAL, 5, 1, 10, "The amount of time (in seconds) between each hit when the darkness damages you.");
		damage = cfg.getInt("Damage Amount", Configuration.CATEGORY_GENERAL, 2, 2, 20, "The amount of damage (half hearts) you take from the darkness.");
		delay = cfg.getInt("Damage Delay", Configuration.CATEGORY_GENERAL, 10, 1, 60, "The amount of time (in seconds) you have to spend in darkness before you begin to receive damage.");
		light_level = cfg.getInt("Light Level", Configuration.CATEGORY_GENERAL, 4, 0, 15, "The light level where it's considered dark enough that you begin to receive damage.");
		total_darkness = cfg.getInt("Total Darkness Light Level", Configuration.CATEGORY_GENERAL, 0, -1, 15, "The light level that's considered total darkness. Being in this light level halves the time it takes before you start to receive damage, while also doubling the damage receieved. Disable by setting it to -1.");
		useBlacklist = cfg.get(Configuration.CATEGORY_GENERAL, "Use Blacklist", false, "Toggles whether or not to use the dimension whitelist as a blacklist instead.").getBoolean();
		hurt_stages = cfg.get(Configuration.CATEGORY_GENERAL, "Hurting Stages", new String[] {}, "If Game Stages is installed, this list can be used to specify stages where the darkness hurt you. Format is stage:priority, where stage is the stage name and the priority is an integer that determines if this takes effect over the non-hurting stages (higher number = higher priority).").getStringList();
		nohurt_stages = cfg.get(Configuration.CATEGORY_GENERAL, "Non-hurting Stages", new String[] {}, "If Game Stages is installed, this list can be used to specify stages where the darkness doesn't hurt you. Format is stage:priority, where stage is the stage name and the priority is an integer that determines if this takes effect over the hurting stages (higher number = higher priority).").getStringList();
		height = cfg.getInt("Damage Height", Configuration.CATEGORY_GENERAL, 256, 0, 256, "The y-level where the you're considered safe from the darkness regardless of light level. Going below this y-level will make the darkness damage you again.");
		unrealisticLight = cfg.get(Configuration.CATEGORY_GENERAL, "Unrealistic Light", false, "Toggles whether or not Dynamic Light behaves as if the player is fully lit up, rather than adding to the light level. Faking full brightness is what the old behavior did.").getBoolean();
		String[] data = cfg.get(Configuration.CATEGORY_GENERAL, "Dimension-specific Configuration", new String[0], "Values added to this list will allow you to define dimension-specific configurations that override any of the corresponding global ones defined in the other options. "
				+ "\nFormat: dim_id:damage_frequency:damage:delay:light_level:total_darkness:height"
				+ "\nEvery value except dim_id can be substituted with def, which'll set the value to whatever the global config uses."
				+ "\nExample: '0:10:def:60:4:-1:128'").getStringList();
		biomeWhitelist = cfg.get(Configuration.CATEGORY_GENERAL, "Biome Whitelist", new String[0], "Add biome IDs to this list if you want the darkness to damage players here. The list won't be used if it's empty.\nFormat: modid:name").getStringList();
		useBiomeBlacklist = cfg.get(Configuration.CATEGORY_GENERAL, "Use Biome Blacklist", false, "Toggles whether or not to use the biome whitelist as a blacklist instead.").getBoolean();

		dimension_configs = Arrays.stream(data).map(s -> s.trim().split(":")).collect(Collectors.toMap(s -> Integer.parseInt(s[0]), s -> new DimensionData(s)));

		if(cfg.hasChanged())
			cfg.save();
	}

	private static void addBiomeToList(ResourceLocation res) {
		if(ForgeRegistries.BIOMES.containsKey(res))
			biome_whitelist.add(ForgeRegistries.BIOMES.getValue(res));
		else LOGGER.log(Level.ERROR, "{} is not a valid Biome!", res);
	}

	public static DimensionData getDimensionConfig(int id) {
		return dimension_configs.getOrDefault(id, config_default);
	}

	private void getSupporterList(){
		new Thread("Hungering Darkness Get Supporters") {
			public void run() {
				BufferedReader nameFile;
				String names = "";
				try {
					nameFile = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Shinoow/AbyssalCraft/master/supporters.txt").openStream()));

					names = nameFile.readLine();
					nameFile.close();

				} catch (IOException e) {
					LOGGER.log(Level.ERROR, "Failed to fetch supporter list, using local version!");
					names = "Jenni Mort, Simon.R.K";
				}

				metadata.description += String.format("\n\n\u00a76Supporters: %s\u00a7r", names);
			}
		}.start();
	}

	private class ConfigDimData extends DimensionData {

		@Override
		public int getDamageFrequency() {
			return damageFrequency;
		}

		@Override
		public int getDamage() {
			return damage;
		}

		@Override
		public int getDelay() {
			return delay;
		}

		@Override
		public int getLightLevel() {
			return light_level;
		}

		@Override
		public int getTotalDarkness() {
			return total_darkness;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}
}