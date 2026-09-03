package moth.boxxed.panels.mixin.compat.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import moth.boxxed.panels.api.module.io.ModuleIOInfo;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.compat.create.panel_link.ModuleLinkEntries;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlock;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Green moose purple goose
@Pseudo
@Mixin(ClipboardValueSettingsHandler.class)
public class ClipboardValueSettingsHandlerMixin {
    @Inject(method = "interact",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
            cancellable = true)
    private static void panels$drawCustomSelection(
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
            final DataResult<Pair<ModuleLinkEntries.ModuleEntry, Tag>> entryResult = ModuleLinkEntries.ModuleEntry.CODEC.decode(ops, tag);

            if (!entryResult.isSuccess())
                return;

            final ModuleLinkEntries.ModuleEntry entry = entryResult.getOrThrow().getFirst();
            panelLink.getOrCreate().compileModules();
            if (panelLink.getOrCreate().getCompiledModules().containsKey(entry.getEntry().name()) && panelLinkBlock.openMenu(level, pos, player, entry)) {
                if (event instanceof PlayerInteractEvent.RightClickBlock rightClickBlock)
                    rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.literal("THIS IS PASTING"),
                            true
                    );
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
                    if (event instanceof PlayerInteractEvent.RightClickBlock rightClickBlock)
                        rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.literal("THIS IS COPYING MODULE IO"),
                                true
                        );

                        tag.put("panel_module_frequency", tagResult.getOrThrow());
                        itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent.setType(ClipboardOverrides.ClipboardType.WRITTEN).setCopiedValues(tag));
                    }

                    ci.cancel();
                }
            }
        } else if (!paste && blockEntity instanceof RedstoneLinkBlockEntity linkBE) {
            if (tag.contains("panel_module_frequency") && linkBE.getBehaviour(LinkBehaviour.TYPE) != null) {
                final RegistryOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                final DataResult<Pair<ModuleLinkEntries.ModuleEntry, Tag>> entryResult = ModuleLinkEntries.ModuleEntry.CODEC.decode(ops, tag);

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
                    if (event instanceof PlayerInteractEvent.RightClickBlock rightClickBlock)
                        rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);

                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.literal("THIS IS COPYING FREQUENCIES"),
                                true
                        );

                        tag.put("panel_module_frequency", tagResult.getOrThrow());
                        itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, clipboardContent.setType(ClipboardOverrides.ClipboardType.WRITTEN).setCopiedValues(tag));
                    }

                    ci.cancel();
                }
            }
        }
    }
}
