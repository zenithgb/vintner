package com.zenith.vintner.mixin;

import com.zenith.vintner.wine.WinePairingManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "finishUsingItem",
            at = @At("HEAD")
    )
    private void vintner$rememberWinePairing(
            Level level,
            LivingEntity consumer,
            CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        ItemStack stack = (ItemStack) (Object) this;

        if (
                level instanceof ServerLevel serverLevel
                        && stack.has(DataComponents.FOOD)
        ) {
            WinePairingManager.onMealConsumed(
                    serverLevel,
                    consumer,
                    stack
            );
        }
    }
}
