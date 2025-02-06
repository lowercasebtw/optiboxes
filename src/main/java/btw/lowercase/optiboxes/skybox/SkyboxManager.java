package btw.lowercase.optiboxes.skybox;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SkyboxManager {
    public static final SkyboxManager INSTANCE = new SkyboxManager();

    private final List<OptiFineSkybox> loadedSkyboxes = new ArrayList<>();
    private final List<OptiFineSkybox> activeSkyboxes = new LinkedList<>();
    private final List<ResourceLocation> preloadedTextures = new ArrayList<>();

    public void addSkybox(OptiFineSkybox optiFineSkybox) {
        Preconditions.checkNotNull(optiFineSkybox, "Skybox was null");
        this.loadedSkyboxes.add(optiFineSkybox);
    }

    public void clearSkyboxes() {
        this.loadedSkyboxes.clear();
        this.activeSkyboxes.clear();
        this.preloadedTextures.forEach(Minecraft.getInstance().getTextureManager()::release);
        this.preloadedTextures.clear();
    }

    public void tick(ClientLevel level) {
        for (OptiFineSkybox optiFineSkybox : this.loadedSkyboxes) {
            optiFineSkybox.tick(level);
        }

        this.activeSkyboxes.removeIf(optiFineSkybox -> !optiFineSkybox.isActive());
        for (OptiFineSkybox optiFineSkybox : this.loadedSkyboxes) {
            if (!this.activeSkyboxes.contains(optiFineSkybox) && optiFineSkybox.isActive()) {
                this.activeSkyboxes.add(optiFineSkybox);
            }
        }
    }

    public List<OptiFineSkybox> getActiveSkyboxes() {
        return this.activeSkyboxes;
    }
}