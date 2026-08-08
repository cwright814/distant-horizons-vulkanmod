package com.braffolk.dhvulkan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;

/**
 * Simple JSON config for the DH-VulkanMod extension.
 * Saved to {@code config/dh-vulkanmod.json}.
 */
public class DhVulkanConfig {

    private static final Logger LOGGER = LogManager.getLogger("DH-VulkanMod");

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("dh-vulkanmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static DhVulkanConfig INSTANCE;

    // ---- Config Fields ----

    /**
     * Debug render mode for the composite shader.
     * 0=normal, 1=DH depth, 2=SSAO, 3=fog alpha, 4=fog color, 5=normals, 6=MC depth
     * Hot-reloadable: edit dh-vulkanmod.json while the game is running.
     */
    public int vulkanRenderMode = 0;

    /**
     * Force Vulkan rendering regardless of DH's "Rendering API" config setting.
     * When true, DH-VulkanMod always binds its Vulkan renderer, even if DH's
     * renderingApi is set to OPEN_GL or BLAZE_3D.
     *
     * Use this if DH's config doesn't include a Vulkan option and you want to
     * ensure Vulkan is always used when VulkanMod is installed.
     *
     * Default: false (respects DH's renderingApi setting; AUTO uses Vulkan).
     */
    public boolean forceVulkanRendering = false;

    /**
     * Sunrise transition curve (ticks 22000 to 24000)
     */
    public float[] sunriseCurve = new float[]{0.0f, 0.55f, 0.85f, 0.96f, 1.0f};

    /**
     * Sunset transition curve (ticks 12000 to 14000)
     */
    public float[] sunsetCurve = new float[]{1.0f, 0.96f, 0.85f, 0.55f, 0.0f};

    public enum FresnelPreset {
        CHUNKS_12("12 Chunks"),
        CHUNKS_20("20 Chunks"),
        CHUNKS_32("32 Chunks"),
        CUSTOM("Custom");

        private final String displayName;
        FresnelPreset(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }

    /** Fresnel height scaling values */
    public FresnelPreset fresnelPreset = FresnelPreset.CHUNKS_12;
    public float fresnelHeightBaseY = 70.0f;
    public float fresnelHeightTargetY = 105.0f;
    public float fresnelHeightBaseMult = 2.4f;
    public float fresnelHeightTargetMult = 1.0f;
    public float fresnelHeightMinMult = 0.2f;
    public float fresnelHeightMaxMult = 3.0f;

    public void applyPreset(FresnelPreset preset) {
        if (preset == FresnelPreset.CUSTOM) return;

        // For now, all presets share the same default values.
        // We prep the structure so they can be changed easily later.
        switch (preset) {
            case CHUNKS_20:
                this.fresnelHeightBaseY = 80.0f;
                this.fresnelHeightTargetY = 130.0f;
                this.fresnelHeightBaseMult = 2.2f;
                this.fresnelHeightTargetMult = 1.0f;
                this.fresnelHeightMinMult = 0.4f;
                this.fresnelHeightMaxMult = 3.0f;
                break;
            case CHUNKS_32:
                this.fresnelHeightBaseY = 100.0f;
                this.fresnelHeightTargetY = 180.0f;
                this.fresnelHeightBaseMult = 2.25f;
                this.fresnelHeightTargetMult = 1.0f;
                this.fresnelHeightMinMult = 0.7f;
                this.fresnelHeightMaxMult = 3.0f;
                break;
            case CHUNKS_12:
            default:
                this.fresnelHeightBaseY = 70.0f;
                this.fresnelHeightTargetY = 105.0f;
                this.fresnelHeightBaseMult = 2.4f;
                this.fresnelHeightTargetMult = 1.0f;
                this.fresnelHeightMinMult = 0.2f;
                this.fresnelHeightMaxMult = 3.0f;
                break;
        }
        this.fresnelPreset = preset;
    }

    // ---- Load / Save ----

    public static DhVulkanConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static long lastReloadTime = 0;

    /** Re-read config from disk, throttled to once per second. */
    public static void reload() {
        long now = System.currentTimeMillis();
        if (now - lastReloadTime < 1000)
            return;
        lastReloadTime = now;
        INSTANCE = load();
    }

    private static DhVulkanConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                DhVulkanConfig config = GSON.fromJson(json, DhVulkanConfig.class);
                if (config != null)
                    return config;
            } catch (Exception e) {
                LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
            }
        }
        // Create default config
        DhVulkanConfig config = new DhVulkanConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("Failed to save config: {}", e.getMessage());
        }
    }
}