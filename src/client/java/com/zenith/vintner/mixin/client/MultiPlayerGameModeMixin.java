package com.zenith.vintner.mixin.client;

import com.zenith.vintner.block.TastingServiceBlock;
import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.network.TastingServicePickPayload;
import com.zenith.vintner.wine.VisibleWineSlotTargeting;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes only an actual displayed-wine pick through the narrow C2S path. */
@Mixin(MultiPlayerGameMode.class)
abstract class MultiPlayerGameModeMixin {
    @Inject(
            method = "handlePickItemFromBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vintner$pickDisplayedWineBottle(
            BlockPos pos,
            boolean includeData,
            CallbackInfo callback
    ) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null
                || client.level == null
                || !client.player.getAbilities().instabuild
                || !(client.hitResult instanceof BlockHitResult hit)
                || !hit.getBlockPos().equals(pos)
                || !ClientPlayNetworking.canSend(
                TastingServicePickPayload.TYPE
        )) {
            return;
        }

        var state = client.level.getBlockState(pos);
        boolean placedBottle = state.getBlock() instanceof WineBottleBlock
                && state.getValue(WineBottleBlock.SERVINGS) > 0;
        boolean serviceBottle = state.getBlock()
                instanceof TastingServiceBlock
                && state.getValue(TastingServiceBlock.SERVINGS) > 0
                && VisibleWineSlotTargeting.targetsTastingServiceBottle(
                        state,
                        pos,
                        hit
                );
        if (!placedBottle && !serviceBottle) {
            return;
        }

        ClientPlayNetworking.send(new TastingServicePickPayload(pos));
        callback.cancel();
    }
}
