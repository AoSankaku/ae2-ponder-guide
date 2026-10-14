package dev.ae2guide.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;

/** GuideME CommandLinks execute these locally, before any server command is sent. */
final class GuideCommands {
    private GuideCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ae2ponder")
                .executes(context -> open(GuideBook.lessons().get(0).id()))
                .then(Commands.literal("lesson").then(Commands.argument("id", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                GuideBook.lessons().stream().map(GuideBook.Lesson::id), builder))
                        .executes(context -> open(StringArgumentType.getString(context, "id"))))));
    }

    private static int open(String id) throws CommandSyntaxException {
        if (Minecraft.getInstance().level == null)
            throw new SimpleCommandExceptionType(Component.translatable("ae2ponder.button.requires_world")).create();
        var lesson = GuideBook.lessons().stream().filter(value -> value.id().equals(id)).findFirst()
                .orElseThrow(() -> new DynamicCommandExceptionType(value ->
                        Component.translatable("ae2ponder.command.unknown", value)).create(id));
        GuidePonderScreen.open(lesson);
        return 1;
    }
}
