package moth.boxxed.panels.content.modules.buzzer;

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
    protected BuzzSoundInstance(SoundEvent event, SoundSource source, RandomSource randomSource) {
        super(event, source, randomSource);
    }

    @Override
    public void tick() {

    }
}
