package dev.ae2guide.mixin;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.ponder.foundation.ui.PonderUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Sets the initial lesson without animating through the lessons before it. */
@Mixin(value = PonderUI.class, remap = false)
public interface PonderUIAccessor {
    @Accessor("index") void ae2ponder$setIndex(int index);
    @Accessor("lazyIndex") LerpedFloat ae2ponder$getLazyIndex();
}
