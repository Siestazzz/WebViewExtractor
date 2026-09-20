package org.example;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

record Cls(String name, String parent, Set<String> fields, Set<String> locals) {
}

record Why(Kind kind, String to) {
}

record Rel(String from, String to, Kind kind) {
}

record Edge(String from, String to, Kind kind) {
    Edge {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(kind);
    }
}

record Hit(List<Edge> path, String base, List<Child> children) {
    String key() {
        return path.get(0).from() + "\t" + base;
    }
}

record Child(String name, String base) {
}

record Group(String outer, String base, List<Hit> paths) {
}

record GroupBuilder(String outer, String base, List<Hit> paths) {
    GroupBuilder(String outer, String base) {
        this(outer, base, new ArrayList<>());
    }
}

record Result(Map<String, LinkedHashSet<Why>> why, List<Hit> paths) {
}

record ApiHit(String name, int weight) {
}

record ClassRisk(String name, int score, List<ApiHit> apis) {
}

record ActivityRisk(String activity, int score, int pathCount, int classCount, List<ClassRisk> classes) {
}

enum Kind {
    INHERITS("inherits"),
    HOLDS("holds"),
    DECLARES("declares");

    private final String label;

    Kind(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
