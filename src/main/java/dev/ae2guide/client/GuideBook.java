package dev.ae2guide.client;

import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable teaching data, shared by the catalogue, translations and storyboards. */
public final class GuideBook {
    private GuideBook() {}

    private static final class Holder {
        private static final List<Lesson> LESSONS = load();
    }

    public static List<Lesson> lessons() { return Holder.LESSONS; }

    private static List<Lesson> load() {
        var stream = Objects.requireNonNull(GuideBook.class.getResourceAsStream(
                "/assets/ae2ponder/guide/lessons.json"), "Missing AE2 lesson catalogue");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var lessons = List.copyOf(Arrays.asList(new Gson().fromJson(reader, Lesson[].class)));
            if (lessons.isEmpty()) throw new IllegalStateException("AE2 lesson catalogue is empty");
            if (lessons.stream().map(Lesson::id).distinct().count() != lessons.size())
                throw new IllegalStateException("Duplicate AE2 lesson ID");
            return lessons;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load AE2 Ponder Guide lessons", exception);
        }
    }

    public record Localized(String ja, String en) {
        public Localized {
            if (ja == null || ja.isBlank() || en == null || en.isBlank())
                throw new IllegalArgumentException("Both Japanese and English text are required");
        }
    }

    public record Node(String id, String block, List<Integer> pos, Map<String, String> state, String nbt) {
        public Node {
            Objects.requireNonNull(id);
            ResourceLocation.parse(block);
            pos = List.copyOf(pos);
            if (pos.size() != 3 || pos.get(0) < 0 || pos.get(0) > 8 || pos.get(1) < 1
                    || pos.get(1) > 4 || pos.get(2) < 0 || pos.get(2) > 8)
                throw new IllegalArgumentException("Node outside the 9 x 5 x 9 stage: " + id);
            state = state == null ? Map.of() : Map.copyOf(state);
            nbt = nbt == null ? "{}" : nbt;
        }
        public BlockPos position() { return new BlockPos(pos.get(0), pos.get(1), pos.get(2)); }
    }

    public record Flow(String from, String to, String item) {}

    public record Step(Localized text, List<String> show, List<String> focus, List<String> hide,
                       Flow flow, boolean warning, Map<String, String> nbt) {
        public Step {
            Objects.requireNonNull(text);
            show = show == null ? List.of() : List.copyOf(show);
            focus = focus == null ? List.of() : List.copyOf(focus);
            hide = hide == null ? List.of() : List.copyOf(hide);
            nbt = nbt == null ? Map.of() : Map.copyOf(nbt);
        }
    }

    public record Lesson(String id, String chapter, Localized title, Localized summary, String icon,
                         List<String> components, List<Node> nodes, List<Step> steps) {
        public Lesson {
            if (id == null || !id.matches("[a-z0-9_]+")) throw new IllegalArgumentException("Invalid lesson ID");
            Objects.requireNonNull(chapter);
            Objects.requireNonNull(title);
            Objects.requireNonNull(summary);
            ResourceLocation.parse(icon);
            components = List.copyOf(components);
            nodes = List.copyOf(nodes);
            steps = List.copyOf(steps);
            if (nodes.isEmpty() || steps.isEmpty()) throw new IllegalArgumentException("Empty lesson: " + id);
            if (nodes.stream().map(Node::id).distinct().count() != nodes.size())
                throw new IllegalArgumentException("Duplicate node in " + id);
            var nodeList = nodes;
            for (var step : steps) {
                step.show().forEach(n -> requireNode(nodeList, n));
                step.focus().forEach(n -> requireNode(nodeList, n));
                step.hide().forEach(n -> requireNode(nodeList, n));
                step.nbt().keySet().forEach(n -> requireNode(nodeList, n));
                if (step.flow() != null) {
                    requireNode(nodes, step.flow().from());
                    requireNode(nodes, step.flow().to());
                }
            }
        }
        public Node node(String id) { return requireNode(nodes, id); }
        private static Node requireNode(List<Node> nodes, String id) {
            return nodes.stream().filter(n -> n.id().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown scene node: " + id));
        }
    }
}
