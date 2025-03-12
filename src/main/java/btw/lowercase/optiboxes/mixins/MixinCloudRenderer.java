package btw.lowercase.optiboxes.mixins;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/GpuBuffer;close()V", shift = At.Shift.AFTER))
    private void genBuf(int i, CloudStatus cloudStatus, float f, Vec3 vec3, float g, CallbackInfo ci) {
        GlStateManager._glGenBuffers();
    }
}
