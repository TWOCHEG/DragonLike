package pon.main.utils.interfaces;

import pon.main.modules.combat.KillAura;

public interface IOtherClientPlayerEntity {
    void resolve(KillAura.Resolver mode);

    void releaseResolver();
}
