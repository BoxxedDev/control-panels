package moth.boxxed.panels.content.modules.key_switch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.module.tooltip.IHoverTooltip;
import moth.boxxed.panels.api.module.tooltip.TooltipContext;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.*;
import moth.boxxed.panels.util.PolyVoxel;
import moth.boxxed.panels.util.ShortUUID;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;

import java.util.List;
import java.util.function.BiConsumer;

public class KeySwitchModule extends Module implements IExternalUpdatable, IMultiInput, IHoverTooltip, Container {
    protected ItemStack currentKeyStack = ItemStack.EMPTY;
    protected ShortUUID keyId;
    protected boolean turned = false;
    protected float turn = 0;

    protected float oldRenderTurn = 0;
    protected float renderTurn = 0;

    public KeySwitchModule(int x, int y) {
        super(PanelModules.KEY_SWITCH.get(), x, y);
    }

    @Override
    public InteractionResult onUse(ModuleHitResult hitResult, Level level, Player player) {
        if (!level.isClientSide() && player.isShiftKeyDown() && !this.currentKeyStack.isEmpty() && !this.turned) {
            player.getInventory().add(this.currentKeyStack.copyAndClear());
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide() && !this.currentKeyStack.isEmpty() && !PanelHoldInteractions.KEY_SWITCH.isActive()) {
            PanelHoldInteractions.KEY_SWITCH.startHold(level, player, this);
            return InteractionResult.SUCCESS;
        }

        return super.onUse(hitResult, level, player);
    }

    @Override
    public ItemInteractionResult onItemUse(ModuleHitResult hitResult, ItemStack stack, Level level, Player player) {
        if (currentKeyStack.isEmpty() && stack.is(PanelItems.KEY_ITEM)) {
            if (keyId == null && !stack.has(PanelDataComponents.BOUND_MODULE.get())) {
                ShortUUID generatedId = ShortUUID.random();
                stack.set(PanelDataComponents.BOUND_MODULE.get(), new BoundModule(this.getParentPos(), generatedId));
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                this.keyId = generatedId;
                return ItemInteractionResult.SUCCESS;
            } else if (stack.has(PanelDataComponents.BOUND_MODULE.get())) {
                BoundModule boundModule = stack.get(PanelDataComponents.BOUND_MODULE.get());
                if (boundModule.pos().equals(this.getParentPos()) && boundModule.uuid().equals(this.keyId)) {
                    this.currentKeyStack = stack.copyAndClear();
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return super.onItemUse(hitResult, stack, level, player);
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        if (!this.currentKeyStack.isEmpty()) {
            tag.put("key", this.currentKeyStack.save(registries));
        }
        if (this.keyId != null) {
            this.keyId.put("key_id", tag);
        }
        tag.putBoolean("turned", this.turned);
        tag.putFloat("turn", this.turn);

        tag.putFloat("render_turn", this.renderTurn);
        tag.putFloat("old_render_turn", this.oldRenderTurn);
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("key_id")) {
            this.keyId = ShortUUID.fromTag("key_id", tag);
        }
        if (tag.contains("key")) {
            Tag itemTag = tag.get("key");
            if (itemTag != null) {
                this.currentKeyStack = ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY);
            }
        }
        this.turned = tag.getBoolean("turned");
        this.turn = tag.getFloat("turn");

        this.renderTurn = tag.getFloat("render_turn");
        this.oldRenderTurn = tag.getFloat("old_render_turn");
        return super.loadData(tag, registries);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        this.oldRenderTurn = this.renderTurn;
        this.renderTurn = Math.lerp(this.renderTurn, this.turn*-90f, 0.5f);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PanelPreloadedModels.KEY_SWITCH_BASE.render(poseStack, packedLight);

        poseStack.pushPose();

        poseStack.rotateAround(Axis.YP.rotationDegrees(Math.lerp(this.oldRenderTurn, this.renderTurn, partialTick)), 0.0625f, 0, 0.0625f);
        PanelPreloadedModels.KEY_SWITCH_HOLE.render(poseStack, packedLight);
        if (!this.currentKeyStack.isEmpty()) {
            PanelPreloadedModels.KEY_SWITCH_KEY.render(poseStack, packedLight);
        }
        poseStack.popPose();
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(-0.5, 0, -0.5, 2.5, 2, 2.5);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 2, 2);
    }

    @Override
    public void update(ServerPlayer player, CompoundTag tag, HolderLookup.Provider registries) {
        this.turned = tag.getBoolean("turned");
        this.turn = tag.getFloat("turn");
    }

    @Override
    public void getValues(BiConsumer<String, AnalogResult> consumer) {
        consumer.accept("turned", () -> this.turned ? 15 : 0);
        consumer.accept("in slot", () -> !this.currentKeyStack.isEmpty() ? 15 : 0);
    }

    @Override
    public void addLines(TooltipContext context, List<Component> list) {
        if (this.keyId != null) {
            list.add(Component.translatable("tooltip.dashpanels.module.key_switch.key_id", this.keyId.toString()));
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.currentKeyStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.currentKeyStack;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.currentKeyStack.copyAndClear();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.currentKeyStack.copyAndClear();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.currentKeyStack = stack;
    }

    @Override
    public void setChanged() {
        if (this.parentBlockEntity != null) {
            this.parentBlockEntity.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.currentKeyStack = ItemStack.EMPTY;
    }
}