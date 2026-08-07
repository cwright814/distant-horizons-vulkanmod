#version 450

/**
 * DH Terrain Fragment Shader — native Vulkan GLSL 450
 *
 * Port of DH's flat_shaded.frag with Vulkan-specific changes:
 * - UBO at binding 0 (no individual uniforms)
 * - bool → int for std140 layout
 * - Inputs/outputs via layout(location = N)
 * - gl_FragCoord is built-in (no redeclaration)
 */

// Inputs from vertex shader
layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec3 vertexWorldPos;
layout(location = 2) in vec4 vPos;

// Output
layout(location = 0) out vec4 fragColor;

// Uniforms — shared with vertex shader
layout(set = 0, binding = 0) uniform DhUniforms {
    mat4 uCombinedMatrix;
#ifndef USE_PUSH_CONSTANTS
    vec3 uModelOffset;
#endif
    float uWorldYOffset;
    float uMircoOffset;
    float uEarthRadius;
    int uIsWhiteWorld;
    float uClipDistance;
    int uDitherDhRendering;
    int uNoiseEnabled;
    int uNoiseSteps;
    float uNoiseIntensity;
    int uNoiseDropoff;
    float uWaterDesaturation;
    float uCameraY;
    float uWorldDayTime;
    float uRainLevel;
    float uThunderLevel;
    float uSunrise1;
    float uSunrise2;
    float uSunrise3;
    float uSunrise4;
    float uSunrise5;
    float uSunset1;
    float uSunset2;
    float uSunset3;
    float uSunset4;
    float uSunset5;
    float uFresnelHeightBaseY;
    float uFresnelHeightTargetY;
    float uFresnelHeightTargetMult;
    float uFresnelHeightMinMult;
    float uFresnelHeightMaxMult;
};


// ==================== //
//    Noise functions    //
// ==================== //

// Integer hash — no trig, faster than sin-based PRNG on GPU
uint ihash(uint x) { x += x << 10u; x ^= x >> 6u; x += x << 3u; x ^= x >> 11u; x += x << 15u; return x; }
float rand(float co) { return float(ihash(floatBitsToUint(co))) / 4294967295.0; }
float rand(vec2 co) { return float(ihash(ihash(floatBitsToUint(co.x)) ^ floatBitsToUint(co.y))) / 4294967295.0; }
float rand(vec3 co) { return float(ihash(ihash(ihash(floatBitsToUint(co.x)) ^ floatBitsToUint(co.y)) ^ floatBitsToUint(co.z))) / 4294967295.0; }

vec3 quantize(vec3 val, int stepSize)
{
    return floor(val * stepSize) / stepSize;
}

void applyNoise(inout vec4 frag, const in float viewDist)
{
    vec3 vertexNormal = normalize(cross(dFdy(vPos.xyz), dFdx(vPos.xyz)));
    vec3 fixedVPos = vPos.xyz + vertexNormal * 0.001;

    float noiseAmplification = uNoiseIntensity;
    float lum = (frag.r + frag.g + frag.b) / 3.0;
    noiseAmplification = (1.0 - pow(lum * 2.0 - 1.0, 2.0)) * noiseAmplification;
    noiseAmplification *= frag.a;

    float randomValue = rand(quantize(fixedVPos, uNoiseSteps))
        * 2.0 * noiseAmplification - noiseAmplification;

    vec3 newCol = frag.rgb + (1.0 - frag.rgb) * randomValue;
    newCol = clamp(newCol, 0.0, 1.0);

    if (uNoiseDropoff != 0) {
        float distF = min(viewDist / float(uNoiseDropoff), 1.0);
        newCol = mix(newCol, frag.rgb, distF);
    }

    frag.rgb = newCol;
}


// ==================== //
//    Dither function    //
// ==================== //

/** Returns a normalized value between 0.0 and 1.0 */
float bayerMatrix4x4(vec2 st)
{
    int x = int(mod(st.x, 4.0));
    int y = int(mod(st.y, 4.0));

    float bayer4x4[16] = float[16](
         0.0,  8.0,  2.0, 10.0,
        12.0,  4.0, 14.0,  6.0,
         3.0, 11.0,  1.0,  9.0,
        15.0,  7.0, 13.0,  5.0
    );

    int index = y * 4 + x;
    return bayer4x4[index] / 16.0;
}


// ==================== //
//         Main         //
// ==================== //

void main()
{
    fragColor = vertexColor;

    // Water fragment detection (translucent or blue-dominant)
    if (fragColor.a < 0.99 || (fragColor.b > fragColor.r + 0.05 && fragColor.b > fragColor.g))
    {
        // 1. Water Desaturation controlled dynamically by DH Saturation slider
        float gray = dot(fragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
        fragColor.rgb = mix(fragColor.rgb, vec3(gray), clamp(uWaterDesaturation, 0.0, 1.0));

        // 2. Precalculated Piecewise Linear Fresnel Transition
        vec3 normal = normalize(cross(dFdy(vPos.xyz), dFdx(vPos.xyz)));
        vec3 viewDir = normalize(-vPos.xyz);
        float NdotV = clamp(dot(normal, viewDir), 0.0, 1.0);

        // Schlick Fresnel term
        float fresnel = pow(1.0 - NdotV, 4.0);

        // Tick-based brightness mapping (0 to 1) using precalculated arrays
        const float sunsetCurve[5] = float[](uSunset1, uSunset2, uSunset3, uSunset4, uSunset5);
        const float sunriseCurve[5] = float[](uSunrise1, uSunrise2, uSunrise3, uSunrise4, uSunrise5);

        float skyBrightness = 1.0;
        if (uWorldDayTime >= 12000.0 && uWorldDayTime <= 14000.0) {
            float t = (uWorldDayTime - 12000.0) / 500.0;
            int idx = int(clamp(floor(t), 0.0, 3.0));
            skyBrightness = mix(sunsetCurve[idx], sunsetCurve[idx + 1], fract(t));
        } else if (uWorldDayTime > 14000.0 && uWorldDayTime < 22000.0) {
            skyBrightness = 0.0;
        } else if (uWorldDayTime >= 22000.0 && uWorldDayTime <= 24000.0) {
            float t = (uWorldDayTime - 22000.0) / 500.0;
            int idx = int(clamp(floor(t), 0.0, 3.0));
            skyBrightness = mix(sunriseCurve[idx], sunriseCurve[idx + 1], fract(t));
        }

        // Bright daytime sky gloss tint matching Beryl's sky tone
        vec3 skyGloss = mix(vertexColor.rgb * 1.5, vec3(0.65, 0.75, 0.85), skyBrightness);

        // Height-based multiplier for fresnel (Squared Curve)
        // Computes a linear base and squares it. This creates a curve that grows rapidly > 1.0 and falls slowly < 1.0
        float targetBase = sqrt(uFresnelHeightTargetMult);
        float diffBase = 1.0 - targetBase;
        float diffY = uFresnelHeightTargetY - uFresnelHeightBaseY;
        
        float slope = (diffY == 0.0) ? 0.0 : diffBase / diffY;
        float deltaY = uCameraY - uFresnelHeightBaseY;
        
        float linearMult = max(0.0, 1.0 - (slope * deltaY));
        float heightMult = linearMult * linearMult;
        
        heightMult = clamp(heightMult, uFresnelHeightMinMult, uFresnelHeightMaxMult);

        fragColor.rgb = mix(fragColor.rgb, skyGloss, fresnel * 0.45 * skyBrightness * heightMult);
    }

    // Counteract vanilla lightmap darkening during rain/thunder
    float weatherBoost = 1.0 + (uRainLevel * 1.0) + (uThunderLevel * 1.0);
    fragColor.rgb *= weatherBoost;

    float viewDist = length(vertexWorldPos);

    // Fade/clip based on distance
    if (uDitherDhRendering != 0)
    {
        float worldNoise = bayerMatrix4x4(gl_FragCoord.xy);
        worldNoise += 0.001;

        float fadeStep = smoothstep(uClipDistance, uClipDistance * 1.5, viewDist);
        if (fadeStep <= worldNoise)
        {
            discard;
        }
    }

    // Apply noise
    if (uNoiseEnabled != 0)
    {
        applyNoise(fragColor, viewDist);
    }
}
