package moth.boxxed.panels.api.module.tooltip;

import moth.boxxed.panels.api.module.ModuleHitResult;

//Just made this a record in case I ever wanna add more stuff, if you wanna mixin to this or smn to add more parameters good luck because I have no idea how that works.
public record TooltipContext(ModuleHitResult hitResult) {
}
