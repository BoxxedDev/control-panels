package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.util.PolyVoxel;
import moth.boxxed.panels.util.Rect2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import org.joml.*;
import oshi.util.tuples.Pair;

import javax.annotation.Nonnull;
import java.awt.*;

/**
Base class for creating a custom module
 */
public abstract class Module {
    @Deprecated(forRemoval = true, since = "2.0") private final Vector2i size;
    @Deprecated(forRemoval = true, since = "2.0") public Rect2d rect;

    public AbstractPanelBlockEntity parentBlockEntity;
    public ModuleType<?> type;
    private Vector2i pos;
    protected String name = "";

    private ModuleConfig config;
    protected ModuleConfigValue.StringValue nameConfig;

    public Module(@Nonnull ModuleType<?> type, int x, int y) {
        this.type = type;
        this.pos = new Vector2i(x, y);
        this.size = null;
    }

    @Deprecated(since = "2.0")
    public Module(@Nonnull ModuleType<?> type, int x, int y, int sizeX, int sizeY) {
        this.type = type;
        this.pos = new Vector2i(x, y);
        this.size = new Vector2i(sizeX, sizeY);
        this.rect = new Rect2d(this.pos.x, this.pos.y, this.pos.x+this.size.x, this.pos.y+this.size.y);
    }

    @Override
    public String toString() {
        return "Module:{name:[ " + this.name + " ], type:[ " + ModulesRegistry.MODULE_REGISTRY.getKey(this.type) + " ]}";
    }

    public void setPos(int x, int y) {
        this.setPos(new Vector2i(x, y));
    }

    public void setPos(Vector2i vec) {
        this.pos = vec;
        if (this.size != null)
            this.rect = new Rect2d(this.pos.x, this.pos.y, this.pos.x+this.size.x, this.pos.y+this.size.y);
    }

    public Vector2i getPos() {
        return pos;
    }
    public Vector2d getSize() {
        PolyVoxel shape = this.getShape();
        return new Vector2d(
                shape.getBounds().sizeX(),
                shape.getBounds().sizeY()
        );
    }
    public BlockPos getParentPos() {
        return this.parentBlockEntity.getBlockPos();
    }

    @Deprecated(since = "2.0")
    public InteractionResult onUse(Level level, Player player) {
        return InteractionResult.PASS;
    }

    public InteractionResult onUse(ModuleHitResult hitResult, Level level, Player player) {
        return this.onUse(level, player);
    }

    @Deprecated(since = "2.0")
    public ItemInteractionResult onItemUse(ItemStack stack, Level level, Player player) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public ItemInteractionResult onItemUse(ModuleHitResult hitResult, ItemStack stack, Level level, Player player) {
        return this.onItemUse(stack, level, player);
    }

    private void compileConfig() {
        this.nameConfig = new ModuleConfigValue.StringValue("name", this.name);
        this.nameConfig.setRevertable(false);
        this.nameConfig.withValidator(
                s -> {
                    if (this.parentBlockEntity == null)
                        return false;
                    if (this.parentBlockEntity.getModules().normalContainsKey(s))
                        return false;
                    if (this.parentBlockEntity.getLevel() == null)
                        return true;
                    if (this.parentBlockEntity.getLevel().isClientSide)
                        return true;
                    if (this.parentBlockEntity.getOrCreate() == null)
                        return true;
                    this.parentBlockEntity.getOrCreate().compileModules();
                    return !this.parentBlockEntity.getOrCreate().hasModule(s);
                }
        );
        ModuleConfig.Builder builder = new ModuleConfig.Builder();
        builder.add(nameConfig);
        this.createConfig(builder);
        this.config = builder.build();
    }

    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.setPos(tag.getInt("pos_x"), tag.getInt("pos_y"));
        this.name = tag.getString("name");

        this.compileConfig();
        CompoundTag configTag = tag.getCompound("config");
        for (ModuleConfigValue<?, ?> configValue : this.config.getValues()) {
            if (configValue == null || !configTag.contains(configValue.getId()))
                continue;
            CompoundTag valueTag = configTag.getCompound(configValue.getId());
            configValue.load(valueTag, registries);
        }

        return true;
    }

    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        ResourceLocation type = ModuleType.getKey(this.type);
        if (type == null)
            return false;

        CompoundTag configTag = new CompoundTag();
        this.compileConfig();
        for (ModuleConfigValue<?, ?> configValue : this.config.getValues()) {
            CompoundTag valueTag = new CompoundTag();
            configValue.save(valueTag, registries);
            configTag.put(configValue.getId(), valueTag);
        }

        tag.put("config", configTag);
        tag.putInt("pos_x", this.pos.x);
        tag.putInt("pos_y", this.pos.y);
        tag.putString("type", type.toString());
        tag.putString("name", this.name);

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay);

    @Deprecated(since = "2.0")
    @OnlyIn(Dist.CLIENT)
    public void renderOutline(PoseStack poseStack, float partialTick, int color) {
        MultiBufferSource bs = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bs.getBuffer(RenderType.lines());

        poseStack.pushPose();
        LevelRenderer.renderShape(
                poseStack,
                consumer,
                this.getVoxelShape(),
                0, 0, 0, ((color >> 16) & 0xFF)/255f, ((color >> 8) & 0xFF)/255f, (color & 0xFF)/255f, 0.4f
        );
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOutline(ModuleHitResult hitResult, PoseStack poseStack, float partialTick, int color) {
        this.renderOutline(poseStack, partialTick, color);
    }

    public static Pair<Double, Vec3> clipModule(
            AbstractPanelBlockEntity pbe,
            Module module,
            Vec3 shapeOffset,
            Vec3 eyePosMoj,
            Vec3 viewVectorMoj,
            float partialTick
    ) {
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

        pbe.transformPanelClipping(stack);

        Matrix4f pose = stack.last().pose();
        pose.invert();
        stack.popPose();

        Vector3f localViewPos = pose.transformPosition(new Vector3f());
        Vector3f localViewDir = pose.transformDirection(new Vector3f((float) viewVector.x, (float) viewVector.y, (float) viewVector.z));

        VoxelShape shape = module.getVoxelShape().move(shapeOffset.x, shapeOffset.y, shapeOffset.z);

        eyePos.set(localViewPos);
        viewVector.set(localViewDir).mul(player.blockInteractionRange()).add(eyePos);

        BlockHitResult result = shape.clip(JOMLConversion.toMojang(eyePos), JOMLConversion.toMojang(viewVector), BlockPos.ZERO);

        if (result == null || result.getType() == HitResult.Type.MISS)
            return null;

        Vec3 location = result.getLocation();
        return new Pair<>(eyePos.distanceSquared(location.x, location.y, location.z), location);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {

    }

    public abstract VoxelShape getVoxelShape();

    //TODO: Make abstract and reformat all the modules
    //Currently not abstract as to not totally kill the creators of the addons adding a large amount of modules
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, this.size.x, this.size.y);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String string) {
        this.name = string;
        this.nameConfig.set(string);
    }

    public void createConfig(ModuleConfig.Builder builder) {}

    public ModuleConfig getConfig() {
        return this.config;
    }

    public void setParentBE(AbstractPanelBlockEntity abstractPanelBlockEntity) {
        this.parentBlockEntity = abstractPanelBlockEntity;
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

    @Deprecated(forRemoval = true, since = "2.0")
    public boolean inside(int x, int y) {
        return this.rect.contains(x, y);
    }

    @Deprecated(forRemoval = true, since = "2.0")
    public boolean inside(Rect2d rect) {
        return this.rect.contains(rect);
    }

    @Deprecated(forRemoval = true, since = "2.0")
    public Rect2i getRect() {
        return new Rect2i(
                this.pos.x,
                this.pos.y,
                this.size.x,
                this.size.y
        );
    }
}
