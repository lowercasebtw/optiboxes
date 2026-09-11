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

package btw.lowercase.skyboxify.skybox.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.resources.Identifier;

import java.util.*;

//? >=1.21.6 {
//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? } else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///? }
//? } else {
/*import btw.lowercase.skyboxify.utils.BlendFunction;
*///? }

public abstract class FeatureRenderer<T extends FeatureRenderer.Submit> {
    protected final RenderTarget renderTarget;
    protected final List<T> submits = new ArrayList<>();

    protected FeatureRenderer(final RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    protected abstract T createSubmit(final Pipeline pipeline, final Geometry geometry, final RenderUniforms uniforms, final Identifier location);

    public void submit(final Pipeline pipeline, final Geometry geometry, final RenderUniforms uniforms, final Identifier location) {
        if (geometry != null && !geometry.isClosed()) {
            this.submits.add(createSubmit(pipeline, geometry, uniforms, location));
        } else {
            throw new IllegalStateException("Cannot call submit with " + (geometry == null ? "null" : "closed") + " geometry!");
        }
    }

    // Override and implement your rendering
    public void endFrame() {
        this.clear();
    }

    public void clear() {
        this.submits.clear();
    }

    public record Pipeline(
        //? >=1.21.6 {
        RenderPipeline pipeline
        //? } else {
        /*BlendFunction blendFunction
        *///? }
    ) {
    }

    protected interface Submit {
    }
}
