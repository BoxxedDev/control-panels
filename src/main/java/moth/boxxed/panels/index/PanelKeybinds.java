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

    public static final KeyMapping HOLD_MOVE_CAMERA = register(new KeyMapping(
            "key.dashpanels.hold_move_camera",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.dashpanels"
    ));
    public static final KeyMapping HOLD_TO_OPEN_WIKI = register(new KeyMapping(
            "key.dashpanels.hold_to_open_wiki",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.dashpanels"
    ));

    private static KeyMapping register(KeyMapping keyMapping) {
        MAPPINGS.add(keyMapping);
        return keyMapping;
    }

    public static void init() {}
}
