package com.zenith.vintner.client;

import com.zenith.vintner.client.screen.EstateManagementDeskScreen;
import com.zenith.vintner.client.render.EstateManagementDeskRenderer;
import com.zenith.vintner.network.AlmanacReportPayload;
import com.zenith.vintner.network.EstateDeskPayload;
import com.zenith.vintner.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;

public class VintnerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(
				ModBlockEntities.ESTATE_MANAGEMENT_DESK,
				EstateManagementDeskRenderer::new
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
