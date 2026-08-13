package moth.boxxed.panels.index;

import moth.boxxed.panels.Dashpanels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PanelSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Dashpanels.MOD_ID);

    public static Supplier<SoundEvent> BUZZ = SOUND_EVENTS.register("buzz",
            () -> SoundEvent.createFixedRangeEvent(Dashpanels.path("buzz"), 8f));
    public static Supplier<SoundEvent> KEY_JINGLE = SOUND_EVENTS.register("key_jingle",
            () -> SoundEvent.createVariableRangeEvent(Dashpanels.path("key_jingle")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
