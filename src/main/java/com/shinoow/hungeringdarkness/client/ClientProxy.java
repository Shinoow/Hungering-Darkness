package com.shinoow.hungeringdarkness.client;

import com.shinoow.hungeringdarkness.client.render.entity.layers.LayerStarSpawnTentacles;
import com.shinoow.hungeringdarkness.common.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {}

	@Override
	public void init() {
		if(!Loader.isModLoaded("abyssalcraft") && !Loader.isModLoaded("grue")){
			RenderPlayer render1 = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default");
			render1.addLayer(new LayerStarSpawnTentacles(render1));
			RenderPlayer render2 = Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim");
			render2.addLayer(new LayerStarSpawnTentacles(render2));
		}
	}

	@Override
	public void postInit() {}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		// Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
		// your packets will not work because you will be getting a client
		// player even when you are on the server! Sounds absurd, but it's true.

		// Solution is to double-check side before returning the player:
		return ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx);
	}

	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		return ctx.side.isClient() ? Minecraft.getMinecraft() : super.getThreadFromContext(ctx);
	}
}
