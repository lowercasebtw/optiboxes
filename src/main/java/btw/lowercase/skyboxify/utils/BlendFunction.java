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

package btw.lowercase.skyboxify.utils;

//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.BlendFactor;
//? } else >=26.2 {
/*import com.mojang.blaze3d.platform.BlendFactor;
*///? } else >=1.21.6 {
/*import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.platform.DestFactor;
*///? } else {
/*import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor;
*///? }

public record BlendFunction(SrcFactor srcFactor, DstFactor dstFactor) {
    public enum SrcFactor {
        ZERO,
        ONE,
        SRC_ALPHA,
        DST_COLOR,
        ONE_MINUS_DST_COLOR;

        //~ if >=26.2 'SourceFactor' -> 'BlendFactor' {
        public BlendFactor vanilla() {
            return switch (this) {
                case ZERO -> BlendFactor.ZERO;
                case ONE -> BlendFactor.ONE;
                case SRC_ALPHA -> BlendFactor.SRC_ALPHA;
                case DST_COLOR -> BlendFactor.DST_COLOR;
                case ONE_MINUS_DST_COLOR -> BlendFactor.ONE_MINUS_DST_COLOR;
            };
        }
        //~ }
    }

    public enum DstFactor {
        ZERO,
        ONE,
        SRC_COLOR,
        ONE_MINUS_SRC_COLOR,
        ONE_MINUS_SRC_ALPHA;

        //~ if >=26.2 'DestFactor' -> 'BlendFactor' {
        public BlendFactor vanilla() {
            return switch (this) {
                case ZERO -> BlendFactor.ZERO;
                case ONE -> BlendFactor.ONE;
                case SRC_COLOR -> BlendFactor.SRC_COLOR;
                case ONE_MINUS_SRC_COLOR -> BlendFactor.ONE_MINUS_SRC_COLOR;
                case ONE_MINUS_SRC_ALPHA -> BlendFactor.ONE_MINUS_SRC_ALPHA;
            };
        }
        //~ }
    }

    //? >=1.21.6 {
    //~ if >=26.3 'com.mojang.blaze3d.pipeline.BlendFunction' -> 'com.mojang.renderpearl.api.pipeline.BlendFunction' {
    public com.mojang.renderpearl.api.pipeline.BlendFunction vanilla() {
        return new com.mojang.renderpearl.api.pipeline.BlendFunction(this.srcFactor.vanilla(), this.dstFactor.vanilla());
    }
    //? }
    //?}
}
