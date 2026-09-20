package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class Analyzer {
    static final int DEFAULT_PATH_LIMIT = 20;

    private Analyzer() {
    }

    static Result run(Path apk) throws IOException {
        return run(apk, DEFAULT_PATH_LIMIT);
    }

    static Result run(Path apk, int pathLimit) throws IOException {
        Map<String, Cls> classes = DexReader.read(apk);
        Map<String, LinkedHashSet<Why>> why = new LinkedHashMap<>();
        Map<String, List<Rel>> idx = index(classes);
        Set<String> discovered = new HashSet<>();
        Map<String, Boolean> afterActivity = new LinkedHashMap<>();

        ArrayDeque<String> queue = new ArrayDeque<>(Cfg.WEBVIEWS.stream().sorted().toList());
        Cfg.WEBVIEWS.forEach(webView -> {
            discovered.add(webView);
            afterActivity.put(webView, false);
        });
        while (!queue.isEmpty()) {
            String target = queue.removeFirst();
            boolean targetAfterActivity = afterActivity.getOrDefault(target, false);
            List<Rel> rels = idx.getOrDefault(target, List.of()).stream()
                    .sorted(Comparator.comparing(Rel::from).thenComparing(Rel::kind))
                    .toList();
            for (Rel rel : rels) {
                if (targetAfterActivity && rel.kind() != Kind.INHERITS) {
                    continue;
                }
                Why reason = new Why(rel.kind(), rel.to());
                why.computeIfAbsent(rel.from(), ignored -> new LinkedHashSet<>()).add(reason);
                if (discovered.add(rel.from())) {
                    afterActivity.put(rel.from(), targetAfterActivity || activityBase(rel.from(), classes).isPresent());
                    queue.addLast(rel.from());
                }
            }
        }
        return new Result(why, paths(why, classes, pathLimit));
    }

    private static Map<String, List<Rel>> index(Map<String, Cls> classes) {
        Map<String, List<Rel>> idx = new LinkedHashMap<>();
        classes.values().stream()
                .sorted(Comparator.comparing(Cls::name))
                .forEach(cls -> {
                    if (Cfg.isSystemLibrary(cls.name())) {
                        return;
                    }
                    add(idx, cls.name(), cls.parent(), Kind.INHERITS);
                    outer(cls.name(), classes).ifPresent(owner -> add(idx, owner, cls.name(), Kind.HOLDS));
                    for (String type : cls.fields()) {
                        if (!outerBackRef(cls.name(), type)) {
                            add(idx, cls.name(), type, Kind.HOLDS);
                        }
                    }
                    for (String type : cls.locals()) {
                        add(idx, cls.name(), type, Kind.DECLARES);
                    }
                });
        return idx;
    }

    private static void add(Map<String, List<Rel>> idx, String from, String to, Kind kind) {
        if (to != null) {
            String fromUnit = Util.classUnit(from);
            String toUnit = Util.classUnit(to);
            if (!fromUnit.equals(toUnit)) {
                idx.computeIfAbsent(toUnit, ignored -> new ArrayList<>()).add(new Rel(fromUnit, toUnit, kind));
            }
        }
    }

    private static List<Hit> paths(Map<String, LinkedHashSet<Why>> why, Map<String, Cls> classes, int pathLimit) {
        List<Hit> hits = new ArrayList<>();
        why.keySet().stream()
                .sorted(Comparator
                        .comparingInt((String name) -> activityBase(name, classes).isPresent() ? 0 : 1)
                        .thenComparing(name -> name))
                .filter(root -> activityBase(root, classes).isPresent())
                .forEach(root -> hits.addAll(shortestHits(root, why, classes, pathLimit)));
        return hits;
    }

    private static Hit hit(String root, List<Edge> path, Map<String, Cls> classes) {
        Optional<String> base = activityBase(root, classes);
        if (base.isPresent()) {
            return new Hit(List.copyOf(path), base.get(), childActivities(path, classes));
        }
        return new Hit(List.copyOf(path), Cfg.CLASS_TAG, List.of());
    }

    private static List<Hit> shortestHits(String root, Map<String, LinkedHashSet<Why>> why, Map<String, Cls> classes,
            int pathLimit) {
        List<Hit> hits = new ArrayList<>();
        ArrayDeque<PathState> queue = new ArrayDeque<>();
        queue.add(new PathState(root, List.of(), Set.of(root)));
        while (!queue.isEmpty() && hits.size() < pathLimit) {
            PathState state = queue.removeFirst();
            for (Edge edge : nextClassEdges(state.cur(), why)) {
                if (state.seen().contains(edge.to())) {
                    continue;
                }
                List<Edge> path = append(state.path(), edge);
                if (Cfg.WEBVIEWS.contains(edge.to())) {
                    hits.add(hit(root, path, classes));
                    if (hits.size() >= pathLimit) {
                        break;
                    }
                    continue;
                }
                LinkedHashSet<String> seen = new LinkedHashSet<>(state.seen());
                seen.add(edge.to());
                queue.addLast(new PathState(edge.to(), path, Set.copyOf(seen)));
            }
        }
        return hits;
    }

    private static List<Edge> append(List<Edge> path, Edge edge) {
        List<Edge> next = new ArrayList<>(path.size() + 1);
        next.addAll(path);
        next.add(edge);
        return List.copyOf(next);
    }

    private record PathState(String cur, List<Edge> path, Set<String> seen) {
    }

    private static List<Edge> nextClassEdges(String from, Map<String, LinkedHashSet<Why>> why) {
        LinkedHashMap<String, Kind> byClass = new LinkedHashMap<>();
        why.getOrDefault(from, new LinkedHashSet<>()).stream()
                .sorted(Comparator.comparing(Why::to).thenComparing(Why::kind))
                .forEach(item -> byClass.putIfAbsent(item.to(), item.kind()));
        return byClass.entrySet().stream()
                .map(entry -> new Edge(from, entry.getKey(), entry.getValue()))
                .toList();
    }

    private static List<Child> childActivities(List<Edge> path, Map<String, Cls> classes) {
        List<Child> children = new ArrayList<>();
        for (int i = 1; i < path.size(); i++) {
            Edge edge = path.get(i);
            activityBase(edge.from(), classes).ifPresent(base -> children.add(new Child(edge.from(), base)));
        }
        return children;
    }

    private static Optional<String> activityBase(String cls, Map<String, Cls> classes) {
        Set<String> seen = new HashSet<>();
        String cur = cls;
        String last = cls;
        while (cur != null && seen.add(cur)) {
            Cls info = classes.get(cur);
            if (info == null) {
                return Cfg.ACTIVITIES.contains(cur) ? Optional.of(cur) : Optional.empty();
            }
            last = cur;
            cur = info.parent();
        }
        return Cfg.ACTIVITIES.contains(last) ? Optional.of(last) : Optional.empty();
    }

    private static Optional<String> outer(String cls, Map<String, Cls> classes) {
        int idx = cls.lastIndexOf('$');
        if (idx <= 0) {
            return Optional.empty();
        }
        String owner = cls.substring(0, idx);
        return classes.containsKey(owner) ? Optional.of(owner) : Optional.empty();
    }

    private static boolean outerBackRef(String cls, String type) {
        String cur = cls;
        while (true) {
            int idx = cur.lastIndexOf('$');
            if (idx <= 0) {
                return false;
            }
            cur = cur.substring(0, idx);
            if (cur.equals(type)) {
                return true;
            }
        }
    }
}
