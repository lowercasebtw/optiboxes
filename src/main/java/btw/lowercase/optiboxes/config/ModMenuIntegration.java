package btw.lowercase.optiboxes.config;

import btw.lowercase.optiboxes.OptiBoxesClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> OptiBoxesClient.getConfig().getConfigScreen(parent);
    }
}