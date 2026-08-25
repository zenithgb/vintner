package com.zenith.vintner.item;

import net.minecraft.world.item.Item;

/**
 * A lightweight serving vessel used with placed bottles and tasting services.
 * It deliberately has no placeable form; complete table settings are handled
 * by the tasting-service block instead of fragile individual glass models.
 */
public class WineGlassItem extends Item {
    public WineGlassItem(Properties properties) {
        super(properties);
    }
}
