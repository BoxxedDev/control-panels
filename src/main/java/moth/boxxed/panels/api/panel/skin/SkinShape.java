package moth.boxxed.panels.api.panel.skin;

import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO: fix
public record SkinShape(Optional<Boolean> directional, List<Bounds> bounds) {
    public static final Codec<SkinShape> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("directional").forGetter(SkinShape::directional),
                        Codec.list(Codec.list(Codec.DOUBLE, 6,6), 1, 20).fieldOf("bounds").forGetter(SkinShape::boundsListInList)
                ).apply(instance, SkinShape::fromCodec));

    public static SkinShape fromCodec(Optional<Boolean> directional, List<List<Double>> boundsListInList) {
        List<Bounds> boundsList = new ArrayList<>();

        for (List<Double> doubleList : boundsListInList) {
            boundsList.add(Bounds.fromList(doubleList));
        }

        return new SkinShape(directional, boundsList);
    }

    public List<List<Double>> boundsListInList() {
        List<List<Double>> ret = new ArrayList<>();

        for (Bounds bound : this.bounds) {
            ret.add(bound.asList());
        }

        return ret;
    }

    public record Bounds(double x1, double y1, double z1, double x2, double y2, double z2) {
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

            Vector3d v1 = new Vector3d(
                    this.x1,
                    this.y1,
                    this.z1
            );
            Vector3d v2 = new Vector3d(
                    this.x2,
                    this.y2,
                    this.z2
            );

            Quaternionf rotation = Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180));

            v1.sub(8, 8, 8);
            v2.sub(8, 8, 8);

            rotation.transform(v1);
            rotation.transform(v2);

            v1.add(8, 8, 8);
            v2.add(8, 8, 8);

            return Block.box(
                    Math.min(v1.x, v2.x),
                    Math.min(v1.y, v2.y),
                    Math.min(v1.z, v2.z),
                    Math.max(v1.x, v2.x),
                    Math.max(v1.y, v2.y),
                    Math.max(v1.z, v2.z)
            );
        }

        public List<Double> asList() {
            return List.of(
                    this.x1, this.y1, this.z1,
                    this.x2, this.y2, this.z2
            );
        }

        public static Bounds fromList(List<Double> list) {
            return new Bounds(
                    list.get(0),
                    list.get(1),
                    list.get(2),
                    list.get(3),
                    list.get(4),
                    list.get(5)
            );
        }
    }
}
