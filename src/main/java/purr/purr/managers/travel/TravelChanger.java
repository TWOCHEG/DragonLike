package purr.purr.managers.travel;

import java.util.function.Supplier;

public record TravelChanger(int priority, Supplier<Float[]> rotateGetter, Supplier<Boolean> withMoveFix, Supplier<Boolean> strongMoveFix) {
}
