package pon.main.utils.interfaces;

import pon.main.modules.combat.KillAura;

import java.util.List;

public interface IEntityLiving {
    double getPrevServerX();

    double getPrevServerY();

    double getPrevServerZ();

    List<KillAura.Position> getPositionHistory();
}

