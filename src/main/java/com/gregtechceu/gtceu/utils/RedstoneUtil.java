package com.gregtechceu.gtceu.utils;

import java.math.BigInteger;

public class RedstoneUtil {

    /**
     * Compares a value against a min and max, with an option to invert the logic
     *
     * @param value      value to be compared
     * @param minValue   the smallest possible value
     * @param maxValue   the largest possible value
     * @param isInverted whether to invert the logic of this method
     * @return an int from 0 (value <= min) to 15 (value >= max) normally, with a ratio when the value is between min
     *         and max
     */
    public static int computeRedstoneBetweenValues(int value, int minValue, int maxValue, boolean isInverted) {
        if (value >= maxValue) return isInverted ? 0 : 15;
        else if (value <= minValue) return isInverted ? 15 : 0;

        float ratio;
        if (isInverted) ratio = 15f * (maxValue - value) / (maxValue - minValue);
        else ratio = 15f * (value - minValue) / (maxValue - minValue);

        return Math.round(ratio);
    }

    public static int computeRedstoneBetweenValues(BigInteger value, BigInteger minValue, BigInteger maxValue,
                                                   boolean isInverted) {
        if (value.compareTo(maxValue) >= 0) return isInverted ? 0 : 15;
        else if (value.compareTo(maxValue) <= 0) return isInverted ? 15 : 0;

        float ratio;
        if (isInverted) ratio = 15f * GTMath.ratio(maxValue.subtract(value), maxValue.subtract(minValue));
        else ratio = 15f * GTMath.ratio(value.subtract(minValue), maxValue.subtract(minValue));

        return Math.round(ratio);
    }

    /**
     * Compares a value against a min and max, with an option to invert the logic. Has latching functionality.
     *
     * @param value    value to be compared
     * @param minValue the smallest possible value
     * @param maxValue the largest possible value
     * @param output   the output value the function modifies
     * @return returns the modified output value
     */
    public static int computeLatchedRedstoneBetweenValues(int value, int minValue, int maxValue,
                                                          boolean isInverted, int output) {
        if (value >= maxValue) {
            output = !isInverted ? 0 : 15; // value above maxValue should normally be 0, otherwise 15
        } else if (value <= minValue) {
            output = !isInverted ? 15 : 0; // value below minValue should normally be 15, otherwise 0
        }
        return output;
    }

    /**
     * Compares a value against a min and max, with an option to invert the logic. Has latching functionality.
     *
     * @param value    value to be compared
     * @param minValue the smallest possible value
     * @param maxValue the largest possible value
     * @param output   the output value the function modifies
     * @return returns the modified output value
     */
    public static int computeLatchedRedstoneBetweenValues(long value, long minValue, long maxValue,
                                                          boolean isInverted, int output) {
        if (value >= maxValue) {
            output = !isInverted ? 0 : 15; // value above maxValue should normally be 0, otherwise 15
        } else if (value <= minValue) {
            output = !isInverted ? 15 : 0; // value below minValue should normally be 15, otherwise 0
        }
        return output;
    }

    public static int computeLatchedRedstoneBetweenValues(BigInteger value, BigInteger minValue, BigInteger maxValue,
                                                          boolean isInverted, int output) {
        if (value.compareTo(maxValue) >= 0) {
            output = !isInverted ? 0 : 15; // value above maxValue should normally be 0, otherwise 15
        } else if (value.compareTo(minValue) <= 0) {
            output = !isInverted ? 15 : 0; // value below minValue should normally be 15, otherwise 0
        }
        return output;
    }

    /**
     * Compares a current against max, with an option to invert the logic.
     *
     * @param current    value to be compared
     * @param max        the max that value can be
     * @param isInverted whether to invert the logic of this method
     * @return value 0 to 15; A value of <em>at least</em> 1 is returned if current > 0
     * @throws ArithmeticException when max is 0
     */
    public static int computeRedstoneValue(long current, long max, boolean isInverted) throws ArithmeticException {
        int output = (int) (14f * current / max) + (current > 0 ? 1 : 0);
        return isInverted ? 15 - output : output;
    }

    public static int computeRedstoneValue(BigInteger current, BigInteger max,
                                           boolean isInverted) throws ArithmeticException {
        boolean isNotEmpty = current.compareTo(BigInteger.ZERO) > 0;
        int output = (int) (14f * GTMath.ratio(current, max)) + (isNotEmpty ? 1 : 0);
        return isInverted ? 15 - output : output;
    }
}
