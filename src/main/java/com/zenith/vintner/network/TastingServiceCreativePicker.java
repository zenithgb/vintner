package com.zenith.vintner.network;

import com.zenith.vintner.block.entity.TastingServiceBlockEntity;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.wine.VisibleWineSlotTargeting;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Server-authoritative handling for the Tasting Service bottle pick. */
public final class TastingServiceCreativePicker {
    private TastingServiceCreativePicker() {
    }

    public static boolean tryPick(
            ServerPlayer player,
            BlockPos requestedPos
    ) {
        HitResult currentHit = player.pick(
                player.blockInteractionRange(),
                1.0F,
                false
        );
        return currentHit instanceof BlockHitResult blockHit
                && tryPick(player, requestedPos, blockHit);
    }

    /**
     * Testable validation core. Production callers must supply the server's
     * freshly recomputed crosshair hit, never a hit decoded from the client.
     */
    public static boolean tryPick(
            ServerPlayer player,
            BlockPos requestedPos,
            BlockHitResult currentHit
    ) {
        if (!player.gameMode.isCreative()
                || !player.hasInfiniteMaterials()) {
            return false;
        }

        ServerLevel level = player.level();
        if (!level.isLoaded(requestedPos)
                || !player.isWithinBlockInteractionRange(
                requestedPos,
                1.0
        )
                || !currentHit.getBlockPos().equals(requestedPos)) {
            return false;
        }

        var state = level.getBlockState(requestedPos);
        if (!VisibleWineSlotTargeting.targetsTastingServiceBottle(
                state,
                requestedPos,
                currentHit
        )
                || !(level.getBlockEntity(requestedPos)
                instanceof TastingServiceBlockEntity service)) {
            return false;
        }

        ItemStack bottle = service.getBottleCopy();
        if (!(bottle.getItem() instanceof WineItem)
                || WineMetadata.servings(bottle) <= 0) {
            return false;
        }

        pickExactCopy(player, bottle.copyWithCount(1));
        return true;
    }

    private static void pickExactCopy(
            ServerPlayer player,
            ItemStack bottle
    ) {
        Inventory inventory = player.getInventory();
        int existingSlot = inventory.findSlotMatchingItem(bottle);

        if (existingSlot >= 0) {
            if (Inventory.isHotbarSlot(existingSlot)) {
                inventory.setSelectedSlot(existingSlot);
            } else {
                inventory.pickSlot(existingSlot);
            }
        } else {
            inventory.addAndPickItem(bottle);
        }

        player.connection.send(new ClientboundSetHeldSlotPacket(
                inventory.getSelectedSlot()
        ));
        player.inventoryMenu.broadcastChanges();
    }
}
