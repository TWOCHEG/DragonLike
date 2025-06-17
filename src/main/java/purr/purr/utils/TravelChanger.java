package purr.purr.utils;

import java.util.function.Supplier;

public record TravelChanger(int priority, Supplier<Float[]> rotateGetter, Supplier<Boolean> withMoveFix, Supplier<Boolean> strongMoveFix) {
}
