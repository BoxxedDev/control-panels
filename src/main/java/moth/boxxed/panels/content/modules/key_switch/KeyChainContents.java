package moth.boxxed.panels.content.modules.key_switch;

import com.mojang.serialization.Codec;
import moth.boxxed.panels.config.PanelsConfig;
import moth.boxxed.panels.index.PanelDataComponents;
import moth.boxxed.panels.util.MathUtil;
import moth.boxxed.panels.util.ShortUUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record KeyChainContents(List<ItemStack> items) implements TooltipComponent, ClientTooltipComponent {
    public static final KeyChainContents EMPTY = new KeyChainContents(List.of());
    public static final Codec<KeyChainContents> CODEC = ItemStack.CODEC.listOf().xmap(KeyChainContents::new, KeyChainContents::items);
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyChainContents> STREAM_CODEC = ItemStack.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(KeyChainContents::new, KeyChainContents::items);

    public KeyChainContents(List<ItemStack> items) {
        this.items = List.copyOf(items.stream().limit(50).toList());
    }

    @Override
    public int getHeight() {
        return this.items.size() * 14;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;
        for (ItemStack key : this.items) {
            if (key.get(PanelDataComponents.BOUND_MODULE) == null)
                continue;

            MutableComponent component = this.compileComponentFor(key);
            if (font.width(component) + 14 > width) {
                width = font.width(component) + 14;
            }
        }

        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {

        for (int i = 0; i < this.items.size(); i++) {
            ItemStack key = this.items.get(i);
            if (key.get(PanelDataComponents.BOUND_MODULE) == null)
                continue;

            guiGraphics.pose().pushPose();
            MathUtil.scaleAround(guiGraphics.pose(), 0.75f, 0.75f, 0.75f, x, y + i * 14, 0);
            guiGraphics.renderItem(key, x, y + i * 14);
            guiGraphics.pose().popPose();

            guiGraphics.drawString(font, this.compileComponentFor(key), x + 14, y + 2 + i * 14, 0xFFFFFF);
        }
    }

    private MutableComponent compileComponentFor(ItemStack key) {
        MutableComponent component = Component.empty();
        component.append(Component.literal("| ").withStyle(ChatFormatting.GOLD));
        component.append(Component.literal(key.get(PanelDataComponents.BOUND_MODULE).uuid().toString()));

        Component customName = key.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            component.append(Component.literal(" | ").withStyle(ChatFormatting.GOLD));
            component.append(customName.copy().withStyle(ChatFormatting.ITALIC));
        }
        return component;
    }

    public static class Mutable {
        private List<ItemStack> items;

        public Mutable(KeyChainContents contents) {
            this.items = new ArrayList<>(contents.items);
        }

        public boolean tryAdd(ItemStack stack) {
            if (stack.isEmpty())
                return false;
            if (this.items.size() < PanelsConfig.MAX_KEYS.get()) {
                this.items.add(stack.copyWithCount(1));
                stack.shrink(1);
                return true;
            }
            return false;
        }

        public ItemStack remove() {
            return this.items.isEmpty() ? null : this.items.removeLast().copy();
        }

        public ItemStack remove(ShortUUID keyId, BlockPos pos) {
            for (int i = 0; i < this.items.size(); i++) {
                ItemStack key = this.items.get(i);
                if (key.get(PanelDataComponents.BOUND_MODULE) == null)
                    continue;

                BoundModule boundModule = key.get(PanelDataComponents.BOUND_MODULE);
                if (boundModule.pos().equals(pos) && boundModule.uuid().equals(keyId)) {
                    return this.items.remove(i).copy();
                }
            }
            return ItemStack.EMPTY;
        }

        public boolean has(ShortUUID keyId, BlockPos pos) {
            for (ItemStack key : this.items) {
                if (key.get(PanelDataComponents.BOUND_MODULE) == null)
                    continue;

                BoundModule boundModule = key.get(PanelDataComponents.BOUND_MODULE);
                if (boundModule.pos().equals(pos) && boundModule.uuid().equals(keyId)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isFull() {
            return this.items.size() == PanelsConfig.MAX_KEYS.get();
        }

        public KeyChainContents immutable() {
            return new KeyChainContents(List.copyOf(this.items));
        }
    }
}
