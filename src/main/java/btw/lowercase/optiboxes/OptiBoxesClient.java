package btw.lowercase.optiboxes;

import btw.lowercase.optiboxes.config.OptiBoxesConfig;
import btw.lowercase.optiboxes.config.OptiBoxesConfigScreen;
import btw.lowercase.optiboxes.mixins.RenderPipelinesAccessor;
import btw.lowercase.optiboxes.skybox.OptiFineSkybox;
import btw.lowercase.optiboxes.skybox.SkyboxManager;
import btw.lowercase.optiboxes.utils.CommonUtils;
import btw.lowercase.optiboxes.utils.SkyboxResourceHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.brigadier.Command;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptiBoxesClient implements ClientModInitializer {
    public static OptiBoxesClient INSTANCE;
    public static final String MOD_ID = "optiboxes";

    private static final String OPTIFINE_SKY_PARENT = "optifine/sky";
    private static final String SKY_PATTERN_ENDING = "(?<world>\\w+)/(?<name>\\w+).properties$";
    private static final Pattern OPTIFINE_SKY_PATTERN = Pattern.compile("optifine/sky/" + SKY_PATTERN_ENDING);
    private static final String MCPATCHER_SKY_PARENT = "mcpatcher/sky";
    private static final Pattern MCPATCHER_SKY_PATTERN = Pattern.compile("mcpatcher/sky/" + SKY_PATTERN_ENDING);

    private static final Logger LOGGER = LoggerFactory.getLogger("OptiBoxes");

    public static RenderPipeline getCustomSkyPipeline(BlendFunction blendFunction) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelinesAccessor.getMatricesProjectionSnippet());
        builder.withLocation(id("pipeline/custom_skybox"));
        builder.withVertexShader(id("core/custom_skybox"));
        builder.withFragmentShader(id("core/custom_skybox"));
        builder.withDepthWrite(false);
        builder.withColorWrite(true, false);
        if (blendFunction != null) {
            builder.withBlend(blendFunction);
        }
        builder.withSampler("Sampler0");
        builder.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);
        return builder.build();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        OptiBoxesConfig.load();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SkyboxResourceHelper());
        ClientTickEvents.END_WORLD_TICK.register(SkyboxManager.INSTANCE::tick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(MOD_ID).executes((context) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.schedule(() -> minecraft.setScreen(new OptiBoxesConfigScreen(minecraft.screen, OptiBoxesConfig.instance())));
                    return Command.SINGLE_SUCCESS;
                })));
    }

    public void convert(SkyboxResourceHelper skyboxResourceHelper) {
        if (OptiBoxesConfig.instance().processOptiFine) {
            this.parseSkyboxes(skyboxResourceHelper, OPTIFINE_SKY_PARENT, OPTIFINE_SKY_PATTERN);
        }

        if (OptiBoxesConfig.instance().processMCPatcher) {
            this.parseSkyboxes(skyboxResourceHelper, MCPATCHER_SKY_PARENT, MCPATCHER_SKY_PATTERN);
        }
    }

    private void parseSkyboxes(SkyboxResourceHelper skyboxResourceHelper, String skyParent, Pattern skyPattern) {
        final JsonArray overworldLayers = new JsonArray();
        final JsonArray endLayers = new JsonArray();
        skyboxResourceHelper.searchIn(skyParent)
                .filter(id -> id.getPath().endsWith(".properties"))
                .sorted(Comparator.comparing(ResourceLocation::getPath, (id1, id2) -> {
                    Matcher matcherId1 = skyPattern.matcher(id1);
                    Matcher matcherId2 = skyPattern.matcher(id2);
                    if (matcherId1.find() && matcherId2.find()) {
                        final int id1No = CommonUtils.parseInt(matcherId1.group("name").replace("sky", ""), -1);
                        final int id2No = CommonUtils.parseInt(matcherId2.group("name").replace("sky", ""), -1);
                        if (id1No >= 0 && id2No >= 0) {
                            return id1No - id2No;
                        }
                    }
                    return 0;
                }))
                .forEach(id -> {
                    Matcher matcher = skyPattern.matcher(id.getPath());
                    if (matcher.find()) {
                        final String world = matcher.group("world");
                        final String name = matcher.group("name");
                        if (world == null || name == null) {
                            return;
                        }

                        if (name.equals("moon_phases") || name.equals("sun")) {
                            // TODO/NOTE: Support moon/sun
                            LOGGER.info("Skipping {}, moon_phases/sun aren't supported!", id);
                            return;
                        }

                        final InputStream inputStream = skyboxResourceHelper.getInputStream(id);
                        if (inputStream == null) {
                            LOGGER.error("Error trying to read namespaced identifier: {}", id);
                            return;
                        }

                        final Properties properties = new Properties();
                        try {
                            properties.load(inputStream);
                        } catch (IOException e) {
                            LOGGER.error("Error trying to read properties from: {}", id);
                            return;
                        } finally {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                LOGGER.error("Error trying to close input stream at namespaced identifier: {}", id);
                            }
                        }

                        final JsonObject json = CommonUtils.convertOptiFineSkyProperties(skyboxResourceHelper, properties, id);
                        if (json != null) {
                            if (world.equals("world0")) {
                                overworldLayers.add(json);
                            } else if (world.equals("world1")) {
                                endLayers.add(json);
                            }
                        }
                    }
                });

        if (!overworldLayers.isEmpty()) {
            JsonObject overworldJson = new JsonObject();
            overworldJson.add("layers", overworldLayers);
            overworldJson.addProperty("world", "minecraft:overworld");
            SkyboxManager.INSTANCE.addSkybox(OptiFineSkybox.CODEC.decode(JsonOps.INSTANCE, overworldJson).getOrThrow().getFirst());
        }

        if (!endLayers.isEmpty()) {
            JsonObject endJson = new JsonObject();
            endJson.add("layers", endLayers);
            endJson.addProperty("world", "minecraft:the_end");
            SkyboxManager.INSTANCE.addSkybox(OptiFineSkybox.CODEC.decode(JsonOps.INSTANCE, endJson).getOrThrow().getFirst());
        }
    }

    public Logger getLogger() {
        return LOGGER;
    }
}
