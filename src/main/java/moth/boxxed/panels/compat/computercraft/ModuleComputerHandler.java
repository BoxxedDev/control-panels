package moth.boxxed.panels.compat.computercraft;

import dan200.computercraft.api.peripheral.AttachedComputerSet;
import dan200.computercraft.api.peripheral.IComputerAccess;
import moth.boxxed.panels.api.module.Module;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public class ModuleComputerHandler {
    private final AttachedComputerSet computerSet = new AttachedComputerSet();
    private final Module module;

    public <T extends Module> ModuleComputerHandler(T module) {
        this.module = module;
    }

    public void attach(IComputerAccess computer) {
        this.computerSet.add(computer);
    }

    public void detach(IComputerAccess computer) {
        this.computerSet.remove(computer);
    }

    /**
     * For module specific events, queues two events.
     * One in the form of {@code os.pullEvent("[module name].[event]")}, it will have the args just there.
     * The second in the form of {@code os.pullEvent("[event]")}, it will provide the module name and then the args
     *
     * @param event
     * @param args
     */
    public void queueModuleEvent(String event, @Nullable Object... args) {
        this.queueEvent(this.module.getName() + "." + event, args);

        //There might be a better way but idk
        Object[] argsWithModuleName = new Object[args.length + 1];
        argsWithModuleName[0] = this.module.getName();
        System.arraycopy(args, 0, argsWithModuleName, 1, args.length);
        this.queueEvent(event, argsWithModuleName);
    }

    /**
     * Just for normal queuing of events, like if you wanna implement just a mouse click event or something like that.
     *
     * @param event
     * @param args
     */
    public void queueEvent(String event, @Nullable Object... args) {
        this.computerSet.queueEvent(event, args);
    }
}
