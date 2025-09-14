package pon.main.injection;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pon.main.managers.Managers;
import pon.main.managers.main.ModuleManager;
import pon.main.modules.combat.KillAura;
import pon.main.modules.misc.FakePlayer;
import pon.main.utils.interfaces.IEntityLiving;
import pon.main.utils.interfaces.IOtherClientPlayerEntity;

import static pon.main.modules.Parent.mc;

@Mixin(OtherClientPlayerEntity.class)
public class MixinOtherClientPlayerEntity extends AbstractClientPlayerEntity implements IOtherClientPlayerEntity {
    @Unique private double backUpX, backUpY, backUpZ;

    public MixinOtherClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    public void resolve(KillAura.Resolver mode) {
        if ((Object) this == FakePlayer.fakePlayer) {
            backUpY = -999;
            return;
        }

        backUpX = getX();
        backUpY = getY();
        backUpZ = getZ();

        if (mode == KillAura.Resolver.BackTrack) {
            double minDst = 999d;
            KillAura.Position bestPos = null;
            for (KillAura.Position p : ((IEntityLiving) this).getPositionHistory()) {
                double dst = mc.player.squaredDistanceTo(p.getX(), p.getY(), p.getZ());
                if (dst < minDst) {
                    minDst = dst;
                    bestPos = p;
                }
            }
            if(bestPos != null) {
                setPosition(bestPos.getX(), bestPos.getY(), bestPos.getZ());
                if (KillAura.target == this)
                    Managers.MODULE_MANAGER.getModule(KillAura.class).resolvedBox = getBoundingBox();
            }
            return;
        }

        Vec3d from = new Vec3d(((IEntityLiving) this).getPrevServerX(), ((IEntityLiving) this).getPrevServerY(), ((IEntityLiving) this).getPrevServerZ());
        Vec3d to = new Vec3d(((IEntityLiving) this).getPrevServerX(), ((IEntityLiving) this).getPrevServerY(), ((IEntityLiving) this).getPrevServerZ());

        if (mode == KillAura.Resolver.Advantage) {
            if (mc.player.squaredDistanceTo(from) > mc.player.squaredDistanceTo(to)) setPosition(to.x, to.y, to.z);
            else setPosition(from.x, from.y, from.z);
        } else {
            setPosition(to.x, to.y, to.z);
        }
        if (KillAura.target == this)
            Managers.MODULE_MANAGER.getModule(KillAura.class).resolvedBox = getBoundingBox();
    }

    public void releaseResolver() {
        if (backUpY != -999) {
            setPosition(backUpX, backUpY, backUpZ);
            backUpY = -999;
        }
    }
}
