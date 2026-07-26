package moth.boxxed.panels.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_MODULE_TOOLTIPS = BUILDER
            .comment("Whether to show the hovering tooltips on modules or not")
            .define("show_module_tooltips", true);

    public static final ModConfigSpec.BooleanValue CLICK_FOR_MODULE_HOLD = BUILDER
            .comment("Click to hold a moduleName instead of holding the mouse button")
            .define("click_for_module_hold", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
