package moth.boxxed.panels.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PanelsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_KEYS = BUILDER
            .defineInRange("max_key_chain_amount", 5, 3, 10);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
