package moth.boxxed.panels.datagen;

import moth.boxxed.panels.Dashpanels;
import moth.boxxed.panels.index.PanelSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class PanelSoundsProvider extends SoundDefinitionsProvider {
    public PanelSoundsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Dashpanels.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(PanelSounds.BUZZ.get(), definition().subtitle("sounds.dashpanels.buzzer.buzz")
                .with(sound(Dashpanels.path("buzz"))));
    }
}
