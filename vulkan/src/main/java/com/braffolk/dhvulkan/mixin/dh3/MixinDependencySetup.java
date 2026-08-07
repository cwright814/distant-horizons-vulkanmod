package com.braffolk.dhvulkan.mixin.dh3;

import com.braffolk.dhvulkan.compat.Compat;
import com.braffolk.dhvulkan.config.DhVulkanConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts DH 3.0's DependencySetup.setRenderingApiBindings() to replace
 * the default GL/Blaze3D renderer with our Vulkan renderer.
 *
 * DH 3.0.3 (26.1) uses {@code com.seibel.distanthorizons.common.wrappers.DependencySetup}.
 * Older mixin targets ({@code loaderCommon.fabric...}) never applied, so DH kept
 * binding GlDhRenderApiDefinition and LODs never rendered on VulkanMod.
 *
 * <h3>Rendering API selection logic:</h3>
 * <ul>
 *   <li>{@code AUTO} + VulkanMod installed → use Vulkan rendering (default)</li>
 *   <li>{@code OPEN_GL} → let DH's original code run (OpenGL fallback)</li>
 *   <li>{@code BLAZE_3D} → let DH's original code run (Blaze3D fallback)</li>
 *   <li>{@code forceVulkanRendering=true} in dh-vulkanmod.json → force Vulkan regardless</li>
 * </ul>
 */
@Mixin(value = com.seibel.distanthorizons.common.wrappers.DependencySetup.class, remap = false)
public class MixinDependencySetup {

    private static final Logger LOGGER = LogManager.getLogger("DH-VulkanMod");

    @Inject(method = "setRenderingApiBindings", at = @At("HEAD"), cancellable = true)
    private static void dhvulkan$overrideRenderApi(CallbackInfo ci) {
        if (!Compat.isVulkanModActive()) return;

        // Check if the user explicitly wants Vulkan rendering
        boolean forceVulkan = false;
        try {
            forceVulkan = DhVulkanConfig.get().forceVulkanRendering;
        } catch (Exception e) {
            // Config may not be loadable this early; ignore
        }

        // Read DH's rendering API config to decide whether to use Vulkan.
        // In DH 3.2+ on MC 26.1.2, BLAZE_3D or AUTO are used for modern rendering backends.
        boolean useVulkan = true;
        try {
            Object apiEnum = com.seibel.distanthorizons.core.config.Config
                    .Client.Advanced.Graphics.Experimental.renderingEngine.get();
            String apiName = apiEnum.toString();

            if ("OPEN_GL".equals(apiName)) {
                // User explicitly requested legacy OPEN_GL renderer
                useVulkan = false;
            }
        } catch (Exception e) {
            LOGGER.debug("[DH-Vulkan] Could not read DH renderingEngine config, defaulting to Vulkan: {}",
                    e.getMessage());
            useVulkan = true;
        }

        if (forceVulkan) {
            useVulkan = true;
        }

        if (!useVulkan) {
            // User explicitly chose OPEN_GL — let DH use the legacy OpenGL API
            LOGGER.info("[DH-VulkanMod] DH rendering API set to OPEN_GL — DH-VulkanMod Vulkan renderer disabled. " +
                    "Set to BLAZE_3D/AUTO or enable forceVulkanRendering in dh-vulkanmod.json to use Vulkan.");
            return;
        }

        // Bind Vulkan renderer
        com.braffolk.dhvulkan.api.ApiDhIntegration integration =
                com.braffolk.dhvulkan.api.ApiDhIntegration.getInstance();
        if (integration == null) {
            // DH can call this before our ClientModInitializer (DH loads first as a dependency).
            com.braffolk.dhvulkan.core.VulkanBackend backend =
                    new com.braffolk.dhvulkan.core.VulkanRenderEngine();
            integration = new com.braffolk.dhvulkan.api.ApiDhIntegration();
            integration.initialize(backend);
            com.braffolk.dhvulkan.DhVulkanModEntrypoint.setActiveIntegration(integration);
        }

        integration.bindRenderApi();
        LOGGER.info("[DH-VulkanMod] Vulkan rendering API bound successfully.");
        ci.cancel();
    }
}