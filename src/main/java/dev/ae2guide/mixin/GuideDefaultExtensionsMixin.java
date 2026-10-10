package dev.ae2guide.mixin;

import dev.ae2guide.client.OptionalModTagCompiler;
import guideme.compiler.TagCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** Adds one namespaced compiler while preserving GuideME's immutable built-in compiler list. */
@Pseudo
@Mixin(targets = "guideme.internal.extensions.DefaultExtensions", remap = false)
public abstract class GuideDefaultExtensionsMixin {
    @Inject(method = "tagCompilers", at = @At("RETURN"), cancellable = true, remap = false)
    private static void ae2ponder$addOptionalModCompiler(CallbackInfoReturnable<List<TagCompiler>> callback) {
        var compilers = new ArrayList<>(callback.getReturnValue());
        if (compilers.stream().noneMatch(compiler -> compiler instanceof OptionalModTagCompiler))
            compilers.add(new OptionalModTagCompiler());
        callback.setReturnValue(List.copyOf(compilers));
    }
}
