package btw.lowercase.optiboxes.utils;

import com.mojang.blaze3d.platform.GlStateManager;

public record BlendFunction(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
}
