package moth.boxxed.panels.api.panel.skin;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SkinShape(boolean directional, Optional<List<Bounds>> bounds, Optional<List<Line>> lines) {
    public static final Codec<SkinShape> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("directional", true).forGetter(SkinShape::directional),
                        Codec.xor(
                                Codec.list(Codec.list(Codec.DOUBLE, 6,6), 1, 20),
                                Codec.list(Line.CODEC, 1, 20)
                        ).fieldOf("bounds").forGetter(SkinShape::boundsEither)
                ).apply(instance, SkinShape::fromCodec));

    public static SkinShape fromCodec(boolean directional, Either<List<List<Double>>, List<Line>> either) {
        Optional<List<Bounds>> optionalBounds = Optional.empty();
        Optional<List<Line>> optionalLines = Optional.empty();

        if (either.left().isPresent()) {
            List<Bounds> list = new ArrayList<>();
            for (List<Double> doubleList : either.left().get()) {
                list.add(Bounds.fromList(doubleList));
            }

            optionalBounds = Optional.of(list);
        } else if (either.right().isPresent()) {
            optionalLines = Optional.of(either.right().get());
        }


        return new SkinShape(directional, optionalBounds, optionalLines);
    }

    public Either<List<List<Double>>, List<Line>> boundsEither() {
        if (this.bounds.isPresent()) {
            List<List<Double>> listInList = new ArrayList<>();
            for (Bounds bound : this.bounds.get()) {
                listInList.add(bound.asList());
            }
            return Either.left(listInList);
        } else if (this.lines.isPresent()) {
            return Either.right(this.lines.get());
        }
        return null;
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

    public record Line(double x1, double y1, double z1, double x2, double y2, double z2) {
        public static final Codec<Line> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.DOUBLE.listOf(3, 3).fieldOf("p1").forGetter(Line::pointOne),
                        Codec.DOUBLE.listOf(3, 3).fieldOf("p2").forGetter(Line::pointTwo)
                ).apply(instance, Line::fromCodec)
        );

        public List<Double> pointOne() {
            return List.of(
                    this.x1,
                    this.y1,
                    this.z1
            );
        }

        public List<Double> pointTwo() {
            return List.of(
                    this.x2,
                    this.y2,
                    this.z2
            );
        }

        public static Line fromCodec(List<Double> pointOne, List<Double> pointTwo) {
            return new Line(
                    pointOne.get(0),
                    pointOne.get(1),
                    pointOne.get(2),
                    pointTwo.get(0),
                    pointTwo.get(1),
                    pointTwo.get(2)
            );
        }

        public Pair<Vec3, Vec3> toPair() {
            return new Pair<>(
                    new Vec3(this.x1/16, this.y1/16, this.z1/16),
                    new Vec3(this.x2/16, this.y2/16, this.z2/16)
            );
        }
    }
}
