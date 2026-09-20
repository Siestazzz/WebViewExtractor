package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class JsonSink {
    private JsonSink() {
    }

    static void containers(Path file, Map<String, LinkedHashSet<Why>> reasons) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("[");
            w.newLine();
            List<Map.Entry<String, LinkedHashSet<Why>>> entries = reasons.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
            for (int i = 0; i < entries.size(); i++) {
                writeContainer(w, entries.get(i));
                commaLine(w, i, entries.size());
            }
            w.write("]");
            w.newLine();
        }
    }

    static void names(Path file, List<Hit> hits, List<ActivityRisk> risks) throws IOException {
        Map<String, Integer> scores = risks.stream()
                .collect(LinkedHashMap::new, (map, risk) -> map.put(risk.activity(), risk.score()), Map::putAll);
        List<Group> groups = group(hits).stream()
                .sorted(Comparator
                        .comparingInt(JsonSink::minPathLength)
                        .thenComparing(Group::outer)
                        .thenComparing(Group::base))
                .toList();
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("{");
            w.newLine();
            w.write("  \"activity\": ");
            summaries(w, groups, scores, "  ");
            w.newLine();
            w.write("}");
            w.newLine();
        }
    }

    static void paths(Path file, List<Hit> hits) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("[");
            w.newLine();
            List<Group> groups = group(hits);
            for (int i = 0; i < groups.size(); i++) {
                writeGroup(w, groups.get(i));
                commaLine(w, i, groups.size());
            }
            w.write("]");
            w.newLine();
        }
    }

    static void riskScores(Path file, List<ActivityRisk> risks) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("[");
            w.newLine();
            for (int i = 0; i < risks.size(); i++) {
                writeActivityRisk(w, risks.get(i));
                commaLine(w, i, risks.size());
            }
            w.write("]");
            w.newLine();
        }
    }

    private static void writeContainer(BufferedWriter w, Map.Entry<String, LinkedHashSet<Why>> entry) throws IOException {
        w.write("  {");
        w.newLine();
        w.write("    \"className\": " + Util.js(entry.getKey()) + ",");
        w.newLine();
        w.write("    \"reasons\": [");
        w.newLine();
        List<Why> why = entry.getValue().stream()
                .sorted(Comparator.comparing(Why::to).thenComparing(Why::kind))
                .toList();
        for (int i = 0; i < why.size(); i++) {
            Why item = why.get(i);
            w.write("      {\"kind\": " + Util.js(item.kind().label()) + ", \"target\": " + Util.js(item.to()) + "}");
            if (i < why.size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write("    ]");
        w.newLine();
        w.write("  }");
    }

    private static void writeActivityRisk(BufferedWriter w, ActivityRisk risk) throws IOException {
        w.write("  {");
        w.newLine();
        w.write("    \"activityName\": " + Util.js(risk.activity()) + ",");
        w.newLine();
        w.write("    \"score\": " + risk.score() + ",");
        w.newLine();
        w.write("    \"pathCount\": " + risk.pathCount() + ",");
        w.newLine();
        w.write("    \"classCount\": " + risk.classCount() + ",");
        w.newLine();
        w.write("    \"matchedClasses\": [");
        w.newLine();
        for (int i = 0; i < risk.classes().size(); i++) {
            writeClassRisk(w, risk.classes().get(i), "      ");
            if (i < risk.classes().size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write("    ]");
        w.newLine();
        w.write("  }");
    }

    private static void writeClassRisk(BufferedWriter w, ClassRisk risk, String indent) throws IOException {
        w.write(indent + "{");
        w.newLine();
        w.write(indent + "  \"className\": " + Util.js(risk.name()) + ",");
        w.newLine();
        w.write(indent + "  \"score\": " + risk.score() + ",");
        w.newLine();
        w.write(indent + "  \"matchedApis\": [");
        for (int i = 0; i < risk.apis().size(); i++) {
            ApiHit api = risk.apis().get(i);
            if (i == 0) {
                w.newLine();
            }
            w.write(indent + "    {\"name\": " + Util.js(api.name()) + ", \"weight\": " + api.weight() + "}");
            if (i < risk.apis().size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        if (risk.apis().isEmpty()) {
            w.write("]");
            w.newLine();
        } else {
            w.write(indent + "  ]");
            w.newLine();
        }
        w.write(indent + "}");
    }

    private static void writeGroup(BufferedWriter w, Group group) throws IOException {
        Hit first = group.paths().get(0);
        w.write("  {");
        w.newLine();
        w.write("    \"outermost\": " + Util.js(group.outer()) + ",");
        w.newLine();
        w.write("    \"outermostBase\": " + Util.js(group.base()) + ",");
        w.newLine();
        w.write("    \"finalParentClass\": " + Util.js(group.base()) + ",");
        w.newLine();
        w.write("    \"childActivities\": ");
        children(w, first.children(), "    ");
        w.write(",");
        w.newLine();
        w.write("    \"paths\": [");
        w.newLine();
        for (int i = 0; i < group.paths().size(); i++) {
            hit(w, group.paths().get(i), "      ");
            if (i < group.paths().size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write("    ]");
        w.newLine();
        w.write("  }");
    }

    private static List<Group> group(List<Hit> hits) {
        LinkedHashMap<String, GroupBuilder> groups = new LinkedHashMap<>();
        hits.forEach(hit -> {
            String outer = hit.path().get(0).from();
            String key = outer + "\t" + hit.base();
            GroupBuilder group = groups.computeIfAbsent(key, ignored -> new GroupBuilder(outer, hit.base()));
            group.paths().add(hit);
        });
        return groups.values().stream()
                .map(g -> new Group(g.outer(), g.base(), List.copyOf(g.paths())))
                .toList();
    }

    private static void hit(BufferedWriter w, Hit hit, String indent) throws IOException {
        List<Edge> path = hit.path();
        w.write(indent + "{");
        w.newLine();
        w.write(indent + "  \"webviewBase\": " + Util.js(path.get(path.size() - 1).to()) + ",");
        w.newLine();
        w.write(indent + "  \"edges\": [");
        w.newLine();
        for (int i = 0; i < path.size(); i++) {
            Edge edge = path.get(i);
            w.write(indent + "    {\"from\": " + Util.js(edge.from())
                    + ", \"kind\": " + Util.js(edge.kind().label())
                    + ", \"to\": " + Util.js(edge.to()) + "}");
            if (i < path.size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write(indent + "  ]");
        w.newLine();
        w.write(indent + "}");
    }

    private static void strings(BufferedWriter w, List<String> values, String indent) throws IOException {
        w.write("[");
        if (values.isEmpty()) {
            w.write("]");
            return;
        }
        w.newLine();
        for (int i = 0; i < values.size(); i++) {
            w.write(indent + "  " + Util.js(values.get(i)));
            if (i < values.size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write(indent + "]");
    }

    private static void summaries(BufferedWriter w, List<Group> groups, Map<String, Integer> scores, String indent) throws IOException {
        w.write("[");
        if (groups.isEmpty()) {
            w.write("]");
            return;
        }
        w.newLine();
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            w.write(indent + "  {\"activityName\":" + Util.js(group.outer())
                    + ",\"activityBase\":" + Util.js(group.base())
                    + ",\"score\":" + scores.getOrDefault(group.outer(), 0)
                    + ",\"pathCount\":" + group.paths().size()
                    + ",\"pathLengths\":");
            ints(w, group.paths().stream().map(hit -> hit.path().size()).sorted().toList());
            w.write("}");
            if (i < groups.size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write(indent + "]");
    }

    private static int minPathLength(Group group) {
        return group.paths().stream()
                .mapToInt(hit -> hit.path().size())
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    private static void ints(BufferedWriter w, List<Integer> values) throws IOException {
        w.write("[");
        for (int i = 0; i < values.size(); i++) {
            w.write(String.valueOf(values.get(i)));
            if (i < values.size() - 1) {
                w.write(",");
            }
        }
        w.write("]");
    }

    private static void children(BufferedWriter w, List<Child> children, String indent) throws IOException {
        w.write("[");
        if (children.isEmpty()) {
            w.write("]");
            return;
        }
        w.newLine();
        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i);
            w.write(indent + "  {\"className\": " + Util.js(child.name())
                    + ", \"finalParentClass\": " + Util.js(child.base()) + "}");
            if (i < children.size() - 1) {
                w.write(",");
            }
            w.newLine();
        }
        w.write(indent + "]");
    }

    private static void commaLine(BufferedWriter w, int index, int size) throws IOException {
        if (index < size - 1) {
            w.write(",");
        }
        w.newLine();
    }
}
