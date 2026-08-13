### Creating a module
To create a base module just extend the abstract module class
```java
public class MyCustomModule extends Module {
    public MyCustomModule(int x, int y) {
        super(MY_CUSTOM_MODULE.get(), x, y);
    }
}
```

Once your custom module class is created you must register, registering the module is similar to registering a [block entity](https://docs.neoforged.net/docs/1.21.1/blockentities/) and a [block](https://docs.neoforged.net/docs/1.21.1/blocks/)

```java
public static final DeferredRegister<ModuleType<?>> MODULES = DeferredRegister.create(ModulesRegistry.MODULE_REGISTRY, YOUR_MOD_ID);

...

public static final Supplier<ModuleType<MyCustomModule>> MY_CUSTOM_MODULE = MODULES.register(
        "my_custom_module", () -> new ModuleType<>(MyCustomModule::new, MyItems.MY_CUSTOM_MODULE_ITEM));
```