package com.zenith.vintner.client;

import com.zenith.vintner.network.AlmanacReportPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;

public class VintnerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(
				AlmanacReportPayload.TYPE,
				(payload, context) -> context.client().execute(() ->
					context.client().gui.setScreen(new BookViewScreen(
								new BookViewScreen.BookAccess(payload.pages())
						))
				)
		);
	}
}
