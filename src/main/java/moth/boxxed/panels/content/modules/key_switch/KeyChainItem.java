package moth.boxxed.panels.content.modules.key_switch;

import moth.boxxed.panels.config.PanelsConfig;
import moth.boxxed.panels.index.PanelDataComponents;
import moth.boxxed.panels.index.PanelSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class KeyChainItem extends Item {
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public KeyChainItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (stack.getCount() != 1 || action != ClickAction.SECONDARY || stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS) == null)
            return false;
        if (slot.allowModification(player)) {
            KeyChainContents.Mutable contents = new KeyChainContents.Mutable(stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS));
            if (!other.isEmpty()) {
                if (other.getItem() instanceof KeyItem && other.has(PanelDataComponents.BOUND_MODULE)) {
                    if (contents.tryAdd(other)) {
                        playJingle(player);
                    }
                }
            } else {
                ItemStack removed = contents.remove();
                if (removed != null) {
                    access.set(removed);
                    playJingle(player);
                }
            }
            stack.set(PanelDataComponents.KEY_CHAIN_CONTENTS, contents.immutable());

            return true;
        }
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (stack.getCount() != 1 || action != ClickAction.SECONDARY)
            return false;

        KeyChainContents contents = stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS);
        if (contents != null) {
            ItemStack slotStack = slot.getItem();
            KeyChainContents.Mutable mutable = new KeyChainContents.Mutable(contents);
            if (slotStack.isEmpty()) {
                ItemStack removedKey = mutable.remove();
                if (removedKey != null) {
                    playJingle(player);
                    mutable.tryAdd(slot.safeInsert(removedKey));
                }
            } else {
                if (!mutable.isFull() && slotStack.getItem() instanceof KeyItem && slotStack.has(PanelDataComponents.BOUND_MODULE)) {
                    if (mutable.tryAdd(slot.safeTake(slotStack.getCount(), 1, player))) {
                        playJingle(player);
                    }
                }
            }

            stack.set(PanelDataComponents.KEY_CHAIN_CONTENTS, mutable.immutable());
            return true;
        }

        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        KeyChainContents contents = stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS);
        if (contents != null && !contents.items().isEmpty()) {
            playJingle(player);
//            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, usedHand);
    }

    public static void playJingle(Entity entity) {
        entity.playSound(PanelSounds.KEY_JINGLE.get(), 1.6f, 0.9f + entity.getRandom().nextFloat()*0.2f);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return !stack.has(DataComponents.HIDE_TOOLTIP) && !stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
                ? Optional.ofNullable(stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS)).map(Function.identity())
                : Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        KeyChainContents contents = stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS);
        if (contents != null) {
            tooltipComponents.add(Component.literal("%d/%d".formatted(contents.items().size(), PanelsConfig.MAX_KEYS.get())).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS) != null && !stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS).items().isEmpty();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS) != null ? Math.round(Mth.clampedMap(stack.get(PanelDataComponents.KEY_CHAIN_CONTENTS).items().size(), 0, PanelsConfig.MAX_KEYS.get(), 0, 13)) : 0;
    }

    public static class ClientExtensions implements IClientItemExtensions {
        private final KeyChainRenderer renderer = new KeyChainRenderer();

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    }
}
