package com.braffolk.dhvulkan.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DhVulkanModMenuApi implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> buildConfigScreen(parent);
    }

    private Screen buildConfigScreen(Screen parent) {
        DhVulkanConfig cfg = DhVulkanConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("DH-VulkanMod Configuration"));

        DhVulkanConfig.FresnelPreset initPreset = cfg.fresnelPreset;
        float initBaseY = cfg.fresnelHeightBaseY;
        float initTargetY = cfg.fresnelHeightTargetY;
        float initBaseM = cfg.fresnelHeightBaseMult;
        float initTargetM = cfg.fresnelHeightTargetMult;
        float initMinM = cfg.fresnelHeightMinMult;
        float initMaxM = cfg.fresnelHeightMaxMult;

        builder.setSavingRunnable(() -> {
            boolean presetChanged = cfg.fresnelPreset != initPreset;
            boolean valuesTweaked =
                    cfg.fresnelHeightBaseY != initBaseY ||
                    cfg.fresnelHeightTargetY != initTargetY ||
                    cfg.fresnelHeightBaseMult != initBaseM ||
                    cfg.fresnelHeightTargetMult != initTargetM ||
                    cfg.fresnelHeightMinMult != initMinM ||
                    cfg.fresnelHeightMaxMult != initMaxM;

            if (presetChanged && !valuesTweaked) {
                // Only the preset was changed, apply the preset values.
                cfg.applyPreset(cfg.fresnelPreset);
            } else if (valuesTweaked) {
                // Custom tweaks override everything.
                cfg.fresnelPreset = DhVulkanConfig.FresnelPreset.CUSTOM;
            }

            cfg.save();
        });

        ConfigCategory fresnelCategory = builder.getOrCreateCategory(Component.literal("Water Fresnel"));
        ConfigCategory generalCategory = builder.getOrCreateCategory(Component.literal("General"));
        ConfigCategory sunriseCategory = builder.getOrCreateCategory(Component.literal("Sunrise Curve"));
        ConfigCategory sunsetCategory = builder.getOrCreateCategory(Component.literal("Sunset Curve"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Water Fresnel
        fresnelCategory.addEntry(entryBuilder.startEnumSelector(Component.literal("Preset"), DhVulkanConfig.FresnelPreset.class, cfg.fresnelPreset)
                .setDefaultValue(DhVulkanConfig.FresnelPreset.CHUNKS_12)
                .setEnumNameProvider(preset -> Component.literal(((DhVulkanConfig.FresnelPreset) preset).getDisplayName()))
                .setTooltip(Component.literal("Select a preset that matches your real chunk render distance (or the server's render distance, if using Bobby DH Compat on Auto). These presets provide optimal water reflection blending for their respective chunk distances up to 300 blocks high. Manually tweaking any value below will automatically switch this to Custom."))
                .setSaveConsumer(newValue -> cfg.fresnelPreset = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Base Y Level"), cfg.fresnelHeightBaseY)
                .setDefaultValue(70.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightBaseY = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Target Y Level"), cfg.fresnelHeightTargetY)
                .setDefaultValue(105.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightTargetY = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Base Multiplier"), cfg.fresnelHeightBaseMult)
                .setDefaultValue(2.4f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightBaseMult = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Target Multiplier"), cfg.fresnelHeightTargetMult)
                .setDefaultValue(1.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightTargetMult = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Min Multiplier"), cfg.fresnelHeightMinMult)
                .setDefaultValue(0.2f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightMinMult = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Max Multiplier"), cfg.fresnelHeightMaxMult)
                .setDefaultValue(3.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightMaxMult = newValue)
                .build());

        // General
        generalCategory.addEntry(entryBuilder.startBooleanToggle(Component.literal("Force Vulkan Rendering"), cfg.forceVulkanRendering)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> cfg.forceVulkanRendering = newValue)
                .build());

        // Sunrise (22000 to 24000)
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            float defaultVal = (i == 0) ? 0.0f : ((i == 1) ? 0.55f : ((i == 2) ? 0.85f : ((i == 3) ? 0.96f : 1.0f)));
            sunriseCategory.addEntry(entryBuilder.startFloatField(Component.literal("Sunrise Step " + (i + 1)), cfg.sunriseCurve[i])
                    .setDefaultValue(defaultVal)
                    .setMin(0.0f)
                    .setMax(1.0f)
                    .setSaveConsumer(newValue -> cfg.sunriseCurve[finalI] = newValue)
                    .build());
        }

        // Sunset (12000 to 14000)
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            float defaultVal = (i == 0) ? 1.0f : ((i == 1) ? 0.96f : ((i == 2) ? 0.85f : ((i == 3) ? 0.55f : 0.0f)));
            sunsetCategory.addEntry(entryBuilder.startFloatField(Component.literal("Sunset Step " + (i + 1)), cfg.sunsetCurve[i])
                    .setDefaultValue(defaultVal)
                    .setMin(0.0f)
                    .setMax(1.0f)
                    .setSaveConsumer(newValue -> cfg.sunsetCurve[finalI] = newValue)
                    .build());
        }

        return builder.build();
    }
}
