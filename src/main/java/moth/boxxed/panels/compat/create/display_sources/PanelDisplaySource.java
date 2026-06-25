package moth.boxxed.panels.compat.create.display_sources;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class PanelDisplaySource extends DisplaySource {
    @Override
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (!(context.getSourceBlockEntity() instanceof AbstractPanelBlockEntity pbe)) return;

        if (!pbe.getModules().isEmpty()) {
            if (isFirstLine) {
                builder.addSelectionScrollInput(0, 137,
                        (input, label) -> input.forOptions(compileModules(context)),
                        "PanelModules"
                );
                return;
            }

            builder.addSelectionScrollInput(0, 137,
                    (input, label) -> input.forOptions(compileModuleVariables(context)),
                    "ModuleVariable"
            );
        }
    }

    private List<Component> compileModuleVariables(DisplayLinkContext context) {
        return List.of(Component.literal("%d".formatted(context.sourceConfig().getInt("PanelModules"))));
    }

    private List<Component> compileModules(DisplayLinkContext context) {
        List<Component> ret = new ArrayList<>();

        if (!(context.getSourceBlockEntity() instanceof AbstractPanelBlockEntity pbe))
            return ret;
        List<String> keyList = new ArrayList<>(pbe.getModules().keySet());
        keyList.sort(null);
        for (String module : keyList) {
            ret.add(Component.literal(module));
        }
        return ret;
    }

    @Override
    protected String getTranslationKey() {
        return "control_panel";
    }

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        return List.of();
    }
}
