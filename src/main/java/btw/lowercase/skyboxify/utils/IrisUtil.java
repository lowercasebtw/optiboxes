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

import lombok.experimental.UtilityClass;

//? >=1.21.6 {
import btw.lowercase.skyboxify.api.SkyboxifyImpl;

//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? } else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///? }

import java.lang.reflect.Method;
import java.util.Arrays;
//?}

@UtilityClass
public final class IrisUtil {
    //? >=1.21.6 {
    private static Object IRIS_INSTANCE = null;
    private static Method IRIS_ASSIGN_PIPELINE_METHOD = null;

    static {
        try {
            // API
			final Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            IRIS_INSTANCE = irisApiClass.getMethod("getInstance").invoke(null);

            // Enums
            @SuppressWarnings("rawtypes")
			final Class<? extends Enum> irisProgramEnum = Class.forName("net.irisshaders.iris.api.v0.IrisProgram").asSubclass(Enum.class);
            Arrays.stream(IrisPipeline.VALUES).forEach((program) -> program.initialize(irisProgramEnum));

            // Methods
            IRIS_ASSIGN_PIPELINE_METHOD = IRIS_INSTANCE.getClass().getMethod("assignPipeline", RenderPipeline.class, irisProgramEnum);
        } catch (final Exception exception) {
			if (SkyboxifyImpl.config().debug) {
				exception.printStackTrace();
			}
        }
    }

    public static void assignPipeline(final RenderPipeline pipeline, final IrisPipeline program) {
        try {
            IRIS_ASSIGN_PIPELINE_METHOD.invoke(IRIS_INSTANCE, pipeline, program.internal());
        } catch (Exception ignored) {
        }
    }
    //?}
}
