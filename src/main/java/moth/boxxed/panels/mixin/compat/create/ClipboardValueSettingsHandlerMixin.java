package moth.boxxed.panels.mixin.compat.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.utility.CreateLang;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.io.ModuleIOInfo;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.create.panel_link.ModuleLinkEntries;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(ClipboardValueSettingsHandler.class)
//Green moose purple goose (in reference to the mixin name)
public class ClipboardValueSettingsHandlerMixin {
    @Inject(method = "drawCustomBlockSelection",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
            cancellable = true)
    private static void panels$certainBlockSelectionOverride(
            RenderHighlightEvent.Block event, CallbackInfo ci,
            @Local(name = "mc") Minecraft mc,
            @Local(name = "target") BlockHitResult target,
            @Local(name = "pos") BlockPos pos,
            @Local(name = "blockstate") BlockState blockstate) {
        if (!(blockstate.getBlock() instanceof PanelLinkBlock))
            return;

        VoxelShape shape = blockstate.getShape(mc.level, pos);
        if (shape.isEmpty())
            return;

        VertexConsumer vb = event.getMultiBufferSource()
                .getBuffer(RenderType.lines());
        Vec3 camPos = event.getCamera()
                .getPosition();

        PoseStack ms = event.getPoseStack();

        ms.pushPose();
        ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
        TrackBlockOutline.renderShape(shape, ms, vb, true);
        event.setCanceled(true);

        ms.popPose();
        ci.cancel();
    }

    @Inject(method = "clientTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
            cancellable = true)
    private static void panels$customClientTickingBehavior(
            CallbackInfo ci,
            @Local(name = "mc") Minecraft mc,
            @Local(name = "pos") BlockPos pos
    ) {
        final BlockEntity be = mc.level.getBlockEntity(pos);

        ClipboardContent content = mc.player.getMainHandItem()
                .get(AllDataComponents.CLIPBOARD_CONTENT);
        if (content == null)
            return;

        final CompoundTag tagElement = content.copiedValues().orElse(null);

        if (tagElement != null && tagElement.contains("panel_module_frequency") && be instanceof PanelLinkBlockEntity) {
            List<MutableComponent> tip = new ArrayList<>();
            tip.add(CreateLang.translateDirect("clipboard.actions"));
            tip.add(CreateLang.translateDirect("clipboard.to_paste", Component.keybind("key.attack")));
            CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
            ci.cancel();
        } else if (be instanceof AbstractPanelBlockEntity pbe && !pbe.getSelectedModule(mc.player).isEmpty()) {
            Dashpanels.LOGGER.debug("{}", pbe.getSelectedModule(mc.player));
            List<MutableComponent> tip = new ArrayList<>();
            tip.add(CreateLang.translateDirect("clipboard.actions"));
            tip.add(CreateLang.translateDirect("clipboard.to_copy", Component.keybind("key.use")));
            CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
            ci.cancel();
        }
    }

    @Inject(method = "interact",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
            cancellable = true)
    private static void panels$customInteraction(
            PlayerInteractEvent event, boolean paste, CallbackInfo ci,
            @Local(name = "itemStack") ItemStack itemStack,
            @Local(name = "pos") BlockPos pos,
            @Local(name = "world") Level level,
            @Local(name = "player") Player player
    ) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        final BlockState blockState = level.getBlockState(pos);
        final ClipboardContent clipboardContent = itemStack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);

        CompoundTag tag = clipboardContent.copiedValues().orElse(null);
        if (paste && tag == null)
            return;
        if (tag == null)
            tag = new CompoundTag();

        if (paste && tag.contains("panel_module_frequency") &&
                blockEntity instanceof PanelLinkBlockEntity panelLink &&
                blockState.getBlock() instanceof PanelLinkBlock panelLinkBlock) {
            final RegistryOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            final DataResult<Pair<ModuleLinkEntries.ModuleEntry, Tag>> entryResult = ModuleLinkEntries.ModuleEntry.CODEC.decode(ops, tag.get("panel_module_frequency"));

            if (!entryResult.isSuccess())
                return;

            final ModuleLinkEntries.ModuleEntry entry = entryResult.getOrThrow().getFirst();

            panelLink.getOrCreate().compileModules();
            if (panelLink.getOrCreate().getCompiledModules().containsKey(entry.getEntry().name())) {
                if (event instanceof ICancellableEvent cancellableEvent)
                    cancellableEvent.setCanceled(true);
                if (event instanceof PlayerInteractEvent.RightClickBlock rightClickBlock)
                    rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

                if (!level.isClientSide) {
                    panelLinkBlock.openMenu(level, pos, player, entry);
                    player.displayClientMessage(CreateLang
                            .translate("clipboard.pasted_to", level.getBlockState(pos)
                                    .getBlock()
                                    .getName()
                                    .withStyle(ChatFormatting.WHITE))
                            .style(ChatFormatting.GREEN)
                            .component(), true);
                }

                ci.cancel();
            }
        } else if (!paste && blockEntity instanceof AbstractPanelBlockEntity pbe &&
                pbe.getSelectedModule(player) != null && pbe.getModule(pbe.getSelectedModule(player)) != null) {
            final ModuleIOInfo ioInfo = ModuleIOInfo.create(pbe.getSelectedModule(player), pbe.getModule(pbe.getSelectedModule(player)));
            if (ioInfo != null) {
                final ModuleLinkEntries.ModuleEntry newEntry = new ModuleLinkEntries.ModuleEntry(
                        RedstoneLinkNetworkHandler.Frequency.EMPTY,
                        RedstoneLinkNetworkHandler.Frequency.EMPTY,
                        ioInfo.ioEntries().getFirst(),
                        null
                );

                final RegistryOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                DataResult<Tag> tagResult = ModuleLinkEntries.ModuleEntry.CODEC.encodeStart(ops, newEntry);
                if (tagResult.isSuccess()) {
                    if (event instanceof ICancellableEvent cancellableEvent)
                        cancellableEvent.setCanceled(true);
                    if (event instanceof PlayerInteractEvent.RightClickBlock rightClickBlock)
                        rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

                    if (!level.isClientSide) {
                        player.displayClientMessage(CreateLang
                                .translate("clipboard.copied_from", Component.literal(pbe.getSelectedModule(player))
                                        .withStyle(ChatFormatting.WHITE))
                                .style(ChatFormatting.GREEN)
                                .component(), true);

                        tag.put("panel_module_frequency", tagResult.getOrThrow());
                        itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent.setType(ClipboardOverrides.ClipboardType.WRITTEN).setCopiedValues(tag));
                    }

                    ci.cancel();
                }
            }
        } else if (!paste && blockEntity instanceof RedstoneLinkBlockEntity linkBE) {
            if (tag.contains("panel_module_frequency") && linkBE.getBehaviour(LinkBehaviour.TYPE) != null) {
                final RegistryOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                final DataResult<Pair<ModuleLinkEntries.ModuleEntry, Tag>> entryResult = ModuleLinkEntries.ModuleEntry.CODEC.decode(ops, tag.get("panel_module_frequency"));

                if (!entryResult.isSuccess())
                    return;

                final ModuleLinkEntries.ModuleEntry entry = entryResult.getOrThrow().getFirst();

                final ModuleLinkEntries.ModuleEntry newEntry = new ModuleLinkEntries.ModuleEntry(
                        linkBE.getBehaviour(LinkBehaviour.TYPE).getNetworkKey().getFirst(),
                        linkBE.getBehaviour(LinkBehaviour.TYPE).getNetworkKey().getSecond(),
                        entry.getEntry(),
                        null
                );

                DataResult<Tag> tagResult = ModuleLinkEntries.ModuleEntry.CODEC.encodeStart(ops, newEntry);
                if (tagResult.isSuccess()) {
                    if (event instanceof ICancellableEvent cancellableEvent)
                        cancellableEvent.setCanceled(true);
                    if (event instanceof PlayerInteractEvent.RightClickBlock rightClickBlock)
                        rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

                    if (!level.isClientSide) {
                        player.displayClientMessage(CreateLang
                                .translate("clipboard.copied_from", level.getBlockState(pos)
                                        .getBlock()
                                        .getName()
                                        .withStyle(ChatFormatting.WHITE))
                                .style(ChatFormatting.GREEN)
                                .component(), true);

                        tag.put("panel_module_frequency", tagResult.getOrThrow());
                        itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent.setType(ClipboardOverrides.ClipboardType.WRITTEN).setCopiedValues(tag));
                    }
                    ci.cancel();
                }
            }
        }
    }
}
