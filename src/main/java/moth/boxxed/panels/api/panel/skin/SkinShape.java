package moth.boxxed.panels.api.panel.skin;

import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.List;
import java.util.Optional;

//TODO: fix
public record SkinShape(Optional<Boolean> directional, List<Bounds> bounds) {
    public static final Codec<SkinShape> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("directional").fieldOf("directional").forGetter(SkinShape::directional),
                        Codec.list(Bounds.CODEC, 1, 20).fieldOf("bounds").forGetter(SkinShape::bounds)
                ).apply(instance, SkinShape::new));

    public record Bounds(double x1, double y1, double z1, double x2, double y2, double z2) {
        public static final Codec<Bounds> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.DOUBLE.fieldOf("x1").forGetter(Bounds::x1),
                        Codec.DOUBLE.fieldOf("y1").forGetter(Bounds::y1),
                        Codec.DOUBLE.fieldOf("z1").forGetter(Bounds::z1),
                        Codec.DOUBLE.fieldOf("x2").forGetter(Bounds::x2),
                        Codec.DOUBLE.fieldOf("y2").forGetter(Bounds::y2),
                        Codec.DOUBLE.fieldOf("z2").forGetter(Bounds::z2)
                ).apply(instance, Bounds::new));

        public VoxelShape toVoxelShape(boolean isDirectional, Direction direction) {
            if (!isDirectional)
                return Block.box(
                        Math.min(this.x1, this.x2),
                        Math.min(this.y1, this.y2),
                        Math.min(this.z1, this.z2),
                        Math.max(this.x1, this.x2),
                        Math.max(this.y1, this.y2),
                        Math.max(this.z1, this.z2)
                );

            Vector3d min = new Vector3d(
                    Math.min(this.x1, this.x2),
                    Math.min(this.y1, this.y2),
                    Math.min(this.z1, this.z2)
            );
            Vector3d max = new Vector3d(
                    Math.max(this.x1, this.x2),
                    Math.max(this.y1, this.y2),
                    Math.max(this.z1, this.z2)
            );

            Quaternionf rotation = Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180));

            min.sub(8, 8, 8);
            max.sub(8, 8, 8);

            rotation.transform(min);
            rotation.transform(max);

            min.add(8, 8, 8);
            max.add(8, 8, 8);

            return Block.box(
                    min.x,
                    min.y,
                    min.z,
                    max.x,
                    max.y,
                    max.z
            );
        }
    }
}
