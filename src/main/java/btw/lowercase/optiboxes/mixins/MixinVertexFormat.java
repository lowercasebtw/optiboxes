package btw.lowercase.optiboxes.mixins;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(value = VertexFormat.class, remap = false)
public abstract class MixinVertexFormat {
    @Inject(method = "uploadImmediateVertexBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/GpuBuffer;close()V", shift = At.Shift.AFTER))
    private void vertex(ByteBuffer byteBuffer, CallbackInfoReturnable<GpuBuffer> cir) {
        GlStateManager._glGenBuffers();
    }

    @Inject(method = "uploadImmediateIndexBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/GpuBuffer;close()V", shift = At.Shift.AFTER))
    private void index(ByteBuffer byteBuffer, CallbackInfoReturnable<GpuBuffer> cir) {
        GlStateManager._glGenBuffers();
    }
}