package com.zenith.vintner.registry;

import com.zenith.vintner.block.entity.CellarCollectionBlockEntity;
import com.zenith.vintner.block.entity.TastingServiceBlockEntity;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import com.zenith.vintner.block.entity.WineCrateBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** One-shot, placement-time stock for the five maintained village winery buildings. */
public final class GeneratedWineryStock {
    private static final Set<String> BUILDINGS = Set.of(
            "vintner:village/plains/vineyard",
            "vintner:village/desert/vineyard",
            "vintner:village/savanna/winery_tier1",
            "vintner:village/snowy/vineyard",
            "vintner:village/taiga/winery_tier1"
    );

    private GeneratedWineryStock() {}

    public static boolean supports(Identifier templateId) {
        return BUILDINGS.contains(templateId.toString());
    }

    public enum Profile {
        LIGHT(1), NORMAL(2), STOCKED(4);
        final int bottles;
        Profile(int bottles) { this.bottles = bottles; }
    }

    public static long mix(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public static long winerySeed(long worldSeed, Identifier templateId, BlockPos origin) {
        long value = mix(worldSeed ^ 0x6A09E667F3BCC909L);
        value = mix(value ^ templateId.toString().hashCode());
        value = mix(value ^ ((long) origin.getX() << 32) ^ (origin.getZ() & 0xffffffffL));
        return value;
    }

    public static long containerSeed(long winerySeed, BlockPos pos, String role) {
        return mix(winerySeed ^ pos.asLong() ^ role.hashCode());
    }

    /** LIGHT 40%, NORMAL 45%, STOCKED 15%. */
    public static Profile profile(long winerySeed) {
        int roll = (int) Long.remainderUnsigned(mix(winerySeed), 100L);
        return roll < 40 ? Profile.LIGHT : roll < 85 ? Profile.NORMAL : Profile.STOCKED;
    }

    public static void populate(StructureTemplateManager templates, WorldGenLevel level,
            Identifier templateId, BlockPos origin, BlockPos pivot, Rotation rotation,
            BoundingBox chunkBox) {
        if (!supports(templateId)) return;
        StructureTemplate template = templates.getOrCreate(templateId);
        // SinglePoolElement.place uses the default zero rotation pivot.
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation);
        List<Target> targets = new ArrayList<>();
        collect(targets, template, origin, settings, Blocks.CHEST, "chest");
        for (Block block : ModBlocks.WINE_RACKS.values())
            collect(targets, template, origin, settings, block, "rack");
        for (Block block : ModBlocks.WINE_CRATES.values())
            collect(targets, template, origin, settings, block, "crate");
        for (Block block : ModBlocks.LABELLED_CELLAR_SHELVES.values())
            collect(targets, template, origin, settings, block, "shelf");
        for (Block block : ModBlocks.WINE_BASKETS.values())
            collect(targets, template, origin, settings, block, "basket");
        for (Block block : ModBlocks.TASTING_SERVICES.values())
            collect(targets, template, origin, settings, block, "tasting");

        long winerySeed = winerySeed(level.getSeed(), templateId, origin);
        Profile profile = profile(winerySeed);
        List<Target> displays = targets.stream()
                .filter(target -> !target.role.equals("chest"))
                .sorted((a, b) -> {
                    int rank = Long.compareUnsigned(
                            containerSeed(winerySeed, a.pos, a.role),
                            containerSeed(winerySeed, b.pos, b.role));
                    return rank != 0 ? rank : a.pos.compareTo(b.pos);
                }).toList();
        int selected = Math.min(profile.bottles, displays.size());
        for (int i = 0; i < selected; i++) {
            Target target = displays.get(i);
            if (!chunkBox.isInside(target.pos)) continue;
            BlockEntity entity = level.getBlockEntity(target.pos);
            ItemStack wine = ordinaryWine(winerySeed, target.pos);
            if (entity instanceof WineRackBlockEntity rack && rack.getBottleCount() == 0)
                rack.insertOne(wine);
            else if (entity instanceof WineCrateBlockEntity crate && crate.getBottleCount() == 0)
                crate.insertOne(wine);
            else if (entity instanceof CellarCollectionBlockEntity shelf && shelf.getBottleCount() == 0)
                shelf.insertOne(wine);
            else if (entity instanceof WineBasketBlockEntity basket && basket.getBottleCopy().isEmpty()) {
                if (basket.insertBottle(wine))
                    level.setBlock(target.pos,
                            basket.displayState(level.getBlockState(target.pos)), Block.UPDATE_CLIENTS);
            } else if (entity instanceof TastingServiceBlockEntity tasting && tasting.getBottleCopy().isEmpty()) {
                if (tasting.insertBottle(wine))
                    level.setBlock(target.pos,
                            tasting.displayState(level.getBlockState(target.pos)), Block.UPDATE_CLIENTS);
            }
        }
        for (Target target : targets) {
            if (!target.role.equals("chest") || !chunkBox.isInside(target.pos)) continue;
            if (level.getBlockEntity(target.pos) instanceof Container chest)
                stockChest(chest, winerySeed, target.pos, profile);
        }
    }

    private static void collect(List<Target> targets, StructureTemplate template, BlockPos origin,
            StructurePlaceSettings settings, Block block, String role) {
        for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(origin, settings, block))
            targets.add(new Target(info.pos(), role));
    }

    private static ItemStack ordinaryWine(long winerySeed, BlockPos pos) {
        long seed = containerSeed(winerySeed, pos, "wine");
        ItemStack bottle = new ItemStack((seed & 1L) == 0L
                ? ModItems.RED_WINE : ModItems.WHITE_WINE);
        // The existing generic TABLE-wine contract intentionally has unknown
        // provenance. Never invent a harvest, producer, or processing batch.
        WineMetadata.ensureDefaults(bottle);
        WineMetadata.ensureBatchIdentity(bottle, mix(seed ^ 0xD6E8FEB86659FD93L));
        WineMetadata.markBottled(bottle, 1L);
        return bottle;
    }

    private static void stockChest(Container chest, long winerySeed, BlockPos pos,
            Profile profile) {
        long seed = containerSeed(winerySeed, pos, "chest");
        List<GrapeCultivar> cultivars = GrapeCultivar.activeValues();
        GrapeCultivar cultivar = cultivars.get((int) Long.remainderUnsigned(
                mix(winerySeed ^ 0xA0761D6478BD642FL), cultivars.size()));
        ItemStack grapes = new ItemStack(cultivar.variety() == GrapeVariety.RED
                ? ModItems.RED_GRAPES : ModItems.WHITE_GRAPES,
                profile == Profile.LIGHT ? 2 : profile == Profile.NORMAL ? 3 : 4);
        WineMetadata.applyCultivar(grapes, cultivar);
        putInEmptySlot(chest, grapes, seed, 0);
        if (profile != Profile.LIGHT)
            putInEmptySlot(chest, new ItemStack(Items.GLASS_BOTTLE, 1), seed, 1);
    }

    private static void putInEmptySlot(Container container, ItemStack stack, long seed,
            int index) {
        int size = container.getContainerSize();
        if (size == 0) return;
        // A fixed slot makes repeated chunk-piece invocation idempotent.
        int slot = ((int) Long.remainderUnsigned(seed, size) + index) % size;
        if (container.getItem(slot).isEmpty()) {
            container.setItem(slot, stack);
            container.setChanged();
        }
    }

    private record Target(BlockPos pos, String role) {}
}
