package com.zenith.vintner.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Describes where a desk module sits relative to its connected Estate
 * Management Desk. Modules use this state to align themselves automatically;
 * the desk uses the same relationship to close the join in its writing top.
 */
public enum DeskModuleConnection implements StringRepresentable {
    NONE,
    LEFT,
    RIGHT,
    FRONT,
    BACK;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Attachment find(
            LevelReader level,
            BlockPos modulePos,
            Direction preferredFacing
    ) {
        Attachment fallback = null;
        for (Direction towardDesk : Direction.Plane.HORIZONTAL) {
            BlockState neighbour = level.getBlockState(
                    modulePos.relative(towardDesk)
            );
            if (!(neighbour.getBlock()
                    instanceof EstateManagementDeskBlock)
                    || !neighbour.hasProperty(
                    EstateManagementDeskBlock.FACING
            )) {
                continue;
            }

            Direction deskFacing = neighbour.getValue(
                    EstateManagementDeskBlock.FACING
            );
            Direction moduleFromDesk = towardDesk.getOpposite();
            Attachment candidate = new Attachment(
                    deskFacing,
                    fromRelativeDirection(deskFacing, moduleFromDesk)
            );
            if (deskFacing == preferredFacing) {
                return candidate;
            }
            if (fallback == null) {
                fallback = candidate;
            }
        }
        return fallback == null
                ? new Attachment(preferredFacing, NONE)
                : fallback;
    }

    public Direction worldDirection(Direction deskFacing) {
        return switch (this) {
            case LEFT -> deskFacing.getCounterClockWise();
            case RIGHT -> deskFacing.getClockWise();
            case FRONT -> deskFacing;
            case BACK -> deskFacing.getOpposite();
            case NONE -> deskFacing;
        };
    }

    private static DeskModuleConnection fromRelativeDirection(
            Direction deskFacing,
            Direction moduleFromDesk
    ) {
        if (moduleFromDesk == deskFacing.getCounterClockWise()) {
            return LEFT;
        }
        if (moduleFromDesk == deskFacing.getClockWise()) {
            return RIGHT;
        }
        if (moduleFromDesk == deskFacing) {
            return FRONT;
        }
        return BACK;
    }

    public record Attachment(
            Direction facing,
            DeskModuleConnection connection
    ) {
    }
}
