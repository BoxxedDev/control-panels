package moth.boxxed.panels.content.panel.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleHitResult;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.io.IMultiInput;
import moth.boxxed.panels.api.module.tooltip.IHoverTooltip;
import moth.boxxed.panels.api.module.tooltip.TooltipContext;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.computercraft.IModuleLuaObject;
import moth.boxxed.panels.index.PanelModules;
import moth.boxxed.panels.index.PanelPreloadedModels;
import moth.boxxed.panels.util.PolyVoxel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.BiConsumer;

public class PushButtonModule extends Module implements IMultiInput, IModuleLuaObject, IHoverTooltip {
    private Integer selectedButton;

    protected ModuleConfigValue.IntValue buttonsAmount = new ModuleConfigValue.IntValue("buttons", 1, 1, 8)
            .withValidator(i -> {
                if (this.parentBlockEntity == null)
                    return false;
                if (this.getPos().x+(i*2) > 16)
                    return false;

                PolyVoxel polyVoxel = new PolyVoxel(0, 0, i*2,3)
                                .move(this.getPos().x, this.getPos().y);
                Dashpanels.LOGGER.debug(polyVoxel.toString());
                return !this.parentBlockEntity.polyVoxelCollidesModules(
                        new PolyVoxel(0, 0, i*2,3)
                                .move(this.getPos().x, this.getPos().y),
                        this.name
                );
            });

    public PushButtonModule(int x, int y) {
        super(PanelModules.PUSH_BUTTON.get(), x, y);
    }

    @Override
    public InteractionResult onUse(ModuleHitResult hitResult, Level level, Player player) {
        int selectedSwitch = (int) Math.clamp(Math.floor(Mth.clampedMap(hitResult.location().x, 0, this.buttonsAmount.get() * 0.125f, 0, this.buttonsAmount.get())), 0, this.buttonsAmount.get()-1);
        if (this.selectedButton != null && this.selectedButton == selectedSwitch) {
            this.selectedButton = null;
            level.playSound(null, this.getParentPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 0.5f);
            return InteractionResult.SUCCESS;
        }
        this.selectedButton = selectedSwitch;
        level.playSound(null, this.getParentPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.1f, 1f);
        return InteractionResult.SUCCESS;

    }

    @Override
    public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
        if (selectedButton != null) {
            tag.putInt("selected_switch", this.selectedButton);
        }
        return super.saveData(tag, registries);
    }

    @Override
    public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("selected_switch")) {
            this.selectedButton = tag.getInt("selected_switch");
        }
        return super.loadData(tag, registries);
    }

    @Override
    public void render(AbstractPanelBlockEntity pbe, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        for (int i = 1; i < this.buttonsAmount.get()+1; i++) {
            poseStack.pushPose();

            poseStack.translate(((this.buttonsAmount.get()-i)*2)/16f, 0, 0);
            PanelPreloadedModels.PUSH_BUTTON_BASE.render(poseStack, packedLight);

            float y = 0;
            if (this.selectedButton != null) {
                y = i-1 == this.selectedButton ? -0.25f : 0;
            }
            poseStack.translate(0, y/16f, 0);
            PanelPreloadedModels.PUSH_BUTTON.render(poseStack, packedLight);

            poseStack.popPose();
        }
    }

    @Override
    public void renderOutline(ModuleHitResult hitResult, PoseStack poseStack, float partialTick, int color) {
        if (hitResult != null) {
            MultiBufferSource bs = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = bs.getBuffer(RenderType.lines());

            poseStack.pushPose();
            int i = (int) Math.clamp(Math.floor(Mth.clampedMap(hitResult.location().x, 0, this.buttonsAmount.get() * 0.125f, 0, this.buttonsAmount.get())), 0, this.buttonsAmount.get()-1);
            poseStack.translate(i*2/16f, 0, 0);
            LevelRenderer.renderShape(
                    poseStack,
                    consumer,
                    Block.box(0, 0, 0, 2, 1, 3),
                    0, 0, 0, 1, 1, 1, 0.4f
            );
            poseStack.popPose();
        }
        super.renderOutline(hitResult, poseStack, partialTick, 0x000000);
    }

    @Override
    public VoxelShape getVoxelShape() {
        return Block.box(0, 0, 0, this.buttonsAmount.get()*2, 1, 3);
    }

    @Override
    public PolyVoxel getShape() {
        return new PolyVoxel(0, 0, this.buttonsAmount.get()*2,3);
    }

    @Override
    public void getValues(BiConsumer<String, AnalogResult> consumer) {
        for (int i = 1; i < this.buttonsAmount.get()+1; i++) {
            int finalI = i;
            consumer.accept("Button %d".formatted(i), () -> {
                if (this.selectedButton == null)
                    return 0;
                return finalI == this.selectedButton ? 15 : 0;
            });
        }
    }

    @Override
    public void getMethods(BiConsumer<String, ReturnMethod<?>> consumer) {

    }

    @Override
    public void createConfig(ModuleConfig.Builder builder) {
        builder.add(buttonsAmount);
    }

    @Override
    public void addLines(TooltipContext context, List<Component> list) {
        int i = (int) Math.clamp(Math.floor(Mth.clampedMap(context.hitResult().location().x, 0, this.buttonsAmount.get() * 0.125f, 0, this.buttonsAmount.get())), 0, this.buttonsAmount.get()-1);
        list.add(Component.translatable("tooltip.dashpanels.module.push_button", i));
    }
}
