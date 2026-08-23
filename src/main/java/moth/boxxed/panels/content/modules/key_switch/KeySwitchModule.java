package moth.boxxed.panels.content.modules.key_switch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import moth.boxxed.panels.api.module.IExternalUpdatable;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.module.tooltip.IHoverTooltip;
import moth.boxxed.panels.api.module.tooltip.TooltipContext;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.compat.computercraft.ModuleMethodBuilder;
import moth.boxxed.panels.index.PanelDataComponents;
import moth.boxxed.panels.index.PanelHoldInteractions;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import moth.boxxed.panels.util.ShortUUID;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class KeySwitchModule extends Module implements IExternalUpdatable, IMultiInput, IHoverTooltip, Container {
    protected ItemStack currentKeyStack = ItemStack.EMPTY;
    protected ShortUUID keyId;

    protected UUID playerWhoFroze = null;

    protected boolean turned = false;
    protected float turn = 0;

    protected float oldRenderTurn = 0;
    protected float renderTurn = 0;

    public final ModuleConfigValue.BooleanValue togglable = new ModuleConfigValue.BooleanValue("togglable", true);

    public KeySwitchModule(int x, int y) {
        super(PanelModules.KEY_SWITCH.get(), x, y);
    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(togglable);
    }

    @Override
    public InteractionResult onUse(ModuleHitResult hitResult, Level level, Player player) {
        if (player.isShiftKeyDown() && !this.currentKeyStack.isEmpty() && !this.turned) {
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
        if (currentKeyStack.isEmpty() && stack.getItem() instanceof KeyItem) {
            if (keyId == null && !stack.has(PanelDataComponents.BOUND_MODULE.get())) {
                ItemStack newStack = stack.copyWithCount(1);

                stack.shrink(1);
                ShortUUID generatedId = ShortUUID.random();
                newStack.set(PanelDataComponents.BOUND_MODULE.get(), new BoundModule(this.getParentPos(), generatedId));
                newStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

                player.getInventory().add(newStack);

                this.keyId = generatedId;
                return ItemInteractionResult.SUCCESS;
            } else if (stack.has(PanelDataComponents.BOUND_MODULE.get())) {
                BoundModule boundModule = stack.get(PanelDataComponents.BOUND_MODULE.get());
                if (boundModule.pos().equals(this.getParentPos()) && boundModule.uuid().equals(this.keyId)) {
                    this.currentKeyStack = stack.copyWithCount(1);
                    stack.shrink(1);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        } else if (stack.getCount() == 1 && stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS) != null) {
            KeyChainContents.Mutable mutable = new KeyChainContents.Mutable(stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS));
            if (this.currentKeyStack.isEmpty() && mutable.has(this.keyId, this.getParentPos())) {
                this.currentKeyStack = mutable.remove(this.keyId, this.getParentPos());
                KeyChainItem.playJingle(player);
            } else if (player.isShiftKeyDown() && !this.currentKeyStack.isEmpty() && !this.turned) {
                if (mutable.tryAdd(this.currentKeyStack)) {
                    this.currentKeyStack = ItemStack.EMPTY;
                    KeyChainItem.playJingle(player);
                }
            }
            stack.set(PanelDataComponents.KEY_CHAIN_CONTENTS, mutable.immutable());
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.HONEYCOMB) && this.playerWhoFroze == null && this.keyId != null) {
            stack.consume(1, player);
            this.playerWhoFroze = player.getUUID();

            if (level.isClientSide()) {
                ParticleUtils.spawnParticlesOnBlockFaces(player.level(), this.getParentPos(), ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                level.playLocalSound(this.getParentPos(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.onItemUse(hitResult, stack, level, player);
    }

    @Override
    public boolean canRemove(Player player) {
        if (player.getUUID().equals(this.playerWhoFroze)) {
            this.playerWhoFroze = null;
            if (player.level().isClientSide()) {
                ParticleUtils.spawnParticlesOnBlockFaces(player.level(), this.getParentPos(), ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
                player.level().playLocalSound(this.getParentPos(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            return false;
        }

        if (this.playerWhoFroze != null) {
            return false;
        }

        if (this.keyId != null) {
            this.keyId = null;
            return false;
        }

        return super.canRemove(player);
    }

    @Override
    public boolean canMove(Player player) {
        return this.keyId == null;
    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        if (!this.currentKeyStack.isEmpty()) {
            tag.put("key", this.currentKeyStack.save(registries));
        }
        if (this.keyId != null) {
            this.keyId.put("key_id", tag);
        }
        if (this.playerWhoFroze != null) {
            tag.putUUID("froze", this.playerWhoFroze);
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
        if (tag.contains("froze")) {
            this.playerWhoFroze = tag.getUUID("froze");
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

        poseStack.rotateAround(Axis.YP.rotationDegrees(Math.lerp(this.oldRenderTurn, this.renderTurn, partialTick)), 1.5f/16f, 0, 1.5f/16f);
        PanelPreloadedModels.KEY_SWITCH_HOLE.render(poseStack, packedLight);
        if (!this.currentKeyStack.isEmpty()) {
            if (this.currentKeyStack.getItem() instanceof KeyItem keyItem) {
                if (keyItem.getColor().isEmpty()) {
                    PanelPreloadedModels.KEY_SWITCH_KEY.render(poseStack, packedLight);
                } else {
                    PanelPreloadedModels.KEY_SWITCH_COLOR.render(poseStack, RenderType.cutout(), packedLight, keyItem.getColor().orElse(DyeColor.WHITE).getTextureDiffuseColor());
                    PanelPreloadedModels.KEY_SWITCH_END.render(poseStack, packedLight);
                }
            }
        }
        poseStack.popPose();
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, 3, 1, 3);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, 3, 3);
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

    @Override
    public void buildComputerMethods(ModuleMethodBuilder builder) {
        builder.addReturn("keyInserted", args -> !this.currentKeyStack.isEmpty());
        builder.addReturn("keyTurned", args -> this.turned);
        builder.addReturn("keyId", args -> this.keyId.toString());
    }
}