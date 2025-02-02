package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.utils.api.AbstractSkybox;
import btw.lowercase.optiboxes.utils.api.AbstractSkyboxManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SkyboxManager implements AbstractSkyboxManager {
    public static final SkyboxManager INSTANCE = new SkyboxManager();

    private final List<ResourceLocation> preloadedTextures = new ArrayList<>();
    private final Map<ResourceLocation, AbstractSkybox> skyboxMap = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<ResourceLocation, AbstractSkybox> permanentSkyboxMap = new Object2ObjectLinkedOpenHashMap<>();
    private final List<AbstractSkybox> activeAbstractSkyboxes = new LinkedList<>();

    @Override
    public void addSkybox(ResourceLocation resourceLocation, AbstractSkybox abstractSkybox) {
        Preconditions.checkNotNull(resourceLocation, "Identifier was null");
        Preconditions.checkNotNull(abstractSkybox, "Skybox was null");
        this.skyboxMap.put(resourceLocation, abstractSkybox);
    }

    @Override
    public void clearSkyboxes() {
        this.skyboxMap.clear();
        this.activeAbstractSkyboxes.clear();
        this.preloadedTextures.forEach(Minecraft.getInstance().getTextureManager()::release);
        this.preloadedTextures.clear();
    }

    @Override
    public void tick(ClientLevel level) {
        for (AbstractSkybox abstractSkybox : Iterables.concat(this.skyboxMap.values(), this.permanentSkyboxMap.values())) {
            abstractSkybox.tick(level);
        }

        this.activeAbstractSkyboxes.removeIf(abstractSkybox -> !abstractSkybox.isActive());
        for (AbstractSkybox abstractSkybox : Iterables.concat(this.skyboxMap.values(), this.permanentSkyboxMap.values())) {
            if (!this.activeAbstractSkyboxes.contains(abstractSkybox) && abstractSkybox.isActive()) {
                this.activeAbstractSkyboxes.add(abstractSkybox);
            }
        }
    }

    @Override
    public List<AbstractSkybox> getActiveSkyboxes() {
        return this.activeAbstractSkyboxes;
    }
}