package pon.main.utils;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumConverter extends Converter<Enum, JsonElement> {
    private final Class<? extends Enum> clazz;

    public EnumConverter(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    public JsonElement doForward(Enum anEnum) {
        return new JsonPrimitive(anEnum.toString());
    }

    public Enum doBackward(JsonElement jsonElement) {
        try {
            return Enum.valueOf(this.clazz, jsonElement.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

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
