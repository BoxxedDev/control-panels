package moth.boxxed.panels.api.module.interaction;

import moth.boxxed.panels.index.PanelKeybinds;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class ModuleHoldInteractionManager {
    public static final Set<ModuleHoldInteraction<?>> INTERACTIONS = new HashSet<>();
    private static boolean canMoveCamera = false;

    private static ModuleHoldInteraction active = null;

    public static boolean isActive() {
        return active != null;
    }

    public static boolean isActive(ModuleHoldInteraction moduleHoldInteraction) {
        return moduleHoldInteraction == active;
    }

    public static void start(ModuleHoldInteraction interaction) {
        if (active != null) {
            active.stop();
        }

        canMoveCamera = false;

        active = interaction;
        active.start();
    }

    public static void stop() {
        if (active != null) {
            active.stop();
            active = null;
        }

        canMoveCamera = false;
    }

    public static boolean onMouseMove(double yaw, double pitch) {
        if (Minecraft.getInstance().screen != null) return false;
        if (canMoveCamera) {
            return false;
        }
        for (ModuleHoldInteraction<?> interaction : INTERACTIONS) {
            if (interaction.onMouseMove(yaw, pitch)) {
                return true;
            }
        }
        return false;
    }

    public static boolean beforeMouseInput(int button, int action) {
        if (Minecraft.getInstance().screen != null) return false;
        for (ModuleHoldInteraction<?> interaction : INTERACTIONS) {
            if (interaction.chooseInput(button, action)) {
                return true;
            }
        }
        return false;
    }

    public static boolean beforeKeyInput(int key, int scanCode, int action, int modifiers) {
        if (Minecraft.getInstance().screen != null || !isActive()) return false;
        if (action == GLFW.GLFW_REPEAT) return false;
        if (PanelKeybinds.HOLD_MOVE_CAMERA.matches(key, scanCode)) {
            if (action == GLFW.GLFW_PRESS) {
                canMoveCamera = true;
            } else {
                canMoveCamera = false;
            }
            return true;
        }
        for (ModuleHoldInteraction<?> interaction : INTERACTIONS) {
            if (interaction.isActive()) {
                if (action == GLFW.GLFW_PRESS && interaction.keyPress(key, scanCode, modifiers))
                    return true;
                if (action == GLFW.GLFW_RELEASE && interaction.keyRelease(key, scanCode, modifiers))
                    return true;
            }
        }
        return false;
    }

    public static void tick() {
        for (ModuleHoldInteraction<?> interaction : ModuleHoldInteractionManager.INTERACTIONS) {
            if (interaction.isActive()) {
                if (interaction.stillValid()) {
                    interaction.tick();
                } else {
                    stop();
                }
            }
        }
    }

    public static <T extends ModuleHoldInteraction<?>> T register(T interaction) {
        INTERACTIONS.add(interaction);
        return interaction;
    }
}