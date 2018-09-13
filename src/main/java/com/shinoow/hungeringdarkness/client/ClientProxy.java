package com.shinoow.hungeringdarkness.client;

import com.shinoow.hungeringdarkness.client.render.entity.layers.LayerStarSpawnTentacles;
import com.shinoow.hungeringdarkness.common.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.common.Loader;

public class ClientProxy extends CommonProxy {

	public void preInit() {}

	public void init() {
		if(!Loader.isModLoaded("abyssalcraft") && !Loader.isModLoaded("grue")){
			RenderPlayer render1 = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default");
			render1.addLayer(new LayerStarSpawnTentacles(render1));
			RenderPlayer render2 = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim");
			render2.addLayer(new LayerStarSpawnTentacles(render2));
		}
	}

	public void postInit() {}
}
