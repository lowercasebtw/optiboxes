package btw.lowercase.optiboxes.utils.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface AbstractSkyboxManager {
    void addSkybox(ResourceLocation resourceLocation, AbstractSkybox abstractSkybox);

    void clearSkyboxes();

    List<AbstractSkybox> getActiveSkyboxes();

    void renderSkyboxes(SkyRenderer skyRenderer, PoseStack poseStack, float tickDelta, Camera camera, MultiBufferSource.BufferSource bufferSource, FogParameters fogParameters);

    void tick(ClientLevel level);
}