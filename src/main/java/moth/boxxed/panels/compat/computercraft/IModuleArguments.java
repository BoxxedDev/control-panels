package moth.boxxed.panels.compat.computercraft;

import javax.annotation.Nullable;

public interface IModuleArguments {
    int count();
    @Nullable
    Object get(int index);
    String getType(int index);
    IModuleArguments drop(int count);
}