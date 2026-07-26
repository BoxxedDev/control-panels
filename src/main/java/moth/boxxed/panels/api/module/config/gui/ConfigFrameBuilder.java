package moth.boxxed.panels.api.module.config.gui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import moth.boxxed.panels.api.module.config.ModuleConfig;
import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import moth.boxxed.panels.api.module.config.gui.widgets.ConfigFrameWidget;
import moth.boxxed.panels.api.module.config.gui.widgets.EditBoxFrameWidget;
import moth.boxxed.panels.api.module.config.gui.widgets.LabelFrameWidget;
import moth.boxxed.panels.api.module.config.gui.widgets.ValuesButtonFrameWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigFrameBuilder {
    public static final int PADDING = 4;
    
    private final Table<Integer, Integer, ConfigFrameWidget<?>> widgets = HashBasedTable.create();
    private final Map<Integer, Integer> rowHeights = new HashMap<>();
    private final Map<Integer, Integer> columnWidths = new HashMap<>();

    private int currentRow = 0;
    private int currentColumn = 0;

    private int rows = 0;
    private int columns = 0;

    public ConfigFrameBuilder nextRow() {
        this.currentRow++;
        this.currentColumn = 0;

        this.rows = Math.max(this.currentRow, this.rows);
        return this;
    }

    public ConfigFrameBuilder setRow(int row) {
        this.currentRow = row;
        this.currentColumn = 0;

        this.rows = Math.max(row, this.rows);
        return this;
    }

    public ConfigFrameBuilder addWidget(ConfigFrameWidget<?> widget) {
        this.widgets.put(this.currentRow, this.currentColumn, widget);
        if (this.rowHeights.get(this.currentRow) != null) {
            this.rowHeights.put(this.currentRow, Math.max(this.rowHeights.get(this.currentRow), widget.getHeight()));
        } else {
            this.rowHeights.put(this.currentRow, widget.getHeight());
        }
        if (this.columnWidths.get(this.currentColumn) != null) {
            this.columnWidths.put(this.currentColumn, Math.max(this.columnWidths.get(this.currentColumn), widget.getWidth()));
        } else {
            this.columnWidths.put(this.currentColumn, widget.getWidth());
        }

        this.currentColumn++;

        this.columns = Math.max(this.currentColumn, this.columns);

        return this;
    }

    public ConfigFrameBuilder addEmpty() {
        this.currentColumn++;

        this.columns = Math.max(this.currentColumn, this.columns);
        return this;
    }

    public int getRows() {
        return this.rows;
    }
    public int getColumns() {
        return this.columns;
    }

    public int getRowsHeight() {
        int heights = (this.rows-1)*PADDING;
        for (Integer height : this.rowHeights.values()) {
            heights += height;
        }
        return heights;
    }

    public int getColumnsWidth() {
        int widths = (this.rows-1)*PADDING;
        for (Integer width : this.columnWidths.values()) {
            widths += width;
        }
        return widths;
    }

    public int getColumnWidth(int column) {
        return this.columnWidths.get(column) == null ? 0 : this.columnWidths.get(column);
    }

    public int getRowHeight(int row) {
        return this.rowHeights.get(row) == null ? 0 : this.rowHeights.get(row);
    }

    public Table<Integer, Integer, ConfigFrameWidget<?>> getWidgets() {
        return this.widgets;
    }

    //Some util methods
    public <T> ConfigFrameBuilder addValuesButton(ModuleConfigValue<T> value, Supplier<T[]> valuesSupplier, int width) {
        ValuesButtonFrameWidget<T> widget = new ValuesButtonFrameWidget<>(
                value,
                valuesSupplier,
                Minecraft.getInstance().font,
                width,
                16,
                value.getName()
        );
        return this.addWidget(widget);
    }

    public <T> ConfigFrameBuilder addValuesButton(ModuleConfigValue<T> value, T[] values, int width) {
        return this.addValuesButton(value, () -> values, width);
    }

    public <T> ConfigFrameBuilder addEditBox(
            ModuleConfigValue<T> value,
            Function<T, String> onSet,
            Predicate<String> filter,
            BiConsumer<ModuleConfigValue<T>, String> setter,
            int width) {
        EditBoxFrameWidget<T> editBox = new EditBoxFrameWidget<>(
                value,
                onSet,
                filter,
                Minecraft.getInstance().font,
                width,
                16,
                value.getName()
        ).valueSetter(setter);
        this.addWidget(editBox);
        return this;
    }

    public <T> ConfigFrameBuilder addEditBox(ModuleConfigValue<T> value, Function<T, String> onSet, BiConsumer<ModuleConfigValue<T>, String> setter, int width) {
        return this.addEditBox(value, onSet, null, setter, width);
    }

    public <T> ConfigFrameBuilder addIntBox(ModuleConfigValue<T> value, Function<T, String> onSet, BiConsumer<ModuleConfigValue<T>, String> setter, int width) {
        return this.addEditBox(
                value,
                onSet,
                s -> {
                    if (s.isEmpty() || s.equals("-"))
                        return true;
                    try {
                        Integer.parseInt(s);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                setter,
                width);
    }

    public <T> ConfigFrameBuilder addDoubleBox(ModuleConfigValue<T> value, Function<T, String> onSet, BiConsumer<ModuleConfigValue<T>, String> setter, int width) {
        return this.addEditBox(
                value,
                onSet,
                s -> {
                    if (s.isEmpty() || s.equals("-"))
                        return true;
                    try {
                        Double.parseDouble(s);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                setter,
                width);
    }

    public ConfigFrameBuilder addLabel(Component label) {
        this.addWidget(new LabelFrameWidget<>(Minecraft.getInstance().font, label, 0xFFFFFF));
        return this;
    }
}
