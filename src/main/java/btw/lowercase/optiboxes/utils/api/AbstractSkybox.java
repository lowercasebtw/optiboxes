package btw.lowercase.optiboxes.utils.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;

public interface AbstractSkybox {
    // TODO/NOTE: Not needed anymore?
    default int getLayer() {
        return 0;
    }

    void render(SkyRenderer skyRenderer, PoseStack poseStack, float tickDelta, Camera camera, MultiBufferSource.BufferSource bufferSource, FogParameters fogParameters);

    void tick(ClientLevel level);

    boolean isActive();
}