package moth.boxxed.panels.event;

import moth.boxxed.panels.ControlPanels;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.api.network.connecting_panels.ModulesNetworkManager;
import moth.boxxed.panels.api.network.connecting_panels.ModulesNetworkMember;
import moth.boxxed.panels.index.PanelHoldInteractions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = ControlPanels.MOD_ID, value = Dist.CLIENT)
public class ControlPanelsClientEvents {
    @SubscribeEvent
    public static void guiRenderPost(RenderGuiEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.renderGui(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks());
            }
        }
    }

    @SubscribeEvent
    public static void clientPostTick(ClientTickEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.tick();
            }
        }
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