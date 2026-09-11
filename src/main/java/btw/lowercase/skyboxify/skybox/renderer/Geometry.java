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

import btw.lowercase.skyboxify.skybox.SkyPart;
import btw.lowercase.skyboxify.skybox.impl.components.UV;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
//? } else >=26.2 {
/*import com.mojang.blaze3d.PrimitiveTopology;
*///? } else {
/*import com.mojang.blaze3d.vertex.VertexFormat;
 *///? }

public interface Geometry extends AutoCloseable {
    StaticGeometry DEFAULT = StaticGeometry.create(
            DefaultVertexFormat.POSITION_TEX,
            //? >=26.2 {
            PrimitiveTopology.QUADS,
            //? } else {
            /*VertexFormat.Mode.QUADS,
             *///? }
            SkyPart.COUNT * 4,
            vertexConsumer -> {
                for (final SkyPart part : SkyPart.VALUES) {
                    final UV uv = part.getUv();
                    final float size = 100.0F; // Bigger the value, the less bad view-bobbing affects it, but it starts clipping which is bad
                    vertexConsumer.addVertex(part.getRotationMatrix(), -size, -size, -size).setUv(uv.minU(), uv.minV());
                    vertexConsumer.addVertex(part.getRotationMatrix(), -size, -size, size).setUv(uv.minU(), uv.maxV());
                    vertexConsumer.addVertex(part.getRotationMatrix(), size, -size, size).setUv(uv.maxU(), uv.maxV());
                    vertexConsumer.addVertex(part.getRotationMatrix(), size, -size, -size).setUv(uv.maxU(), uv.minV());
                }
            });

    boolean isClosed();

    void close();
}
