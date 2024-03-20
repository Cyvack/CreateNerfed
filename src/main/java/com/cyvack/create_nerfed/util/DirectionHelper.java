package com.cyvack.create_nerfed.util;

import net.minecraft.core.Direction;

import static net.minecraft.core.Direction.*;

/**
 * @author Cyvack
 */
public class DirectionHelper {
    public static Direction[]
        X_AXIS_PLANE = {NORTH, SOUTH, DOWN, UP}, //All directions except for ones that share the x-axis
        Y_AXIS_PLANE = {NORTH, SOUTH, EAST, WEST}, // All directions except for the ones that share the y-axis
        Z_AXIS_PLANE = {DOWN, UP, EAST, WEST}; //All directions except for the ones that share the z-axis

    public static Direction[] getSurroundingDirections(Axis axis) {
        return switch (axis) {
            case X -> X_AXIS_PLANE;
            case Y -> Y_AXIS_PLANE;
            case Z -> Z_AXIS_PLANE;
        };
    }
}
