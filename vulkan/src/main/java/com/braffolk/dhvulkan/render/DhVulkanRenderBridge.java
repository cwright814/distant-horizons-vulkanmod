package com.braffolk.dhvulkan.render;

import com.braffolk.dhvulkan.compat.Compat;
import com.seibel.distanthorizons.common.wrappers.McObjectConverter;
import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper;
import com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.api.internal.rendering.DhRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.vulkanmod.vulkan.VRenderSystem;
import org.joml.Matrix4fc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * VulkanMod redirects {@code ChunkSectionsToRender.renderGroup} to
 * {@code LevelRenderer.renderSectionLayer}, so DH's own chunk-section mixin never runs.
 * This mirrors DH's {@code MixinChunkSectionsToRender.renderDeferredLayerHead}.
 */
public final class DhVulkanRenderBridge {

    private static final Logger LOGGER = LogManager.getLogger("DH-VulkanMod");
    private static boolean loggedMatrixFallback;
    private static boolean loggedFirstRender;
    private static boolean loggedSkipRender;

    private DhVulkanRenderBridge() {}

    public static void onTerrainLayerGroup(ChunkSectionLayerGroup group) {
        onTerrainLayerGroup(group, null);
    }

    public static void onTerrainLayerGroup(ChunkSectionLayerGroup group, Matrix4fc levelModelView) {
        if (!Compat.isVulkanModActive()) {
            return;
        }

        if (Minecraft.getInstance().level == null) {
            return;
        }

        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = Minecraft.getInstance().level.dimension();
        if (dim == net.minecraft.world.level.Level.NETHER || dim == net.minecraft.world.level.Level.END) {
            return;
        }

        DhRenderState state = ClientApi.RENDER_STATE;
        state.clientLevelWrapper = ClientLevelWrapper.getWrapperIfDifferent(
                state.clientLevelWrapper,
                Minecraft.getInstance().level);

        ensureRenderMatrices(state, levelModelView);

        try {
            state.canRenderOrThrow();
        } catch (RuntimeException e) {
            if (!loggedSkipRender) {
                loggedSkipRender = true;
                LOGGER.debug("[DH-VulkanMod] Skipping LOD render ({}): {}", group, e.getMessage());
            }
            return;
        }

        if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            ClientApi.INSTANCE.renderDeferredLodsForShaders();
        } else if (group == ChunkSectionLayerGroup.OPAQUE) {
            if (!loggedFirstRender) {
                loggedFirstRender = true;
                LOGGER.debug("[DH-VulkanMod] LOD render hooked at VulkanMod terrain layer ({}).", group);
            }
            ClientApi.INSTANCE.renderLods();
        }
    }

    /**
     * DH normally receives matrices from {@code MixinLevelRenderer} / {@code MixinGameRenderer}.
     * Under VulkanMod those injects may not run before terrain; fall back to VRenderSystem.
     */
    private static void ensureRenderMatrices(DhRenderState state, Matrix4fc levelModelView) {
        state.mcProjectionMatrix = McObjectConverter.convert(VRenderSystem.projection);
        if (!loggedMatrixFallback) {
            loggedMatrixFallback = true;
            LOGGER.debug("[DH-VulkanMod] Using VRenderSystem matrices for DH render state.");
        }
        if (levelModelView != null) {
            state.mcModelViewMatrix = McObjectConverter.convert(levelModelView);
        } else {
            state.mcModelViewMatrix = McObjectConverter.convert(VRenderSystem.modelView);
        }
        state.partialTickTime = MinecraftRenderWrapper.INSTANCE.getPartialTickTime();
    }
}
