package moth.boxxed.panels.content.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.io.IOutput;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleLuaException;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.MathUtil;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.function.BiConsumer;

public class SevenSegmentModule extends Module implements IOutput {
    private static final int MAX_CHAR = 3;

    public String display = "---";
    public DyeColor color = DyeColor.WHITE;

    public SevenSegmentModule(int x, int y) {
        super(PanelModules.SEVEN_SEGMENT.get(), x, y);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("display", this.display);
        tag.putInt("color", color.getId());
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        this.display = tag.getString("display");
        this.color = DyeColor.byId(tag.getInt("color"));
        return super.loadData(tag, registries);
    }

    @Override
    public void render(AbstractPanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.SEVEN_SEGMENT.render(poseStack, RenderType.solid(), packedLight);

        Font font = Minecraft.getInstance().font;

        FormattedCharSequence sequence = Component.literal(this.display).getVisualOrderText();

        float scale = Math.min(17f/font.width(sequence), 1) * 0.9f;
        poseStack.pushPose();
        poseStack.translate(3.8/16f, 1/32f, (2-scale/4f)/16f);
        poseStack.scale(scale/64f, scale/64f, scale/64f);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        font.drawInBatch(
                sequence,
                0, 0,
                this.color.getTextColor(),
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LightTexture.FULL_BRIGHT
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ItemInteractionResult onItemUse(ItemStack stack, Level level, Player player) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            this.color = dyeItem.getDyeColor();
            return ItemInteractionResult.SUCCESS;
        }
        return super.onItemUse(stack, level, player);
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, 4, 0.5, 2);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 4, 2);
    }

    @Override
    public void setAnalog(int signal) {
        this.display = String.valueOf(signal);
    }

    private List<SevenSegmentModule> getConnectedModules() {
        Set<SevenSegmentModule> checked = new HashSet<>();
        Deque<SevenSegmentModule> toCheck = new ArrayDeque<>();
        toCheck.add(this);

        while (!toCheck.isEmpty()) {
            SevenSegmentModule module = toCheck.poll();

            for (int i : new int[]{-1, 1}) {
                if (module.parentBlockEntity.getModuleAt(
                        (int) (module.getPos().x + (Math.ceil(module.getSize().x))*i),
                        module.getPos().y
                ) instanceof SevenSegmentModule other) {
                    if (!checked.contains(other))
                        toCheck.add(other);
                }
            }
            for (int i : new int[]{-1, 1}) {
                if (module.parentBlockEntity.getModuleAt(
                        module.getPos().x,
                        (int) (module.getPos().y + (Math.ceil(module.getSize().y))*i)
                ) instanceof SevenSegmentModule other) {
                    if (!checked.contains(other))
                        toCheck.add(other);
                }
            }

            checked.add(module);
        }
        return checked.stream()
                .sorted(Comparator.comparingInt(module -> module.getPos().y*module.parentBlockEntity.getContentArea().x+module.getPos().x))
                .toList();
    }

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        builder.addReturn("getDisplay", args -> this.display);
        builder.addReturn("getColor", args -> this.color.getSerializedName());
        builder.addVoid("setDisplay", args -> {
            if (!(args.count() == 1 || args.count() == 2))
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");

            List<SevenSegmentModule> connectedModules = this.getConnectedModules();
            if (args.get(1) instanceof Boolean bool && bool) {
                connectedModules = List.of(this);
            }

            if (args.get(0) instanceof String str && str.length() <= connectedModules.size()*MAX_CHAR) {
                for (int i = 0; i < connectedModules.size(); i++) {
                    if (i * MAX_CHAR >= str.length()) {
                        continue;
                    }

                    SevenSegmentModule module = connectedModules.get(i);
                    StringBuilder subStr = new StringBuilder(str.substring(i * MAX_CHAR, Math.min((i * MAX_CHAR) + MAX_CHAR, str.length())));
                    while (subStr.length() < MAX_CHAR) {
                        subStr.append(" ");
                    }
                    module.display = subStr.toString();
                }
                return;
            }
            throw new ModuleLuaException("First arg has to be a string and has to be less than or equal to %d in length".formatted(connectedModules.size()*MAX_CHAR));
        });
        builder.addVoid("setColor", args -> {
            if (args.count() != 1)
                throw new ModuleLuaException("Arg amount cannot be less than or greater than 1");
            if (args.get(0) instanceof String string) {
                this.color = DyeColor.byName(string, DyeColor.WHITE);
                this.parentBlockEntity.networkUpdate(this.parentBlockEntity.getOrCreate());
                return;
            }
            throw new ModuleLuaException("First arg has to be a string");
        });
        builder.addReturn("getCharLimit", args -> this.getConnectedModules().size()*MAX_CHAR);
        builder.addVoid("clear", args -> {
            boolean clearConnected = true;
            if (args.get(0) instanceof Boolean bool) {
                clearConnected = bool;
            }

            if (clearConnected) {
                List<SevenSegmentModule> connectedModules = this.getConnectedModules();
                for (SevenSegmentModule module : connectedModules) {
                    module.display = "";
                }
            } else {
                this.display = "";
            }
        });
    }
}
