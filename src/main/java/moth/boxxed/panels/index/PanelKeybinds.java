package moth.boxxed.panels.index;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

//These will probably be deleted in version 2
public class PanelKeybinds {
    public static final Set<KeyMapping> MAPPINGS = new HashSet<>();

    public static final KeyMapping DELETE_MODULE_MAPPING = register(new KeyMapping(
            "key.dashpanels.delete_module",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_D,
            "key.categories.dashpanels"
    ));
    public static final KeyMapping SELECT_MODULE_MAPPING = register(new KeyMapping(
            "key.dashpanels.select_module",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "key.categories.dashpanels"
    ));
    public static final KeyMapping MOVE_MODULE_MAPPING = register(new KeyMapping(
            "key.dashpanels.move_module",
            KeyConflictContext.GUI,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            "key.categories.dashpanels"
    ));
    public static final KeyMapping HOLD_MOVE_CAMERA = register(new KeyMapping(
            "key.dashpanels.hold_move_camera",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.dashpanels"
    ));

    private static KeyMapping register(KeyMapping keyMapping) {
        MAPPINGS.add(keyMapping);
        return keyMapping;
    }

    public static void init() {}
}
