package com.kotori316.marker.render;

import org.junit.jupiter.api.Test;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import static org.junit.jupiter.api.Assertions.*;

class BoxTest {

    private Box box(double x, double y, double z) {
        return Box.apply(new AxisAlignedBB(1, 2, 3, 1 + x, 2 + y, 3 + z), .125, .25, .5, true, false);
    }

    @Test
    void retainsAxisSpecializations() {
        assertEquals("BoxX", box(4, 0, 0).getClass().getSimpleName());
        assertEquals("BoxY", box(0, 4, 0).getClass().getSimpleName());
        assertEquals("BoxZ", box(0, 0, 4).getClass().getSimpleName());
        assertEquals("BoxXZ", box(4, 0, 4).getClass().getSimpleName());
        assertEquals("Box", box(4, 4, 4).getClass().getSimpleName());
    }

    @Test
    void retainsScalaStyleAccessors() {
        Box box = box(4, 5, 6);
        assertEquals(1, box.startX());
        assertEquals(2, box.startY());
        assertEquals(3, box.startZ());
        assertEquals(5, box.endX());
        assertEquals(7, box.endY());
        assertEquals(9, box.endZ());
        assertEquals(.125, box.sizeX());
        assertEquals(.25, box.sizeY());
        assertEquals(.5, box.sizeZ());
        assertTrue(box.firstSide());
        assertFalse(box.endSide());
    }

    @Test
    void retainsEqualityAndHashCode() {
        Box first = box(4, 5, 6), equal = box(4, 5, 6);
        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first, box(4, 5, 7));
        assertNotEquals(first, null);
        assertNotEquals(first, "box");
    }

    @Test
    void acceptsPointBoxesLikeOriginalFactory() {
        Box point = box(0, 0, 0);
        assertEquals("BoxZ", point.getClass().getSimpleName());
        assertEquals(point.startZ(), point.endZ());
    }

    @Test
    void preservesOrdinaryGeometryAndBoundsHugeRangeRendering() {
        TextureAtlasSprite sprite = new TextureAtlasSprite("test") {
        };
        sprite.setIconWidth(16);
        sprite.setIconHeight(16);
        sprite.initSprite(16, 16, 0, 0, false);
        double[][] ends = {{2, 0, 0}, {0, 2, 0}, {0, 0, 2}, {2, 0, 2}, {2, 1, 3}, {0, 0, 0},
            {Integer.MAX_VALUE, 0, 0}, {0, Integer.MAX_VALUE, 0}, {0, 0, Integer.MAX_VALUE},
            {30_000_000, 0, 30_000_000}, {30_000_000, 255, 30_000_000}};
        for (double[] end : ends) {
            Box box = Box.apply(0, 0, 0, end[0], end[1], end[2], .125, .125, .125, true, true);
            RecordingBuffer recorder = new RecordingBuffer();
            box.render(recorder, sprite);
            double tiles = Math.sqrt(end[0] * end[0] + end[1] * end[1] + end[2] * end[2]) / .125;
            int segments = tiles >= Box.MAX_SEGMENTS ? Box.MAX_SEGMENTS : MathHelper.floor(tiles) + 1;
            assertEquals(segments * 16 + 8, recorder.vertices);
            // Capping the work must still draw the full selected range.
            assertTrue(recorder.maxX >= end[0] - .125);
            assertTrue(recorder.maxY >= end[1] - .125);
            assertTrue(recorder.maxZ >= end[2] - .125);
        }
    }

    private static final class RecordingBuffer extends BufferBuilder {

        int vertices;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        RecordingBuffer() {
            super(256);
        }
        @Override
        public BufferBuilder pos(double x, double y, double z) {
            assertTrue(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z));
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
            return this;
        }
        @Override
        public BufferBuilder color(int r, int g, int b, int a) {
            return this;
        }
        @Override
        public BufferBuilder tex(double u, double v) {
            assertTrue(Double.isFinite(u) && Double.isFinite(v));
            return this;
        }
        @Override
        public BufferBuilder lightmap(int x, int y) {
            return this;
        }
        @Override
        public void endVertex() {
            assertTrue(++vertices <= Box.MAX_SEGMENTS * 16 + 8, "Unbounded range rendering");
        }

    }

}
