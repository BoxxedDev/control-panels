package moth.boxxed.panels.api.module.config;

import moth.boxxed.panels.api.module.config.gui.ConfigFrameBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ModuleConfigValue<T, R extends ModuleConfigValue<T, R>> {
    protected final String name;
    protected final T defaultValue;
    protected T value;
    protected boolean revertable = true;
    protected Predicate<T> validator = Objects::nonNull;

    protected List<ValueChangedListener<T>> listeners = new ArrayList<>();

    public ModuleConfigValue(String name, @Nonnull T defaultValue) {
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Module config value name cannot contain spaces");
        }

        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public abstract void save(CompoundTag tag, HolderLookup.Provider registries);

    public abstract void load(CompoundTag tag, HolderLookup.Provider registries);

    public void loadAndBroadcastChange(CompoundTag tag, HolderLookup.Provider registries) {
        T oldValue = this.get();
        this.load(tag, registries);
        this.broadcastChange(oldValue);
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        if (this.validator.test(value)) {
            T oldValue = this.value;
            this.value = value;
            broadcastChange(oldValue);
        }
    }

    public T getDefault() {
        return this.defaultValue;
    }

    public Component getName() {
        return Component.translatable("module_config.value.%s".formatted(this.name));
    }

    public String getId() {
        return this.name;
    }

    public void setRevertable(boolean revertable) {
        this.revertable = revertable;
    }

    public boolean isRevertable() {
        return this.revertable;
    }

    @SuppressWarnings("unchecked")
    public R addChangeListener(ValueChangedListener<T> listener) {
        this.listeners.add(listener);
        return (R) this;
    }

    public void removeChangeListener(ValueChangedListener<T> listener) {
        this.listeners.remove(listener);
    }

    public void broadcastChange(T oldValue) {
        this.listeners.forEach(listener -> {
            if (listener != null)
                listener.run(oldValue, this.value);
        });
    }

    @SuppressWarnings("unchecked")
    public R withValidator(Predicate<T> validator) {
        this.validator = validator;
        return (R) this;
    }

    public abstract void buildGuiFrame(ConfigFrameBuilder builder);

    @FunctionalInterface
    public interface ValueChangedListener<T> {
        void run(T oldValue, T newValue);
    }

    public static class BooleanValue extends ModuleConfigValue<Boolean, BooleanValue> {
        public BooleanValue(String name, boolean defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putBoolean("value", this.value);
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            if (!tag.contains("value"))
                return;
            this.value = tag.getBoolean("value");
        }

        @Override
        public void buildGuiFrame(ConfigFrameBuilder builder) {
            builder.addValuesButton(this, new Boolean[]{true, false}, 32);
        }
    }

    public static class IntValue extends ModuleConfigValue<Integer, IntValue> {
        protected final int min;
        protected final int max;

        public IntValue(String name, int defaultValue, int min, int max) {
            super(name, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putInt("value", this.value);
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            if (!tag.contains("value"))
                return;
            this.value = Math.clamp(tag.getInt("value"), this.min, this.max);
        }

        @Override
        public void buildGuiFrame(ConfigFrameBuilder builder) {
            builder.addIntBox(
                    this,
                    String::valueOf,
                    (value, string) -> {
                        try {
                            int num = Integer.parseInt(string);
                            value.set(Math.clamp(num, this.min, this.max));
                        } catch (NumberFormatException ignored) {}
            },
                    32
            );
        }
    }

    public static class StringValue extends ModuleConfigValue<String, StringValue> {
        public StringValue(String name, @NonNull String defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putString("value", this.value);
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            if (!tag.contains("value"))
                return;
            this.value = tag.getString("value");
        }

        @Override
        public void buildGuiFrame(ConfigFrameBuilder builder) {
            builder.addEditBox(this, value -> value, StringValue::set, 128);
        }
    }

    public static class EnumValue<T extends Enum<T> & StringRepresentable> extends ModuleConfigValue<T, EnumValue<T>> {
        private final StringRepresentable.EnumCodec<T> codec;
        private final Supplier<T[]> valuesSupplier;

        public EnumValue(String name, T defaultValue, Supplier<T[]> valuesSupplier) {
            super(name, defaultValue);
            this.codec = StringRepresentable.fromEnum(valuesSupplier);
            this.valuesSupplier = valuesSupplier;
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            var result = this.codec.encodeStart(ops, this.value);
            if (result.isSuccess()) {
                tag.put("value", result.getOrThrow());
            }
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            if (!tag.contains("value"))
                return;
            RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            var result = this.codec.decode(ops, tag.get("value"));
            if (result.isSuccess()) {
                this.value = result.getOrThrow().getFirst();
            }
        }

        @Override
        public void buildGuiFrame(ConfigFrameBuilder builder) {
            builder.addValuesButton(this, this.valuesSupplier, 64);
        }
    }

    public static class Vec3Value extends ModuleConfigValue<Vec3, Vec3Value> {
        public Vec3Value(String name, @NonNull Vec3 defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putDouble("x", this.value.x());
            tag.putDouble("y", this.value.y());
            tag.putDouble("z", this.value.z());
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider registries) {
            if (tag.contains("x") && tag.contains("y") && tag.contains("z")) {
                this.value = new Vec3(
                        tag.getDouble("x"),
                        tag.getDouble("y"),
                        tag.getDouble("z")
                );
            }
        }

        @Override
        public void buildGuiFrame(ConfigFrameBuilder builder) {
            builder.addLabel(Component.literal("X: "));
            builder.addDoubleBox(this,
                    vec3 -> String.valueOf(vec3.x()),
                    (value, string) -> {
                try {
                    Vec3 originalVec3 = value.get();
                    double x = Double.parseDouble(string);
                    value.set(
                            new Vec3(x, originalVec3.y(), originalVec3.z())
                    );
                } catch (NumberFormatException e) {

                }
            }, 32);
            builder.nextRow();

            builder.addLabel(Component.literal("Y: "));
            builder.addDoubleBox(this,
                    vec3 -> String.valueOf(vec3.y()),
                    (value, string) -> {
                try {
                    Vec3 originalVec3 = value.get();
                    double y = Double.parseDouble(string);
                    value.set(
                            new Vec3(originalVec3.x(), y, originalVec3.z())
                    );
                } catch (NumberFormatException e) {

                }
            }, 32);
            builder.nextRow();

            builder.addLabel(Component.literal("Z: "));
            builder.addDoubleBox(this,
                    vec3 -> String.valueOf(vec3.z()),
                    (value, string) -> {
                try {
                    Vec3 originalVec3 = value.get();
                    double z = Double.parseDouble(string);
                    value.set(
                            new Vec3(originalVec3.x(), originalVec3.y(), z)
                    );
                } catch (NumberFormatException e) {

                }
            }, 32);
        }
    }
}