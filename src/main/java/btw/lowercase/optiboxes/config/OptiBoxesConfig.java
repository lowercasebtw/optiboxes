package btw.lowercase.optiboxes.config;

import btw.lowercase.lightconfig.lib.Config;
import btw.lowercase.lightconfig.lib.field.BooleanConfigField;
import btw.lowercase.optiboxes.OptiBoxesClient;

import java.nio.file.Path;

public class OptiBoxesConfig extends Config {
    public final BooleanConfigField enabled = this.booleanFieldOf("enabled", true);
    public final BooleanConfigField processOptiFine = this.booleanFieldOf("processOptiFine", true);
    public final BooleanConfigField processMCPatcher = this.booleanFieldOf("processMCPatcher", false);
    public final BooleanConfigField renderSunMoon = this.booleanFieldOf("renderSunMoon", true);
    public final BooleanConfigField renderStars = this.booleanFieldOf("renderStars", true);
    public final BooleanConfigField showOverworldForUnknownDimension = this.booleanFieldOf("showOverworldForUnknownDimension", true);

    public OptiBoxesConfig(Path path) {
        super(OptiBoxesClient.MOD_ID, path);
    }
}