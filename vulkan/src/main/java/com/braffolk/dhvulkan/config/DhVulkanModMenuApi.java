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

        builder.setSavingRunnable(() -> {
            cfg.save();
        });

        ConfigCategory sunriseCategory = builder.getOrCreateCategory(Component.literal("Sunrise Curve"));
        ConfigCategory sunsetCategory = builder.getOrCreateCategory(Component.literal("Sunset Curve"));
        ConfigCategory generalCategory = builder.getOrCreateCategory(Component.literal("General"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // General
        generalCategory.addEntry(entryBuilder.startBooleanToggle(Component.literal("Force Vulkan Rendering"), cfg.forceVulkanRendering)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> cfg.forceVulkanRendering = newValue)
                .build());

        // Sunrise (22000 to 24000)
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            float defaultVal = (i == 0) ? 0.0f : ((i == 1) ? 0.5f : ((i == 2) ? 0.75f : ((i == 3) ? 0.9f : 1.0f)));
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
            float defaultVal = (i == 0) ? 1.0f : ((i == 1) ? 0.9f : ((i == 2) ? 0.75f : ((i == 3) ? 0.5f : 0.0f)));
            sunsetCategory.addEntry(entryBuilder.startFloatField(Component.literal("Sunset Step " + (i + 1)), cfg.sunsetCurve[i])
                    .setDefaultValue(defaultVal)
                    .setMin(0.0f)
                    .setMax(1.0f)
                    .setSaveConsumer(newValue -> cfg.sunsetCurve[finalI] = newValue)
                    .build());
        }

        ConfigCategory fresnelCategory = builder.getOrCreateCategory(Component.literal("Fresnel Height"));
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Base Y Level"), cfg.fresnelHeightBaseY)
                .setDefaultValue(80.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightBaseY = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Target Y Level"), cfg.fresnelHeightTargetY)
                .setDefaultValue(125.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightTargetY = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Target Multiplier"), cfg.fresnelHeightTargetMult)
                .setDefaultValue(0.45f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightTargetMult = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Min Multiplier"), cfg.fresnelHeightMinMult)
                .setDefaultValue(0.25f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightMinMult = newValue)
                .build());
        fresnelCategory.addEntry(entryBuilder.startFloatField(Component.literal("Max Multiplier"), cfg.fresnelHeightMaxMult)
                .setDefaultValue(2.0f)
                .setSaveConsumer(newValue -> cfg.fresnelHeightMaxMult = newValue)
                .build());

        return builder.build();
    }
}
