package moth.boxxed.panels.compat.create.panel_link.screen;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import moth.boxxed.panels.api.module.io.ModuleIOInfo;
import moth.boxxed.panels.compat.create.PanelCreateRegistries;
import moth.boxxed.panels.compat.create.panel_link.PanelLinkBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.List;

public class PanelLinkMenu extends GhostItemMenu<PanelLinkBlockEntity> {
    public boolean showSlots = false;
    public List<ModuleIOInfo> modulesInfo;

    public PanelLinkMenu(int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(PanelCreateRegistries.PANEL_LINK_MENU.get(), id, inv, extraData);
        this.modulesInfo = extraData.readList(buffer -> ModuleIOInfo.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
    }

    public PanelLinkMenu(int id, Inventory inv, PanelLinkBlockEntity be) {
        super(PanelCreateRegistries.PANEL_LINK_MENU.get(), id, inv, be);
        this.modulesInfo = be.getOrCreate().getCompiledModules().filterIOModules();
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(2);
    }

    @Override
    protected boolean allowRepeats() {
        return false;
    }

    @Override
    protected PanelLinkBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level.getBlockEntity(extraData.readBlockPos()) instanceof PanelLinkBlockEntity be) {
            be.loadClient(extraData.readNbt(), extraData.registryAccess());
            return be;
        }
        return null;
    }

    @Override
    protected void addSlots() {
        this.addPlayerSlots(30, 73);

        this.addSlot(new FakeSlot(this.ghostInventory, 0, 115, 21));
        this.addSlot(new FakeSlot(this.ghostInventory, 1, 138, 21));
    }

    @Override
    protected void addPlayerSlots(int x, int y) {
        for (int i = 0; i<9; i++) {
            this.addSlot(new DefaultSlot(this.playerInventory, i, x + i*18, y+58));
        }

        for (int row=0; row<3; row++) {
            for (int col=0; col<9; col++) {
                this.addSlot(new DefaultSlot(this.playerInventory, col + row*9 + 9, x+col*18, y+row*18));
            }
        }
    }

    @Override
    protected void saveData(PanelLinkBlockEntity contentHolder) {}

    private class DefaultSlot extends Slot {
        public DefaultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return PanelLinkMenu.this.showSlots;
        }
    }

    private class FakeSlot extends SlotItemHandler {
        public FakeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isFake() {
            return true;
        }

        @Override
        public boolean isActive() {
            return PanelLinkMenu.this.showSlots;
        }
    }
}
