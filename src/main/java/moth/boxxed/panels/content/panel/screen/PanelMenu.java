package moth.boxxed.panels.content.panel.screen;

import moth.boxxed.panels.api.module.ModuleType;
import moth.boxxed.panels.api.registry.ModulesRegistry;
import moth.boxxed.panels.content.panel.PanelBlockEntity;
import moth.boxxed.panels.index.PanelMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Set;

public class PanelMenu extends AbstractContainerMenu {
    public Inventory inventory;
    public PanelBlockEntity holder;

    public PanelMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(PanelMenuTypes.PANEL.get(), containerId);
        init(inv, createOnClient(extraData));
    }

    public PanelMenu(int containerId, Inventory inv, PanelBlockEntity be) {
        super(PanelMenuTypes.PANEL.get(), containerId);
        init(inv, be);
    }

    @OnlyIn(Dist.CLIENT)
    private PanelBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
        ClientLevel level = Minecraft.getInstance().level;

        BlockEntity be = level.getBlockEntity(extraData.readBlockPos());
        if (be instanceof PanelBlockEntity pbe) {
            pbe.loadClient(extraData.readNbt(), extraData.registryAccess());
            return pbe;
        }
        return null;
    }

    private void init(Inventory inv, PanelBlockEntity be) {
        this.inventory = inv;
        this.holder = be;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 142 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 200));
        }

        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
