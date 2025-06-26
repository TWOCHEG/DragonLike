package purr.purr.utils.travel;

import java.util.function.Supplier;

public record TravelChanger(int priority, Supplier<Float[]> rotateGetter, Supplier<Boolean> withMoveFix, Supplier<Boolean> strongMoveFix) {
}
