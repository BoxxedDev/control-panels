package moth.boxxed.panels.content.modules.buzzer;

import moth.boxxed.panels.config.PanelsClientConfig;
import moth.boxxed.panels.index.PanelSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BuzzSoundInstance extends AbstractTickableSoundInstance {
    private int lifeLeft;
    private boolean active;

    protected BuzzSoundInstance(BlockPos pos) {
        super(PanelSounds.BUZZ.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.active = true;
        this.delay = 0;
        this.volume = 0.15f;

        Vec3 posVec = pos.getCenter();
        this.x = posVec.x();
        this.y = posVec.y();
        this.z = posVec.z();
    }

    public void resetLife() {
        this.lifeLeft = 2;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public void tick() {
        if (this.active) {
            lifeLeft--;
            if (lifeLeft == 0) {
                this.deactivate();
            }

            Player player = Minecraft.getInstance().player;
            this.volume = (float) Mth.map(
                    player.distanceToSqr(this.x, this.y, this.z),
                    0, PanelsClientConfig.BUZZ_SFX_RANGE.get()* PanelsClientConfig.BUZZ_SFX_RANGE.get(),
                    0.15f, 0
            );
        } else {
            this.stop();
        }
    }

    public void setPitch(float p) {
        this.pitch = p;
    }
}
