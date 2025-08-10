package pon.purr.modules.render;

import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import pon.purr.Purr;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.math.AnimHelper;

public class NoCameraClip extends Parent {
    public NoCameraClip() {
        super("no camera clip", Purr.Categories.render);
    }

    public Setting<Boolean> antiFront = new Setting<>("AntiFront", false);
    public Setting<Float> distance = new Setting<>("Distance", 3f, 1f, 20f);
    private float animation;

    public void onRender3D(MatrixStack matrix) {
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) animation = AnimHelper.fast(animation, 0f, 10);
        else animation = AnimHelper.fast(animation, 1f, 10);

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && antiFront.getValue())
            mc.options.setPerspective(Perspective.FIRST_PERSON);
    }

    public float getDistance() {
        return 1f + ((distance.getValue() - 1f) * animation);
    }
}
