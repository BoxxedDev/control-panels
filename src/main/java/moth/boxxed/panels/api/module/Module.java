package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.util.Rect2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class Module {
    private Vector2i pos;
    private Vector2i size;

    public Rect2d rect;
    public PanelBlockEntity parentBlockEntity;
    public ModuleType<?> type;
    public String name = "";

    public Module(ModuleType<?> type, int x, int y, int sizeX, int sizeY) {
        this.type = type;
        this.pos = new Vector2i(x, y);
        this.size = new Vector2i(sizeX, sizeY);
        this.rect = new Rect2d(this.pos.x, this.pos.y, this.pos.x+this.size.x, this.pos.y+this.size.y);
    }

    @Override
    public String toString() {
        return "Module:{pos:[" + pos.toString() + "], size:[" + size.toString() + "]}";
    }

    public void setPos(int x, int y) {
        this.setPos(new Vector2i(x, y));
    }

    public void setPos(Vector2i vec) {
        this.pos = vec;
        this.rect = new Rect2d(this.pos.x, this.pos.y, this.pos.x+this.size.x, this.pos.y+this.size.y);
    }

    public Vector2i getPos() {
        return pos;
    }
    public Vector2i getSize() {
        return this.size;
    }
    public BlockPos getParentPos() {
        return this.parentBlockEntity.getBlockPos();
    }

    public boolean inside(int x, int y) {
        return this.rect.contains(x, y);
    }

    public boolean inside(Rect2d rect) {
        return this.rect.contains(rect);
    }

    public InteractionResult onUse(Level level, Player player) {
        return InteractionResult.PASS;
    }

    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.setPos(tag.getInt("pos_x"), tag.getInt("pos_y"));
        this.name = tag.getString("name");
        return true;
    }

    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        ResourceLocation type = ModuleType.getKey(this.type);
        if (type == null)
            return false;

        tag.putInt("pos_x", this.pos.x);
        tag.putInt("pos_y", this.pos.y);
        tag.putString("type", type.toString());
        tag.putString("name", this.name);

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay);

    @OnlyIn(Dist.CLIENT)
    public void renderOutline(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int color) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        Color rgb = new Color(color);
        poseStack.pushPose();
        LevelRenderer.renderShape(
                poseStack,
                consumer,
                this.getShape(),
                0, 0, 0, rgb.getRed()/255f, rgb.getGreen()/255f, rgb.getBlue()/255f, 0.4f
        );
        poseStack.popPose();
    }

    public static Double clipModule(PanelBlockEntity pbe, Module module, Vec3 shapeOffset, Vec3 eyePosMoj, Vec3 viewVectorMoj, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;

        Vector3d eyePos = JOMLConversion.toJOML(eyePosMoj);
        Vector3d viewVector = JOMLConversion.toJOML(viewVectorMoj);

        if (pbe.getLevel().isClientSide) {
            ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(pbe);
            if (subLevel != null) {
                Pose3dc pose = subLevel.renderPose(partialTick);

                pose.transformPositionInverse(eyePos);
                pose.transformNormalInverse(viewVector);
            }
        } else {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(pbe);
            if (subLevel != null) {
                Pose3dc pose = subLevel.lastPose();

                pose.transformPositionInverse(eyePos);
                pose.transformNormalInverse(viewVector);
            }
        }

        BlockPos blockPos = pbe.getBlockPos();

        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.translate(blockPos.getX()-eyePos.x, blockPos.getY()-eyePos.y, blockPos.getZ()-eyePos.z);

        Direction direction = pbe.getBlockState().getValue(PanelBlock.FACING);
        stack.rotateAround(Axis.YP.rotationDegrees(direction.toYRot() + (direction.getAxis()==Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
        stack.translate(0, 0, 0.25f);

        Matrix4f pose = stack.last().pose();
        pose.invert();
        stack.popPose();

        Vector3f localViewPos = pose.transformPosition(new Vector3f());
        Vector3f localViewDir = pose.transformDirection(new Vector3f((float) viewVector.x, (float) viewVector.y, (float) viewVector.z));

        VoxelShape shape = module.getShape().move(shapeOffset.x, shapeOffset.y, shapeOffset.z);

        eyePos.set(localViewPos);
        viewVector.set(localViewDir).mul(player.blockInteractionRange()).add(eyePos);

        BlockHitResult result = shape.clip(JOMLConversion.toMojang(eyePos), JOMLConversion.toMojang(viewVector), BlockPos.ZERO);

        if (result == null || result.getType() == HitResult.Type.MISS)
            return null;

        Vec3 location = result.getLocation();
        return eyePos.distanceSquared(location.x, location.y, location.z);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {

    }

    public abstract VoxelShape getShape();

    public String getName() {
        return this.name;
    }

    public ItemInteractionResult onItemUse(ItemStack stack, Level level, Player player) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public record ModuleInfo(ResourceLocation type, int x, int y, CompoundTag moduleData) {
        public static final Codec<ModuleInfo> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("module_type").forGetter(ModuleInfo::type),
                        Codec.INT.fieldOf("pos_x").forGetter(ModuleInfo::x),
                        Codec.INT.fieldOf("pos_y").forGetter(ModuleInfo::y),
                        CompoundTag.CODEC.fieldOf("module_data").forGetter(ModuleInfo::moduleData)
                ).apply(instance, ModuleInfo::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ModuleInfo> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, ModuleInfo::type,
                ByteBufCodecs.INT, ModuleInfo::x,
                ByteBufCodecs.INT, ModuleInfo::y,
                ByteBufCodecs.fromCodec(CompoundTag.CODEC), ModuleInfo::moduleData,
                ModuleInfo::new
        );

        public static ModuleInfo fromModule(Module module, HolderLookup.Provider registries) {
            CompoundTag compoundTag = new CompoundTag();
            module.saveData(compoundTag, registries);
            return new ModuleInfo(ModulesRegistry.MODULE_REGISTRY.getKey(module.type), module.pos.x, module.pos.y, compoundTag);
        }

        public Module create(HolderLookup.Provider registries) {
            ModuleType<?> moduleType = ModulesRegistry.MODULE_REGISTRY.get(this.type);
            if (moduleType == null) return null;
            Module module = moduleType.create(this.x, this.y);
            module.loadData(this.moduleData, registries);
            return module;
        }
    }
}
