package moth.boxxed.panels.api.module;

import net.minecraft.world.phys.Vec3;

//TODO: add more params/args/whatever records call it use
/**
 *
 * @param location the local location from the top right of the module, relative to the block so on a 2x2 module at a location of (0, 0) the bottom right of it would be at (0.125, y, 0.125)
 */
public record ModuleHitResult(Vec3 location) {

}
