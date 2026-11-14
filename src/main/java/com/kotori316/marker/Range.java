package com.kotori316.marker;

public class Range {

    private final float min;
    private final float max;
    private final float distance;

    public Range(float min, float max) {
        this.min = min;
        this.max = max;
        if (!Float.isFinite(min) || !Float.isFinite(max) || max <= min || !Float.isFinite(max - min)) {
            throw new IllegalArgumentException(String.format("Invalid range. Min: %f, Max: %f", min, max));
        }
        this.distance = max - min;
    }

    public float convert(float f) {
        if (!Float.isFinite(f))
            return min;
        double remainder = ((double) f - min) % distance;
        if (f < min) {
            return remainder == 0 ? max : (float) (max + remainder);
        } else if (f > max) {
            return (float) (min + remainder);
        } else {
            return f;
        }
    }

    public static void main(String[] args) {
        Range range = new Range(0, 8);
        System.out.printf("convert of 4 is %f.\n", range.convert(4));
        System.out.printf("convert of 0 is %f.\n", range.convert(0));
        System.out.printf("convert of 8 is %f.\n", range.convert(8));
        System.out.printf("convert of 12 is %f.\n", range.convert(12));
        System.out.printf("convert of -4 is %f.\n", range.convert(-4));
        System.out.printf("convert of -5 is %f.\n", range.convert(-5));
    }

}
