package pon.main.utils;

public class EnumHelper {
    @SuppressWarnings("unchecked")
    public static Enum[] getConstaints(Enum<?> enumValue) {
        Class<?> enumClass = enumValue.getDeclaringClass();
        Enum[] constants = (Enum[]) enumClass.getEnumConstants();
        return constants;
    }

    public static String getNameFromEnum(Enum<?> enumValue) {
        String simpleName = enumValue.getDeclaringClass().getSimpleName();
        return simpleName.replaceAll(
                "(?<=[a-z])([A-Z])|(?<=[A-Z])([A-Z][a-z])",
                " $1$2"
        ).trim().toLowerCase();
    }
}
