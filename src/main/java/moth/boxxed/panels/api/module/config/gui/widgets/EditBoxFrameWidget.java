package moth.boxxed.panels.api.module.config.gui.widgets;

import moth.boxxed.panels.api.module.config.ModuleConfigValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EditBoxFrameWidget<T, R extends ModuleConfigValue<T, R>> extends EditBox implements ConfigFrameWidget<T, R> {
    protected final R configValue;
    protected final List<BiConsumer<R, String>> setters = new ArrayList<>();
    protected final Function<T, String> onSet;
    protected final ModuleConfigValue.ValueChangedListener<T> listener;

    public EditBoxFrameWidget(R configValue, Function<T, String> onSet, Predicate<String> filter, Font font, int width, int height, Component message) {
        super(font, width, height, message);
        if (filter != null) {
            this.setFilter(filter);
        }
        this.configValue = configValue;

        this.onSet = onSet;
        this.setValue(this.onSet.apply(configValue.get()));

        this.listener = (oldValue, newValue) -> this.setValue(this.onSet.apply(newValue));
        configValue.addChangeListener(this.listener);
    }

    public EditBoxFrameWidget<T, R > valueSetter(BiConsumer<R, String> setter) {
        this.setters.add(setter);
        return this;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.setters.forEach(
                    consumer -> consumer.accept(this.configValue, this.getValue())
            );
            this.setValue(this.onSet.apply(configValue.get()));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.setters.forEach(
                    consumer -> consumer.accept(this.configValue, this.getValue())
            );
            this.setValue(this.onSet.apply(configValue.get()));
            return true;
        }
        return false;
    }

    @Override
    public void onRemove() {
        this.setters.forEach(
                consumer -> consumer.accept(this.configValue, this.getValue())
        );
        this.configValue.removeChangeListener(this.listener);
    }
}
