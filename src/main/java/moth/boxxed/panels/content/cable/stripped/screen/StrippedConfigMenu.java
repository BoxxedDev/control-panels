package moth.boxxed.panels.content.cable.stripped.screen;

import moth.boxxed.panels.api.module.ModuleMap;
import moth.boxxed.panels.api.module.io.ModuleIOInfo;
import moth.boxxed.panels.index.PanelMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StrippedConfigMenu extends AbstractContainerMenu {
    public List<ModuleIOInfo> modulesInfo;
    public BlockPos pos;
    public String initialConfig;

    public StrippedConfigMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(PanelMenuTypes.STRIPPED_CONFIG.get(), containerId);
        init(extraData.readList(buffer -> ModuleIOInfo.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer)), extraData.readBlockPos(), extraData.readUtf());
    }

    public StrippedConfigMenu(int containerId, List<ModuleIOInfo> modulesInfo, BlockPos pos, String initialConfig) {
        super(PanelMenuTypes.STRIPPED_CONFIG.get(), containerId);
        init(modulesInfo, pos, initialConfig);
    }

    private void init(List<ModuleIOInfo> map, BlockPos pos, String initialConfig) {
        this.modulesInfo = map;
        this.pos = pos;
        this.initialConfig = initialConfig;

        broadcastChanges();
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
