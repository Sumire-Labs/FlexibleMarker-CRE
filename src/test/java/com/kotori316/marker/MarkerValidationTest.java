package com.kotori316.marker;

import org.junit.jupiter.api.Test;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import static org.junit.jupiter.api.Assertions.*;

class MarkerValidationTest {

    @Test
    void missingOrVerticalFacingFallsBackToHorizontal() {
        assertEquals(EnumFacing.NORTH, TileFlexMarker.horizontalFacing(null));
        assertEquals(EnumFacing.NORTH, TileFlexMarker.horizontalFacing(EnumFacing.UP));
        assertEquals(EnumFacing.NORTH, TileFlexMarker.horizontalFacing(EnumFacing.DOWN));
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            assertEquals(facing, TileFlexMarker.horizontalFacing(facing));
        }
    }

    @Test
    void rejectsMalformedChunkSizesAndHeights() {
        assertTrue(valid(BlockPos.ORIGIN, 16, 255, 0));
        assertTrue(valid(BlockPos.ORIGIN, 32, 64, 64));
        assertFalse(valid(BlockPos.ORIGIN, 0, 64, 0));
        assertFalse(valid(BlockPos.ORIGIN, -16, 64, 0));
        assertFalse(valid(BlockPos.ORIGIN, 17, 64, 0));
        assertFalse(valid(BlockPos.ORIGIN, 16, 256, 0));
        assertFalse(valid(BlockPos.ORIGIN, 16, 64, -1));
        assertFalse(valid(BlockPos.ORIGIN, 16, 63, 64));
        assertFalse(valid(BlockPos.ORIGIN, Integer.MAX_VALUE - 15, 64, 0));
    }

    @Test
    void boundsUseTheActualExpansionDirection() {
        assertTrue(valid(new BlockPos(29_999_999, 64, 0), 16, 64, 0));
        assertFalse(valid(new BlockPos(-29_999_999, 64, 0), 16, 64, 0));
        assertFalse(valid(new BlockPos(0, 64, 29_999_999), 16, 64, 0));
        assertTrue(valid(new BlockPos(0, 64, -29_999_999), 16, 64, 0));
    }

    private static boolean valid(BlockPos origin, int size, int top, int bottom) {
        return Tile16Marker.validSize(origin, EnumFacing.AxisDirection.NEGATIVE,
            EnumFacing.AxisDirection.POSITIVE, size, top, bottom, 256);
    }

}
