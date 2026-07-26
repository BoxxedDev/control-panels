package moth.boxxed.panels.event;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.api.module.PlacementManager;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteraction;
import moth.boxxed.panels.api.module.interaction.ModuleHoldInteractionManager;
import moth.boxxed.panels.api.module.tooltip.ModuleTooltipManager;
import moth.boxxed.panels.api.network.ModulesNetworkMember;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.api.panel.PanelModulesHitHandler;
import moth.boxxed.panels.api.panel.PanelType;
import moth.boxxed.panels.api.panel.model.PanelSkinBlockColor;
import moth.boxxed.panels.api.panel.model.PanelSkinModelSwapper;
import moth.boxxed.panels.api.panel.skin.PanelSkinsClientManager;
import moth.boxxed.panels.config.ClientConfig;
import moth.boxxed.panels.content.panel.ceiling.CeilingPanelRenderer;
import moth.boxxed.panels.content.panel.normal.PanelRenderer;
import moth.boxxed.panels.content.panel.wall.WallPanelRenderer;
import moth.boxxed.panels.index.PanelBlockEntities;
import moth.boxxed.panels.index.PanelShaders;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.io.IOException;

@EventBusSubscriber(modid = Dashpanels.MOD_ID, value = Dist.CLIENT)
public class ControlPanelsClientEvents {
    @SubscribeEvent
    public static void guiRenderPost(RenderGuiEvent.Post event) {
        for (ModuleHoldInteraction<?> interaction : ModuleHoldInteractionManager.INTERACTIONS) {
            if (interaction.isActive()) {
                interaction.renderGui(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks());
                return;
            }
        }

        boolean active = false;
        if (ClientConfig.SHOW_MODULE_TOOLTIPS.get() &&
                !ModuleHoldInteractionManager.isActive() &&
                Minecraft.getInstance().hitResult != null &&
                Minecraft.getInstance().hitResult.getType().equals(HitResult.Type.BLOCK)) {
            BlockHitResult blockHitResult = (BlockHitResult) Minecraft.getInstance().hitResult;
            Level level = Minecraft.getInstance().level;
            Player player = Minecraft.getInstance().player;
            if (level.getBlockEntity(blockHitResult.getBlockPos()) instanceof AbstractPanelBlockEntity pbe) {
                Module module = pbe.getModule(pbe.getSelectedModule(player));
                if (!pbe.getSelectedModule(player).isEmpty() && module != null) {
                    ModuleTooltipManager.renderSelected(
                            event.getGuiGraphics(),
                            module
                    );
                    active = true;
                }
            }
        }
        ModuleTooltipManager.guiFrame(active);
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

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        ResourceProvider provider = event.getResourceProvider();
        event.registerShader(
                new ShaderInstance(provider, Dashpanels.path("rendertype_translucent_glow"), DefaultVertexFormat.BLOCK),
                shader -> PanelShaders.translucentGlowShader = shader
        );
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(PanelSkinsClientManager.ReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        PanelSkinModelSwapper.INSTANCE.modifyResult(event);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PanelBlockEntities.PANEL.get(), ctx -> new PanelRenderer());
        event.registerBlockEntityRenderer(PanelBlockEntities.WALL_PANEL.get(), ctx -> new WallPanelRenderer());
        event.registerBlockEntityRenderer(PanelBlockEntities.CEILING_PANEL.get(), ctx -> new CeilingPanelRenderer());
    }

    @SubscribeEvent
    public static void onPreRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
            PlacementManager.frame(event.getCamera().getPosition(), event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        for (PanelType type : PanelType.values()) {
            event.register(new PanelSkinBlockColor(), type.block);
        }
    }
}