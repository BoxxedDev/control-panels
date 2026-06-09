package moth.boxxed.panels.api.module.interaction;

import moth.boxxed.panels.index.PanelHoldInteractions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ModuleHoldInteractionManager {
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

        active = interaction;
        active.start();
    }

    public static void stop() {
        if (active != null) {
            active.stop();
            active = null;
        }
    }

    public static boolean onMouseMove(double yaw, double pitch) {
        if (Minecraft.getInstance().screen != null) return false;
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.onMouseMove(yaw, pitch)) {
                return true;
            }
        }
        return false;
    }

    public static boolean beforeMouseInput(int button, int action) {
        if (Minecraft.getInstance().screen != null) return false;
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.chooseInput(button, action)) {
                return true;
            }
        }
        return false;
    }

    public static boolean beforeKeyInput(int key, int scanCode, int action, int modifiers) {
        if (Minecraft.getInstance().screen != null) return false;
        if (action == GLFW.GLFW_REPEAT) return false;
        for (ModuleHoldInteraction<?> interaction : PanelHoldInteractions.INTERACTIONS) {
            if (interaction.isActive()) {
                if (action == GLFW.GLFW_PRESS && interaction.keyPress(key, scanCode, modifiers))
                    return true;
                if (action == GLFW.GLFW_RELEASE && interaction.keyRelease(key, scanCode, modifiers))
                    return true;
            }
        }
        return false;
    }
}
