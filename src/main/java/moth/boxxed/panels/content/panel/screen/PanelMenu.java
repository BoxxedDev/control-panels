package moth.boxxed.panels.content.panel.screen;

import com.google.common.collect.Sets;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

public class PanelMenu extends AbstractContainerMenu {
    public Inventory inventory;
    public AbstractPanelBlockEntity holder;
    public Set<String> takenNames;

    public PanelMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(PanelMenuTypes.PANEL.get(), containerId);
        init(inv, createOnClient(extraData), extraData.readCollection(Sets::newHashSetWithExpectedSize, ByteBufCodecs.STRING_UTF8));
    }

    public PanelMenu(int containerId, Inventory inv, AbstractPanelBlockEntity be) {
        super(PanelMenuTypes.PANEL.get(), containerId);
        init(inv, be, new HashSet<>(be.getOrCreate().getCompiledModules().keySet()));
    }

    @OnlyIn(Dist.CLIENT)
    private AbstractPanelBlockEntity createOnClient(RegistryFriendlyByteBuf extraData) {
        ClientLevel level = Minecraft.getInstance().level;

        BlockEntity be = level.getBlockEntity(extraData.readBlockPos());
        if (be instanceof AbstractPanelBlockEntity pbe) {
            pbe.loadClient(extraData.readNbt(), extraData.registryAccess());
            return pbe;
        }
        return null;
    }

    private void init(Inventory inv, AbstractPanelBlockEntity be, Set<String> takenNames) {
        this.inventory = inv;
        this.holder = be;
        this.takenNames = takenNames;

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
