package com.braffolk.dhvulkan.mixin.beryl;

import net.vulkanmod.render.shader.ShaderLoadUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Patches Beryl's shader source code at load time to prevent Beryl from creating
 * a solid wall of render distance fog at vanilla view distance (e.g. 16 chunks / 256 blocks),
 * allowing Distant Horizons LODs to be rendered smoothly behind real chunks.
 */
@Mixin(ShaderLoadUtil.class)
public abstract class MixinShaderLoadUtil {

    private static final Logger LOGGER = LogManager.getLogger("DH-VulkanMod-Beryl");

    @Inject(
            method = "getShaderSource(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void dhvulkan$patchBerylFogShader(String path, String shaderName, CallbackInfoReturnable<String> cir) {
        String source = cir.getReturnValue();
        if (source == null) return;

        if ("fog2.glsl".equals(shaderName) || (path != null && path.contains("fog2.glsl"))) {
            if (source.contains("smoothstep(0.8*fogEnd, 1.0*fogEnd, fragDistance)")) {
                // Remove the 16-chunk render distance fog cap so terrain does not turn solid fog at 16 chunks
                source = source.replace(
                        "smoothstep(0.8*fogEnd, 1.0*fogEnd, fragDistance)",
                        "0.0"
                );
                LOGGER.info("[DH-Vulkan-Beryl] Successfully patched fog2.glsl: removed 16-chunk render distance fog cutoff for DH LOD visibility!");
                cir.setReturnValue(source);
            }
        }
    }
}
