package btw.lowercase.optiboxes.skybox;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import com.google.common.base.Preconditions;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class SkyboxManager {
    public static final SkyboxManager INSTANCE = new SkyboxManager();

    private final List<OptiFineSkybox> loadedSkyboxes = new ArrayList<>();
    private final List<OptiFineSkybox> activeSkyboxes = new LinkedList<>();

    public void addSkybox(OptiFineSkybox optiFineSkybox) {
        Preconditions.checkNotNull(optiFineSkybox, "Skybox was null");
        this.loadedSkyboxes.add(optiFineSkybox);
    }

    public void clearSkyboxes() {
        this.loadedSkyboxes.clear();
        this.activeSkyboxes.clear();
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

    public boolean isEnabled(ClientLevel level) {
        return OptiBoxesConfig.instance().enabled && !activeSkyboxes.isEmpty() && level != null;
    }

    public List<OptiFineSkybox> getActiveSkyboxes() {
        return this.activeSkyboxes;
    }
}