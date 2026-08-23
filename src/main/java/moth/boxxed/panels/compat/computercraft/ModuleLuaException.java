package moth.boxxed.panels.compat.computercraft;

public class ModuleLuaException extends Exception {
    private final int level;
    private final boolean hasLevel;

    public ModuleLuaException(String message) {
        super(message);
        this.hasLevel = false;
        this.level = 1;
    }

    public ModuleLuaException(String message, int level) {
        super(message);
        this.hasLevel = true;
        this.level = level;
    }

    public boolean hasLevel() {
        return this.hasLevel;
    }

    public int getLevel() {
        return this.level;
    }
}
