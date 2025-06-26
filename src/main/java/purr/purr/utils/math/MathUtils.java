package purr.purr.utils.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MathUtils {
    private static final int[] defaultSymbols = {97, 122};
    private static final int[] capsSymbols = {65, 90};
    private static final int[] otherSymbols = {33, 46};
    private static final int[] numbers = {48, 57};

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static double roundNumber(double number, int scale) {
        BigDecimal bigDecimal = new BigDecimal(number);
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);

        return bigDecimal.doubleValue();
    }

    public static double roundToDecimal(double n, int point) {
        if (point == 0)
            return Math.floor(n);
        double factor = Math.pow(10, point);
        return Math.round(n * factor) / factor;
    }

    public static double getDistance(Vec3d from, Vec3d to) {
        float f = (float)(from.x - to.x);
        float g = (float)(from.y - to.y);
        float h = (float)(from.z - to.z);
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public static int nearest(int value, int min, int max) {
        double n = Math.abs(min - max) / 2d;
        if (value <= min + n) return min;
        else return max;
    }

    public static boolean hasInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static int applyRange(int number, int min, int max) {
        return Math.min(Math.max(min, number), max);
    }

    public static float applyRange(float number, float min, float max) {
        return Math.min(Math.max(min, number), max);
    }

    public static Vec3d transformPos(Matrix4f matrix, float x, float y, float z) {
        Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
        return new Vec3d(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public static List<Integer> getNumbers(Integer minNumber, Integer maxNumber) {
        ArrayList<Integer> numbers = new ArrayList<>();
        if (minNumber.doubleValue() > maxNumber.doubleValue()) {
            Integer temp = minNumber;
            minNumber = maxNumber;
            maxNumber = temp;
        }
        for (int i = minNumber; i < maxNumber + 1; i++)
            numbers.add(i);
        return numbers;
    }

    public static int randomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static float randomFloat(float min, float max) {
        return (float) Math.min(max, (new Random().nextDouble() * ((max * 1.1) - min)) + min);
    }

    public static double randomDouble(double min, double max) {
        return Math.min(max, (new Random().nextDouble() * ((max * 1.1) - min)) + min);
    }

    public static String randomString(int length, boolean andCaps, boolean andNumbers, boolean andOtherSymbols) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length + 1; i++) {
            char nextSymbol = (char) 0;
            while (nextSymbol == (char) 0) {
                switch (randomInt(1,4)) {
                    case 1 -> nextSymbol = (char) randomInt(defaultSymbols[0], defaultSymbols[1]);
                    case 2 -> {
                        if (andCaps)
                            nextSymbol = (char) randomInt(capsSymbols[0], capsSymbols[1]);
                    }
                    case 3 -> {
                        if (andNumbers)
                            nextSymbol = (char) randomInt(numbers[0], numbers[1]);
                    }
                    case 4 -> {
                        if (andOtherSymbols)
                            nextSymbol = (char) randomInt(otherSymbols[0], otherSymbols[1]);
                    }
                }
            }
            stringBuilder.append(nextSymbol);
        }
        return stringBuilder.toString();
    }
}