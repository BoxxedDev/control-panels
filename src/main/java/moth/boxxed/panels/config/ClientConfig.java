package moth.boxxed.panels.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_MODULE_TOOLTIPS = BUILDER
            .comment("Whether to show the hovering tooltips on modules or not")
            .define("showModuleTooltips", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
