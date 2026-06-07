package moth.boxxed.panels.compat.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import moth.boxxed.panels.compat.PanelCompat;
import moth.boxxed.panels.compat.computercraft.peripherals.NetworkMemberPeripheral;
import moth.boxxed.panels.index.PanelBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CCPeripherals implements PanelCompat {
    public static final List<SupplierGetterPair<BlockEntity, IPeripheral>> PERIPHERALS = new ArrayList<>();

    @Override
    public void init() {
        this.addPeripheral(PanelBlockEntities.PANEL, NetworkMemberPeripheral::new);
        this.addPeripheral(PanelBlockEntities.CABLE, NetworkMemberPeripheral::new);
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> void addPeripheral(Supplier<BlockEntityType<T>> type, CCGetter<T, IPeripheral> getter) {
        PERIPHERALS.add((SupplierGetterPair<BlockEntity, IPeripheral>) new SupplierGetterPair<>(type, getter));
    }

    @Override
    public String id() {
        return "computercraft";
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (SupplierGetterPair<BlockEntity, IPeripheral> pair : CCPeripherals.PERIPHERALS) {
            event.registerBlockEntity(PeripheralCapability.get(), pair.supplier.get(), (be, dir) -> pair.getter.get(be));
        }
    }

    @FunctionalInterface
    private interface CCGetter<T extends BlockEntity, V> {
        V get(T be);
    }

    public record SupplierGetterPair<T extends BlockEntity, V>(Supplier<BlockEntityType<T>> supplier, CCGetter<T, V> getter) {}
}
