package moth.boxxed.panels.event;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.IHoverTooltip;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.ModuleTooltipManager;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteractionManager;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.config.ClientConfig;
import moth.boxxed.panels.content.panel.PanelBlock;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.content.panel.PanelModulesHitHandler;
import moth.boxxed.panels.index.PanelHoldInteractions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Dashpanels.MOD_ID, value = Dist.CLIENT)
public class ControlPanelsClientEvents {
    @SubscribeEvent
    public static void guiRenderPost(RenderGuiEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : ModuleHoldInteractionManager.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.renderGui(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks());
            }
        }
        if (ClientConfig.SHOW_MODULE_TOOLTIPS.get() &&
                !ModuleHoldInteractionManager.isActive() &&
                Minecraft.getInstance().hitResult != null &&
                Minecraft.getInstance().hitResult.getType().equals(HitResult.Type.BLOCK)) {
            BlockHitResult blockHitResult = (BlockHitResult) Minecraft.getInstance().hitResult;
            Level level = Minecraft.getInstance().level;
            if (level.getBlockEntity(blockHitResult.getBlockPos()) instanceof PanelBlockEntity pbe) {
                Module module = pbe.getModule(pbe.getSelectedModule());
                if (!pbe.getSelectedModule().isEmpty() && module != null) {
                    BlockPos pos = pbe.getBlockPos();
                    Direction direction = pbe.getBlockState().getValue(PanelBlock.FACING);
                    Quaternionf rotationQuat = direction.getRotation();
                    Vector3f globalModulePos = new Vector3f(module.getPos().x/16f, 0.75f, module.getPos().y/16f);
                    rotationQuat.transform(globalModulePos);
                    globalModulePos.add(pos.getX(), pos.getY(), pos.getZ());
//                    ModuleTooltipManager.renderSelected(
//                            event.getGuiGraphics(),
//                            event.getPartialTick().getGameTimeDeltaPartialTick(true),
//                            module,
//                            globalModulePos
//                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void clientPostTick(ClientTickEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : ModuleHoldInteractionManager.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.tick();
            }
        }
    }

    @SubscribeEvent
    public static void clientPreTick(ClientTickEvent.Pre event) {
        PanelModulesHitHandler.clearNear();
    }

    @SubscribeEvent
    public static void debugText(CustomizeGuiOverlayEvent.DebugText event) {
        event.getRight().add("");
        event.getRight().add(ChatFormatting.UNDERLINE + "Control Panels");
        
        HitResult hit = Minecraft.getInstance().hitResult;
        ClientLevel level = Minecraft.getInstance().level;
        if (hit == null) return;
        if (level == null) return;
        if (!(hit instanceof BlockHitResult blockHit)) return;

        BlockEntity be = level.getBlockEntity(blockHit.getBlockPos());
        if (!(be instanceof ModulesNetworkMember eminem)) return;

        event.getRight().add("Network: %s".formatted(eminem.network));
    }
}