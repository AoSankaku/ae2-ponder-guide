package dev.ae2guide.mixin;

import dev.ae2guide.client.RightToLeftText;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.TextWindowElement;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Keep Arabic subtitles and their windows on the same shaped-glyph metrics. */
@Mixin(value = TextWindowElement.class, remap = false)
public abstract class PonderTextWindowMixin {
    @Unique private boolean ae2ponder$rightToLeft;
    @Unique private Font ae2ponder$font;
    @Unique private int ae2ponder$lineCount;

    @Inject(method = "render", at = @At(value = "HEAD", remap = false), remap = false)
    private void ae2ponder$selectLayout(PonderScene scene, PonderUI screen, GuiGraphics graphics,
                                        float partialTicks, float fade, CallbackInfo ci) {
        ae2ponder$rightToLeft = scene.getNamespace().equals("ae2ponder") && Language.getInstance().isDefaultRightToLeft();
        ae2ponder$font = screen.getFontRenderer();
    }

    @Redirect(method = "render", remap = false, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/StringSplitter;splitLines(Ljava/lang/String;ILnet/minecraft/network/chat/Style;)Ljava/util/List;", remap = true))
    private List<FormattedText> ae2ponder$wrap(StringSplitter splitter, String text, int width, Style style) {
        var lines = ae2ponder$rightToLeft ? RightToLeftText.wrap(ae2ponder$font, text, width, style)
                : splitter.splitLines(text, width, style);
        ae2ponder$lineCount = lines.size();
        return lines;
    }

    @Redirect(method = "render", remap = false, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I", remap = true))
    private int ae2ponder$measure(Font font, FormattedText line) {
        return ae2ponder$rightToLeft ? RightToLeftText.width(font, line.getString()) : font.width(line);
    }

    @Redirect(method = "render", remap = false, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/Font;wordWrapHeight(Ljava/lang/String;I)I", remap = true))
    private int ae2ponder$height(Font font, String text, int width) {
        return ae2ponder$rightToLeft ? ae2ponder$lineCount * font.lineHeight : font.wordWrapHeight(text, width);
    }

    @Redirect(method = "render", remap = false, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", remap = true))
    private int ae2ponder$draw(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean shadow) {
        if (!ae2ponder$rightToLeft) return graphics.drawString(font, text, x, y, color, shadow);
        // The String overload shapes again; draw the measured visual order directly.
        String shaped = font.bidirectionalShaping(text);
        FormattedCharSequence visual = sink -> StringDecomposer.iterateFormatted(shaped, Style.EMPTY, sink);
        return graphics.drawString(font, visual, x, y, color, shadow);
    }
}
