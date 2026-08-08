# Distant Horizons — VulkanMod Extension

A Fabric extension mod that adds native **Vulkan rendering** to [Distant Horizons](https://gitlab.com/distant-horizons-team/distant-horizons) via [VulkanMod](https://github.com/xCollateral/VulkanMod).

LODs are rendered using VulkanMod's Vulkan pipeline instead of OpenGL, enabling Distant Horizons to work on systems and configurations running VulkanMod. Beryl support is basic:
- Water has shader-based fresnel emulation with no sun support.
- Atmospheric fog blends between real and lod chunks only at standard block heights (300 and below).
- Cloud rendering will be disabled.
- Inclement weather blending is in alpha.

> **This is not a standalone mod.** You must have Distant Horizons, VulkanMod, AND Beryl installed. Get the latest versions as of Aug 6th, 2026. Tested for Minecraft version 26.1.2 with the default texture pack. I recommend installing "Not Enough Vulkan" to avoid broken atmospheric fog - you still get fog, but it allows the blend between the real chunks and lod chunks to be more seamless. Note that the mod manages the atmospheric fog setting automatically, so you do not need to manually disable it.

> **Recommended Config:** I have included my recommended configuration for Distant Horizons in the `recommended_configs` folder. For the best visual experience when using this mod, copy `recommended_configs/DistantHorizons.toml` to your `.minecraft/config` folder. I highly recommend a real chunk render distance of 12 in VulkanMod Graphics settings. If your machine is quite powerful, a render distance of 20 is a great quality middle-ground. Setting the render distance to 32 looks fantastic, but you will likely increase lag spikes and the risk of an out-of-memory crash.

![Distant Horizons running on VulkanMod](docs/dh-vulkanmod.jpg)
*Distant Horizons LODs rendered via VulkanMod's Vulkan backend*

## Status

### ✅ Working
- LOD terrain rendering with correct colors and vertex format
- Lightmap support (day/night cycle, block light)
- Depth integration (LODs render behind MC terrain)
- Transparency / alpha blending (water, glass, etc.)
- Ambient occlusion (SSAO)
- Distance and height fog (all falloff types and mixing modes)
- Noise / dithering on LODs
- Fade / clip distance transitions
- Earth curvature rendering
- Cloud rendering with correct depth against LOD terrain (VM 0.6+)
- Weather effects (rain, snow) render correctly in front of LODs

### ⚠️ Not Yet Implemented
- **Shader pack support** — VulkanMod does not support shader packs (Iris/OptiFine)
- **Wireframe debug mode**

See [docs/vulkan_implementation_roadmap.md](docs/vulkan_implementation_roadmap.md) for the full technical roadmap.

## Requirements

- **Minecraft:** 1.20.6 or 26.1 / 26.1.1 / 26.1.2
- **Mod loader:** Fabric
- **VulkanMod** must be installed (0.4.2 for MC 1.20.6, 0.6.6+ for MC 26.1.x)
- **Distant Horizons** 2.4.0+ (1.20.6) or 3.0.0+ (26.1.x) must be installed

> This is not the official Distant Horizons mod. For the original, see the [GitLab](https://gitlab.com/distant-horizons-team/distant-horizons) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/distant-horizons) pages.

## Building

```bash
# MC 26.1.2 (also supports 26.1 and 26.1.1 in the same jar)
./gradlew :vulkan:build -PmcVer="26.1.2"

# MC 1.20.6
./gradlew :vulkan:build -PmcVer="1.20.6"
```

Place matching **Distant Horizons** and **VulkanMod** jars in `jars/` before building (see Modrinth: DH 3.0.3-b for 26.1.2, VulkanMod 0.6.6 for 26.1.2).

The compiled jar will be in `vulkan/build/libs/`.

## Source Code Setup

### Prerequisites

* JDK 21+ for MC 1.20.6 builds; **JDK 25+** for MC 26.1.x (Gradle auto-downloads via toolchains)
* Git — https://git-scm.com/

### IntelliJ IDEA
1. Install the Manifold plugin
2. Open IDEA and import the `build.gradle`
3. Refresh the Gradle project if required

### Other commands

```bash
./gradlew --refresh-dependencies   # refresh dependencies
./gradlew genSources               # generate MC source for browsing
```

> Source code uses Mojang mappings & [Parchment](https://parchmentmc.org/) mappings.

## Open Source Acknowledgements

- [Forgix](https://github.com/PacifistMC/Forgix) — jar merging
- [LZ4 for Java](https://github.com/lz4/lz4-java) — data compression
- [NightConfig](https://github.com/TheElectronWill/night-config) — JSON & TOML config handling
- [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) — SQLite driver
