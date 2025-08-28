package pon.main.injection;

import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pon.main.Main;
import pon.main.events.impl.PacketEvent;
import pon.main.modules.Parent;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

//    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
//    private void exceptionCaughtHook(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
//        if (ModuleManager.antiPacketException.isEnabled()) {
//            ModuleManager.antiPacketException.sendChatMessage(t.getMessage());
//            ci.cancel();
//        }
//    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if (Parent.fullNullCheck()) return;
        if (packet instanceof BundleS2CPacket packs) {
            packs.getPackets().forEach(p -> {
                PacketEvent.Receive event = new PacketEvent.Receive(p);
                Main.EVENT_BUS.post(event);
                if (event.isCancelled()) {
                    info.cancel();
                }
            });
        } else {
            PacketEvent.Receive event = new PacketEvent.Receive(packet);
            Main.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }


    @Inject(method = "handlePacket", at = @At("TAIL"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacketPost(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if(Parent.fullNullCheck()) return;
        if (packet instanceof BundleS2CPacket packs) {
            packs.getPackets().forEach(p -> {
                PacketEvent.ReceivePost event = new PacketEvent.ReceivePost(p);
                Main.EVENT_BUS.post(event);
                if (event.isCancelled()) {
                    info.cancel();
                }
            });
        } else {
            PacketEvent.ReceivePost event = new PacketEvent.ReceivePost(packet);
            Main.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"),cancellable = true)
    private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
        if(Parent.fullNullCheck()) return;
//        if (ThunderHack.core.silentPackets.contains(packet)) {
//            ThunderHack.core.silentPackets.remove(packet);
//            return;
//        }

        PacketEvent.Send event = new PacketEvent.Send(packet);
        Main.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("RETURN"),cancellable = true)
    private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
        if(Parent.fullNullCheck()) return;
        PacketEvent.SendPost event = new PacketEvent.SendPost(packet);
        Main.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }
}
