package com.ahmer.afzal.pdfviewer.util;

public class MathUtils {

    static private final int BIG_ENOUGH_INT = 16 * 1024;
    static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    static private final double BIG_ENOUGH_CEIL = 16384.999999999996;

    private MathUtils() {
        // Prevents instantiation
    }

    /**
     * Limits the given <b>number</b> between the other values
     *
     * @param number  The number to limit.
     * @param between The smallest value the number can take.
     * @param and     The biggest value the number can take.
     * @return The limited number.
     */
    public static int limit(int number, int between, int and) {
        if (number <= between) {
            return between;
        }
        return Math.min(number, and);
    }

    /**
     * Limits the given <b>number</b> between the other values
     *
     * @param number  The number to limit.
     * @param between The smallest value the number can take.
     * @param and     The biggest value the number can take.
     * @return The limited number.
     */
    public static float limit(float number, float between, float and) {
        if (number <= between) {
            return between;
        }
        return Math.min(number, and);
    }

    public static float max(float number, float max) {
        return Math.min(number, max);
    }

    public static float min(float number, float min) {
        return Math.max(number, min);
    }

    public static int max(int number, int max) {
        return Math.min(number, max);
    }

    public static int min(int number, int min) {
        return Math.max(number, min);
    }

    /**
     * Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14).
     */
    static public int floor(float value) {
        return (int) (value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14).
     */
    static public int ceil(float value) {
        return (int) (value + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
    }
}
