package moth.boxxed.panels.content.modules.buzzer;

import moth.boxxed.panels.index.PanelSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public class BuzzSoundInstance extends AbstractTickableSoundInstance {
    protected BuzzSoundInstance(RandomSource randomSource) {
        super(PanelSounds.BUZZ.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
    }

    @Override
    public void tick() {

    }
}
