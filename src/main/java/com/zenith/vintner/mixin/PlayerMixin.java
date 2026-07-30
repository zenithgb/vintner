package com.zenith.vintner.mixin;

import com.zenith.vintner.wine.WineConsumptionManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @ModifyVariable(
            method = "causeFoodExhaustion",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float vintner$reduceGeneralExhaustion(float amount) {
        return WineConsumptionManager.adjustGeneralExhaustion(
                (Player) (Object) this,
                amount
        );
    }

    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;"
                            + "causeFoodExhaustion(F)V"
            ),
            index = 0
    )
    private float vintner$reduceMeleeExhaustion(float amount) {
        return WineConsumptionManager.adjustMeleeExhaustion(
                (Player) (Object) this,
                amount
        );
    }
}
