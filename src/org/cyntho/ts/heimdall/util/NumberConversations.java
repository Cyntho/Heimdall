package org.cyntho.ts.heimdall.util;

/**
 * The (static) NumberConversation class provides
 * some functionality to handleUserPlaceholder int/float/double etc.
 * to other data types
 *
 * Created by Xida on 12.07.2017.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public final class NumberConversations {

    private NumberConversations() {}

    public static int floor(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int ceil(final double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor + (int) (~Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int round(double num) {
        return floor(num + 0.5d);
    }

    public static double square(double num) {
        return num * num;
    }

    public static int toInt(Object object) throws NumberFormatException, NullPointerException {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        return Integer.valueOf(object.toString());
    }

    public static float toFloat(Object object) throws NumberFormatException, NullPointerException {
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        }
        return Float.valueOf(object.toString());
    }

    public static double toDouble(Object object) throws NumberFormatException, NullPointerException {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }

        return Double.valueOf(object.toString());
    }

    public static long toLong(Object object) throws NumberFormatException, NullPointerException {
        if (object instanceof Number) {
            return ((Number) object).longValue();
        }

        return Long.valueOf(object.toString());
    }

    public static short toShort(Object object) throws NumberFormatException, NullPointerException {
        if (object instanceof Number) {
            return ((Number) object).shortValue();
        }

        return Short.valueOf(object.toString());
    }

    public static byte toByte(Object object) throws NumberFormatException, NullPointerException {
        if (object instanceof Number) {
            return ((Number) object).byteValue();
        }
        return Byte.valueOf(object.toString());
    }

}
