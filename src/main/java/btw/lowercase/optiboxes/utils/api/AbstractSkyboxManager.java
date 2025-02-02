package btw.lowercase.optiboxes.utils.api;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface AbstractSkyboxManager {
    void addSkybox(ResourceLocation resourceLocation, AbstractSkybox abstractSkybox);

    void clearSkyboxes();

    void tick(ClientLevel level);

    List<AbstractSkybox> getActiveSkyboxes();
}