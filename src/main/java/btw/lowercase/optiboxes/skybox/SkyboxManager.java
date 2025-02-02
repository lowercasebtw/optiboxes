package btw.lowercase.optiboxes.skybox;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SkyboxManager {
    public static final SkyboxManager INSTANCE = new SkyboxManager();

    private final Map<ResourceLocation, OptiFineSkybox> skyboxMap = new Object2ObjectLinkedOpenHashMap<>();
    private final List<OptiFineSkybox> activeSkyboxes = new LinkedList<>();
    private final List<ResourceLocation> preloadedTextures = new ArrayList<>();

    public void addSkybox(ResourceLocation resourceLocation, OptiFineSkybox optiFineSkybox) {
        Preconditions.checkNotNull(resourceLocation, "Identifier was null");
        Preconditions.checkNotNull(optiFineSkybox, "Skybox was null");
        this.skyboxMap.put(resourceLocation, optiFineSkybox);
    }

    public void clearSkyboxes() {
        this.skyboxMap.clear();
        this.activeSkyboxes.clear();
        this.preloadedTextures.forEach(Minecraft.getInstance().getTextureManager()::release);
        this.preloadedTextures.clear();
    }

    public void tick(ClientLevel level) {
        for (OptiFineSkybox optiFineSkybox : this.skyboxMap.values()) {
            optiFineSkybox.tick(level);
        }

        this.activeSkyboxes.removeIf(optiFineSkybox -> !optiFineSkybox.isActive());
        for (OptiFineSkybox optiFineSkybox : this.skyboxMap.values()) {
            if (!this.activeSkyboxes.contains(optiFineSkybox) && optiFineSkybox.isActive()) {
                this.activeSkyboxes.add(optiFineSkybox);
            }
        }
    }

    public List<OptiFineSkybox> getActiveSkyboxes() {
        return this.activeSkyboxes;
    }
}