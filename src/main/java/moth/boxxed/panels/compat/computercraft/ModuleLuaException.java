package moth.boxxed.panels.compat.computercraft;

import moth.boxxed.panels.api.module.Module;

public class ModuleLuaException {
    private final String message;
    private final int level;
    private final boolean hasLevel;

    public ModuleLuaException(String message) {
        this(message, 1, false);
    }

    public ModuleLuaException(String message, int level) {
        this(message, level, true);
    }

    private ModuleLuaException(String message, int level, boolean hasLevel) {
        this.message = message;
        this.level = level;
        this.hasLevel = hasLevel;
    }

    public String getMessage() {
        return this.message;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean hasLevel() {
       return this.hasLevel;
    }
}
