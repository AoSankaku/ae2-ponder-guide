package dev.ae2guide.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class GuideScenes {
    private static final int FLOW_TICKS = 36;
    private static final int CALLOUT_SLOT_TICKS = 72; // 60 readable ticks + fades and a short gap.
    private GuideScenes() {}

    public static void build(GuideBook.Lesson lesson, SceneBuilder scene, SceneBuildingUtil util) {
        scene.title(lesson.id(), lesson.title().en());
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.85f);
        for (var node : lesson.nodes()) {
            scene.world().setBlock(node.position(), state(node), false);
            applyNbt(scene, util, node, node.nbt());
        }
        scene.showBasePlate();
        scene.idle(15);
        for (int i = 0; i < lesson.steps().size(); i++) {
            buildStep(lesson, i, scene, util);
        }
        scene.markAsFinished();
    }

    private static void buildStep(GuideBook.Lesson lesson, int index, SceneBuilder scene, SceneBuildingUtil util) {
        var step = lesson.steps().get(index);
        scene.addKeyframe();
        step.hide().forEach(id -> scene.world().hideSection(util.select().position(lesson.node(id).position()), Direction.UP));
        step.show().forEach(id -> scene.world().showSection(util.select().position(lesson.node(id).position()), Direction.DOWN));
        step.nbt().forEach((id, nbt) -> applyNbt(scene, util, lesson.node(id), nbt));
        scene.idle(12);
        // Give longer translations additional reading time; Ponder also provides pauses/keyframes.
        int duration = captionDuration(lesson, index);
        var color = step.warning() ? PonderPalette.RED : PonderPalette.BLUE;
        for (var id : step.focus()) {
            scene.overlay().showOutline(color, "focus_" + id, util.select().position(lesson.node(id).position()), duration);
        }
        var caption = scene.overlay().showText(duration)
                .sharedText(ResourceLocation.fromNamespaceAndPath("ae2ponder", lesson.id() + "_" + index))
                .colored(color);
        if (step.focus().isEmpty()) caption.independent();
        else caption.pointAt(Vec3.atCenterOf(lesson.node(step.focus().getFirst()).position())).placeNearTarget();
        // Show one ingredient/tool at a time, keeping each bubble clear of the next one.
        int slots = Math.max(1, step.callouts().size());
        int slotDuration = duration / slots;
        for (int slot = 0; slot < slots; slot++) {
            if (!step.callouts().isEmpty()) callout(lesson, step.callouts().get(slot), scene, slotDuration);
            int elapsed = 0;
            if (slot == 0 && step.flow() != null) {
                flow(lesson, step.flow(), scene);
                elapsed = FLOW_TICKS;
            }
            scene.idle(slotDuration - elapsed);
        }
        scene.idle(duration % slots + 12);
    }

    public static int captionDuration(GuideBook.Step step) {
        return Math.max(Math.max(120, step.callouts().size() * CALLOUT_SLOT_TICKS),
                Math.max(step.text().ja().length() * 3, step.text().en().length()));
    }

    public static int captionDuration(GuideBook.Lesson lesson, int index) {
        String caption = Component.translatable("ae2ponder.ponder.shared." + lesson.id() + "_" + index).getString();
        return Math.max(captionDuration(lesson.steps().get(index)), caption.codePointCount(0, caption.length()));
    }

    private static void callout(GuideBook.Lesson lesson, GuideBook.Callout callout, SceneBuilder scene, int duration) {
        var node = lesson.node(callout.anchor());
        var offset = callout.offset();
        var position = Vec3.atCenterOf(node.position()).add(offset.get(0), 0.5 + offset.get(1), offset.get(2));
        var id = ResourceLocation.parse(callout.item());
        if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalArgumentException("Unknown callout item: " + id);
        // Ponder adds five ticks each for fade-in/out; leave two ticks before the following bubble.
        var bubble = scene.overlay().showControls(position, Pointing.valueOf(callout.pointing().toUpperCase(Locale.ROOT)), duration - 12)
                .withItem(new ItemStack(BuiltInRegistries.ITEM.get(id)));
        switch (callout.action()) {
            case "right_click" -> bubble.rightClick();
            case "left_click" -> bubble.leftClick();
            case "scroll" -> bubble.scroll();
            default -> { }
        }
        if (callout.sneak()) bubble.whileSneaking();
    }

    private static void flow(GuideBook.Lesson lesson, GuideBook.Flow flow, SceneBuilder scene) {
        var from = Vec3.atCenterOf(lesson.node(flow.from()).position()).add(0, 0.65, 0);
        var to = Vec3.atCenterOf(lesson.node(flow.to()).position()).add(0, 0.65, 0);
        scene.overlay().showBigLine(PonderPalette.GREEN, from, to, FLOW_TICKS + 12);
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(flow.item()));
        var link = scene.world().createItemEntity(from, Vec3.ZERO, new ItemStack(item));
        scene.world().modifyEntity(link, entity -> { entity.setNoGravity(true); entity.noPhysics = true; });
        for (int tick = 1; tick <= FLOW_TICKS; tick++) {
            var position = from.lerp(to, tick / (double) FLOW_TICKS);
            scene.world().modifyEntity(link, entity -> entity.setPos(position));
            scene.idle(1);
        }
        scene.world().modifyEntity(link, entity -> entity.discard());
    }

    private static void applyNbt(SceneBuilder scene, SceneBuildingUtil util, GuideBook.Node node, String snbt) {
        if (snbt.equals("{}")) return;
        var data = parseNbt(snbt);
        scene.world().modifyBlockEntityNBT(util.select().position(node.position()), BlockEntity.class,
                tag -> tag.merge(data.copy()), true);
    }

    public static CompoundTag parseNbt(String snbt) {
        try { return TagParser.parseTag(snbt); }
        catch (CommandSyntaxException exception) { throw new IllegalArgumentException("Invalid guide visual NBT: " + snbt, exception); }
    }

    public static BlockState state(GuideBook.Node node) {
        var id = ResourceLocation.parse(node.block());
        if (!BuiltInRegistries.BLOCK.containsKey(id)) throw new IllegalArgumentException("Unknown guide block: " + id);
        var state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
        for (var entry : node.state().entrySet()) {
            var property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) throw new IllegalArgumentException("Unknown state property: " + node.block() + "/" + entry.getKey());
            state = setProperty(state, property, entry.getValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setProperty(BlockState state, Property<T> property, String value) {
        return state.setValue(property, property.getValue(value)
                .orElseThrow(() -> new IllegalArgumentException("Invalid block state value: " + value)));
    }
}
