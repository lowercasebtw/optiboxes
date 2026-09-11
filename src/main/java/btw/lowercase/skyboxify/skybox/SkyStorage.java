/**
 * Skyboxify
 * A skybox mod that allows you to use OptiFine skies in Fabric 1.21+
 * <p>
 * Copyright (C) 2025-2026 lowercasebtw
 * Copyright (C) 2025-2026 Contributors to the project retain their copyright
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * "MINECRAFT" LINKING EXCEPTION TO THE GPL
 */

package btw.lowercase.skyboxify.skybox;

import btw.lowercase.skyboxify.Skyboxify;
import btw.lowercase.skyboxify.utils.*;
import com.mojang.blaze3d.vertex.*;

//? >=1.21.6 {
//? <=26.1
//import btw.lowercase.skyboxify.mixins.RenderPipelinesAccessor;
//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? } else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///? }
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
//?}

public final class SkyStorage {
    //? >=1.21.6 {
    private static final Map<BlendFunction, RenderPipeline> renderPipelineCache = new Object2ObjectOpenHashMap<>();

    public static RenderPipeline calculateSkyboxPipeline(final @org.jetbrains.annotations.Nullable BlendFunction blendFunction) {
        if (renderPipelineCache.containsKey(blendFunction)) {
            return renderPipelineCache.get(blendFunction);
        } else {
            final RenderPipeline.Builder builder = RenderPipeline.builder(
                    //? <=26.1
                    //RenderPipelinesAccessor.skyboxify$getMatricesProjectionSnippet()
            );
            builder.withLocation(Skyboxify.locationOrNull("pipeline/custom_skybox"));
            builder.withVertexShader(SkyboxResourceHelper.CUSTOM_SKYBOX_LOCATION);
            builder.withFragmentShader(SkyboxResourceHelper.CUSTOM_SKYBOX_LOCATION);

            //? >=26.1 {
            //~ if >=26.3 'com.mojang.blaze3d.pipeline' -> 'com.mojang.renderpearl.api.pipeline' {
            final int writeColor = com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_COLOR;
            final com.mojang.renderpearl.api.pipeline.BlendFunction vanillaBlendFunction = blendFunction == null ? null : blendFunction.vanilla();
            builder.withColorTargetState(new com.mojang.renderpearl.api.pipeline.ColorTargetState(
                    java.util.Optional.ofNullable(vanillaBlendFunction),
                    //? >=26.2 {
                    //~ if >=26.3 'com.mojang.blaze3d.GpuFormat' -> 'com.mojang.renderpearl.api.GpuFormat' {
                    com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM, writeColor
                    //? }
                    //? } else {
                    /*writeColor
                    *///? }
            ));
            //? }
            //? } else {
            /*builder.withDepthWrite(false);
            builder.withColorWrite(true, false);
            if (blendFunction != null) {
                builder.withBlend(blendFunction.vanilla());
            }
            *///? }

            //? >=26.2 {
            //? >=26.3 {
            builder.withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.DYNAMIC_TRANSFORMS);
            builder.withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.PROJECTION);
            //? } else {
            /*builder.withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.MATRICES_PROJECTION);
            *///? }
            builder.withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.SAMPLER0);
            //? } else {
            /*builder.withSampler("Sampler0");
            *///? }

            //? >=26.2 {
            builder.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX);
            //~ if >=26.3 'com.mojang.blaze3d.PrimitiveTopology' -> 'com.mojang.renderpearl.api.pipeline.PrimitiveTopology' {
            builder.withPrimitiveTopology(com.mojang.renderpearl.api.pipeline.PrimitiveTopology.QUADS);
            //? }
            //? } else {
            /*builder.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);
            *///? }

            final RenderPipeline pipeline = builder.build();
            renderPipelineCache.put(blendFunction, pipeline);
            IrisUtil.assignPipeline(pipeline, IrisPipeline.SKY_TEXTURED);
            return pipeline;
        }
    }
    //?}
}