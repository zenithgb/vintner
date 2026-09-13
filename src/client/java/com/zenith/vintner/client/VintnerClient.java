package com.zenith.vintner.client;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.client.screen.EstateManagementDeskScreen;
import com.zenith.vintner.client.render.EstateManagementDeskRenderer;
import com.zenith.vintner.client.render.WineBasketRenderer;
import com.zenith.vintner.network.AlmanacReportPayload;
import com.zenith.vintner.network.EstateDeskPayload;
import com.zenith.vintner.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.resources.Identifier;

public class VintnerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(
				ModBlockEntities.ESTATE_MANAGEMENT_DESK,
				EstateManagementDeskRenderer::new
		);
		BlockEntityRendererRegistry.register(
				ModBlockEntities.WINE_BASKET,
				WineBasketRenderer::new
		);
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.CROSSHAIR,
				Identifier.fromNamespaceAndPath(
						Vintner.MOD_ID,
						"nearby_wine"
				),
				new NearbyWineHud()
		);
		ClientPlayNetworking.registerGlobalReceiver(
				AlmanacReportPayload.TYPE,
				(payload, context) -> context.client().execute(() ->
					context.client().gui.setScreen(new BookViewScreen(
								new BookViewScreen.BookAccess(payload.pages())
						))
				)
		);
		ClientPlayNetworking.registerGlobalReceiver(
				EstateDeskPayload.TYPE,
				(payload, context) -> context.client().execute(() ->
						context.client().gui.setScreen(
								new EstateManagementDeskScreen(payload)
						)
				)
		);
	}
}
