package moth.boxxed.panels.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PoseStack.Pose.class)
public interface PoseStackPoseAccessor {
    @Accessor("trustedNormals")
    @Mutable
    void bends$setTrustedNormals(boolean bool);
}
