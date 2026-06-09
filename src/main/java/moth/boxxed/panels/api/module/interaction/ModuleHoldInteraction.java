package moth.boxxed.panels.api.module.interaction;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public abstract class ModuleHoldInteraction<T extends Module> {
    protected T module;
    protected Player player;
    protected Level level;
    protected HoldGuiContext context = new HoldGuiContext();

    public boolean onMouseMove(double yaw, double pitch) {
        if (this.isActive()) {
            if (this.activeMouseMove(yaw, pitch))
                return true;
        }
        return false;
    }

    public abstract boolean activeMouseMove(double yaw, double pitch);

    public void startHold(Level level, Player player, T module) {
        this.module = module;
        this.player = player;
        this.level = level;
        this.context = new HoldGuiContext();
        ModuleHoldInteractionManager.start(this);
    }

    public void start() {
    }

    public void stop() {
    }

    public void release() {
    }
    
    public boolean isActive() {
        return ModuleHoldInteractionManager.isActive(this);
    }

    public boolean chooseInput(int button, int action) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUse.matchesMouse(button)) {
            return this.use(action);
        }
        if (options.keyPickItem.matchesMouse(button)) {
            return this.pick(action);
        }
        if (options.keyAttack.matchesMouse(button)) {
            return this.attack(action);
        }
        return false;
    }

    public boolean pick(int action) {
        if (this.isActive())
            return true;
        return false;
    }

    public boolean attack(int action) {
        if (this.isActive())
            return true;
        return false;
    }

    public boolean use(int action) {
        if (action == GLFW.GLFW_RELEASE && this.isActive()) {
            this.release();
            ModuleHoldInteractionManager.stop();
        }
        return false;
    }

    public HoldGuiContext getGuiContext() {
        return this.context;
    }

    public void renderGui(GuiGraphics graphics, float partialTick) {

    }

    public void tick() {

    }

    protected void update(Integer... values) {
        PacketDistributor.sendToServer(new DefaultModuleUpdatePacket(this.module.getParentPos(), this.module.getName(), List.of(values)));
    }
}
