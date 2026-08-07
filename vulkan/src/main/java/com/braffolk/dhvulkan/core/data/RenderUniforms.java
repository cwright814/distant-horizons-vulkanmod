package com.braffolk.dhvulkan.core.data;

import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import com.seibel.distanthorizons.core.util.math.DhMat4f;

/**
 * DH-agnostic uniform data for a single render frame.
 * Both DH 2.4 and DH 3.0 integration layers populate this
 * from their respective parameter types.
 */
public class RenderUniforms {
    /** DH's projection matrix (extended near clip for high altitudes) */
    public final DhMat4f dhProjectionMatrix = new DhMat4f();

    /** DH's model-view matrix */
    public final DhMat4f dhModelViewMatrix = new DhMat4f();

    /** MC's projection matrix (for depth remapping in composite) */
    public final DhMat4f mcProjectionMatrix = new DhMat4f();

    /** World Y offset for terrain rendering */
    public double worldYOffset;

    /** Partial tick time for fog interpolation */
    public float partialTicks;

    /**
     * Set all fields from source matrices.
     * Callers should set worldYOffset and partialTicks directly.
     */
    public void set(DhApiMat4f dhProj, DhApiMat4f dhModelView, DhApiMat4f mcProj) {
        if (dhProj != null) this.dhProjectionMatrix.set(dhProj);
        if (dhModelView != null) this.dhModelViewMatrix.set(dhModelView);
        if (mcProj != null) this.mcProjectionMatrix.set(mcProj);
    }
}
