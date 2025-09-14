package pon.main.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

import pon.main.Main;
import pon.main.events.impl.*;
import pon.main.modules.Parent;
import pon.main.utils.math.FrameRateCounter;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    @Final
    private Window window;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Unique
    private String[] shittyServers = {
        "mineblaze",
        "musteryworld",
        "dexland",
        "masedworld",
        "vimeworld",
        "hypemc",
        "vimemc"
    };

    @Inject(method = "tick", at = @At("HEAD"))
    void preTickHook(CallbackInfo ci) {
        Main.EVENT_BUS.post(new EventTick());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    void postTickHook(CallbackInfo ci) {
        Main.EVENT_BUS.post(new EventPostTick());
    }


    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void doItemPickHook(CallbackInfo ci) {
//        if (ModuleManager.middleClick.isEnabled() && ModuleManager.middleClick.antiPickUp.getValue())
//            ci.cancel();
    }

    @Inject(method = "setOverlay", at = @At("HEAD"))
    public void setOverlay(Overlay overlay, CallbackInfo ci) {
        //   if (overlay instanceof SplashOverlay)
        //  Managers.SHADER.reloadShaders();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void setScreenHookPre(Screen screen, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;
        EventScreen event = new EventScreen(screen);
        Main.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void setScreenHookPost(Screen screen, CallbackInfo ci) {
        if (Parent.fullNullCheck()) return;
        if (screen instanceof MultiplayerScreen mScreen && mScreen.getServerList() != null) { // ModuleManager.antiServerAdd.isEnabled() &&
            for (int i = 0; i < mScreen.getServerList().size(); i++) {
                ServerInfo info = mScreen.getServerList().get(i);
                for (String server : shittyServers) {
                    if (info != null && info.address != null && info.address.toLowerCase().contains(server.toLowerCase())) {
                        mScreen.getServerList().remove(info);
                        mScreen.getServerList().saveFile();
                        setScreen(screen);
                        break;
                    }
                }
            }
        }
    }

//    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"))
//    private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
//        if (GLFW.glfwGetPlatform() == 393218) {
//            MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
//            return;
//        }
//
//        setWindowIcon(Main.class.getResourceAsStream("/icon.png"), Main.class.getResourceAsStream("/icon.png"));
//    }

    public void setWindowIcon(InputStream img16x16, InputStream img32x32) {
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            GLFWImage.Buffer buffer = GLFWImage.malloc(2, memorystack);
            List<InputStream> imgList = List.of(img16x16, img32x32);
            List<ByteBuffer> buffers = new ArrayList<>();

            for (int i = 0; i < imgList.size(); i++) {
                NativeImage nativeImage = NativeImage.read(imgList.get(i));
                ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);

                bytebuffer.asIntBuffer().put(nativeImage.copyPixelsArgb());
                buffer.position(i);
                buffer.width(nativeImage.getWidth());
                buffer.height(nativeImage.getHeight());
                buffer.pixels(bytebuffer);

                buffers.add(bytebuffer);
            }

            try {
                if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
                    GLFW.glfwSetWindowIcon(MinecraftClient.getInstance().getWindow().getHandle(), buffer);
                }
            } catch (Exception ignored) {
            }
            buffers.forEach(MemoryUtil::memFree);
        } catch (IOException ignored) {
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void doAttackHook(CallbackInfoReturnable<Boolean> cir) {
        final EventAttack event = new EventAttack(null, true);
        Main.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void handleBlockBreakingHook(boolean breaking, CallbackInfo ci) {
        EventHandleBlockBreaking event = new EventHandleBlockBreaking();
        Main.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(boolean tick, CallbackInfo ci) {
        FrameRateCounter.INSTANCE.recordFrame();
        Main.EVENT_BUS.post(new EventOnRender());
    }
}
