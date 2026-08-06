package moth.boxxed.panels.config;

import moth.boxxed.panels.DashpanelsClient;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_MODULE_TOOLTIPS = BUILDER
            .comment("Whether to show the hovering tooltips on modules or not")
            .define("show_module_tooltips", true);

    public static final ModConfigSpec.BooleanValue DISABLE_MODULE_TOOLTIPS_HUD = BUILDER
            .comment("Whether to show the hovering tooltips on modules when the HUD is invisible")
            .define("module_tooltips_hud", false);

    public static final ModConfigSpec.BooleanValue CLICK_FOR_MODULE_HOLD = BUILDER
            .comment("Click to hold a module instead of holding the mouse button")
            .define("click_for_module_hold", false);

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_PALETTE = BUILDER
            .comment("Default palette that loads when you open the paint wheel screen")
            .define("default_skin_palette", "default", (str) -> DashpanelsClient.PALETTE_STORAGE.validateName((String) str));

    public static final ModConfigSpec SPEC = BUILDER.build();
}
