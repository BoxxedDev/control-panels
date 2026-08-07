package moth.boxxed.panels.api.module.interaction;

import moth.boxxed.panels.api.module.Module;
import moth.boxxed.panels.config.ClientConfig;
import moth.boxxed.panels.network.packet.DefaultModuleUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public abstract class ModuleHoldInteraction<T extends Module> {
    protected T module;
    protected Player player;
    protected Level level;
    protected HoldGuiContext context = new HoldGuiContext();
    protected BlockPos pos;

    public boolean onMouseMove(double yaw, double pitch) {
        if (this.isActive()) {
            if (this.activeMouseMove(yaw, pitch))
                return true;
        }
        return false;
    }

    public abstract boolean activeMouseMove(double yaw, double pitch);

    public final void startHold(Level level, Player player, T module) {
        this.module = module;
        this.pos = this.module.getParentPos();
        this.player = player;
        this.level = level;
        this.context = new HoldGuiContext();
        ModuleHoldInteractionManager.start(this);
    }

    public final boolean stillValid() {
        if (this.isActive()) {
            double reach = this.player.blockInteractionRange() + 2d;
            return this.player.distanceToSqr(this.pos.getCenter()) <= reach * reach;
        }
        return false;
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

    public final boolean chooseInput(int button, int action) {
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
        boolean pressAgain = ClientConfig.CLICK_FOR_MODULE_HOLD.get() && action == GLFW.GLFW_PRESS;
        boolean release = !ClientConfig.CLICK_FOR_MODULE_HOLD.get() && action == GLFW.GLFW_RELEASE;
        if ((pressAgain || release) && this.isActive()) {
            this.release();
            ModuleHoldInteractionManager.stop();
            if (pressAgain)
                return true;
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

    protected final void update(CompoundTag tag) {
        PacketDistributor.sendToServer(new DefaultModuleUpdatePacket(this.module.getParentPos(), this.module.getName(), tag));
    }

    public boolean keyPress(int key, int scanCode, int modifiers) {
        return false;
    }

    public boolean keyRelease(int key, int scanCode, int modifiers) {
        return false;
    }
}
