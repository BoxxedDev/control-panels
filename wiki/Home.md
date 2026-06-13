## Depending on Dashpanels

### build.gradle

```groovy
    repositories {
        maven {
            url = "https://api.modrinth.com/maven"
        }
    }

    dependencies {
        implementation("maven.modrinth:dashpanels:1.0+neoforge1.21.1")
    }
```
---
### Creating a module
To create a base module just extend the abstract module class
```java
public class MyCustomModule extends Module {
    public MyCustomModule(int x, int y) {
        super(MY_CUSTOM_MODULE.get(), x, y, SIZE_X, SIZE_Y);
    }
}
```
---
### Saving data
To save and load data from the module
```java
public boolean saveData(CompoundTag tag, HolderLookup.Provider registries) {
    tag.put("sometag", sometag);
    return super.saveData(tag, registries);
}

public boolean loadData(CompoundTag tag, HolderLookup.Provider registries) {
    this.someObject = tag.get("sometag");
    return super.loadData(tag, registries);
}
```

The reason for the returning of a boolean is if the loading or saving fails

---
### Rendering
Within the module class there is a render method that is only ran on the client, so if you want some kind of value to be rendered you have to save and load the data so it is loaded onto the client
```java
public void render(PanelBlockEntity panelBlockEntity, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    Do something here
}
```

Now if you want some kind of custom model to render, create a preloaded model, see [PanelPreloadedModels](https://github.com/BoxxedDev/control-panels/blob/master/src/main/java/moth/boxxed/panels/index/PanelPreloadedModels.java)

To register a preloaded model you can do `PreLoadedModel.create(ResourceLocation.tryBuild(MOD_ID, path));`

And then you can render it by calling
```java
STATIC_PRELOADED_MODEL.render(poseStack, bufferSource, RenderType.solid(), packedLight);
```
Also please note you don't have to use the solid rendertype, can use any block render type.

---

### Registering the module
Registering a new module is very similar to registering a [block entity](https://docs.neoforged.net/docs/blockentities/)
<br>
<br>
To start off you need to get the deferred register
```java
public static final DeferredRegister<ModuleType<?>> MODULES = DeferredRegister.create(ModulesRegistry.MODULE_REGISTRY, YOUR_MOD_ID);
```

As well you need to register a base item to piggy back off of. See `PanelItems` [switch declaration](https://github.com/BoxxedDev/control-panels/blob/master/src/main/java/moth/boxxed/panels/index/PanelItems.java#L19)

And then to register a module type do

```java
public static final Supplier<ModuleType<MyCustomModule>> MY_CUSTOM_MODULE = MODULES.register(
        "my_custom_module", () -> new ModuleType<>(MyCustomModule::new, MyItems.MY_CUSTOM_MODULE_ITEM));
```

---

### Module Input Output

#### Please note a module can only be an input, output, multi input, or multi output

---

To make your module an input type (e.g. Switch or Knob) have your module class implement `IInput`
```java
public class MyCustomModule extends Module implements IInput
```

This interface contains a method that returns an analog signal

---

For an output module (e.g. Indicator bulb)
```java
public class MyCustomModule extends Module implements IOutput
```

This interface contains a method that sets an analog signal

---

For a multi input module (e.g. joystick)
```java
public class MyCustomModule extends Module implements IMultiInput
```

The interface contains a method that provides a consumer, for example
```java
public void getValues(BiConsumer<String, AnalogResult> consumer) {
    consumer.accept("val", () -> this.val);
}
```

---

For a multi output module (e.g. screen)
```java
public class MyCustomModule extends Module implements IMultiOutput
```

The interface contains a method that provides a consumer, for example
```java
public void setValues(BiConsumer<String, AnalogRunnable> consumer) {
    consumer.accept("val", analog -> this.val = analog);
}
```

### Hovering tooltips

For custom tooltips when you hover your mouse over a module

```java
public class MyCustomModule extends Module implements IHoverTooltip {
    public void addLines(List<Component> lines) {
        lines.add(Component.literal("Line thingy"));
    }
}
```

---

### Module hold interaction
```java
public class MyModuleHoldInteraction extends ModuleHoldInteraction<MyCustomModule>
```

and then register it by using 
```java
public static final MY_MODULE_HOLD = ModuleHoldInteractionManager.register(new MyHoldInteraction)
```
and then start it in your `onUse` method in your module by doing
```java
if (level.isClientSide && player.isLocalPlayer())
            if (!MyModuleHoldInteraction.MY_MODULE_HOLD.isActive()) {
                MyModuleHoldInteraction.MY_MODULE_HOLD.startHold(level, player, this);
                return InteractionResult.SUCCESS;
            }
```
---