package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.util.Rect2d;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector2i;

public abstract class Module {
    private Vector2i pos;
    private Vector2i size;

    public Rect2d rect;
    public PanelBlockEntity parentBlockEntity;
    public ModuleType<?> type;
    public String name;

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

    public boolean loadData(CompoundTag tag) {
        this.setPos(tag.getInt("pos_x"), tag.getInt("pos_y"));
        return true;
    }

    public boolean saveData(CompoundTag tag) {
        ResourceLocation type = ModuleType.getKey(this.type);
        if (type == null)
            return false;

        tag.putInt("pos_x", this.pos.x);
        tag.putInt("pos_y", this.pos.y);
        tag.putString("type", type.toString());

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay);

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {

    }

    public abstract VoxelShape getShape();

    public String getName() {
        return this.name;
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

        public static ModuleInfo fromModule(Module module) {
            CompoundTag compoundTag = new CompoundTag();
            module.saveData(compoundTag);
            return new ModuleInfo(ModulesRegistry.MODULE_REGISTRY.getKey(module.type), module.pos.x, module.pos.y, compoundTag);
        }

        public Module create() {
            ModuleType<?> moduleType = ModulesRegistry.MODULE_REGISTRY.get(this.type);
            if (moduleType == null) return null;
            Module module = moduleType.create(this.x, this.y);
            module.loadData(this.moduleData);
            return module;
        }
    }
}
