package org.example;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class Util {
    private Util() {
    }

    static Optional<String> refType(String desc) {
        if (desc == null) {
            return Optional.empty();
        }
        String type = desc;
        while (type.startsWith("[")) {
            type = type.substring(1);
        }
        if (!type.startsWith("L") || !type.endsWith(";")) {
            return Optional.empty();
        }
        return Optional.of(type.substring(1, type.length() - 1).replace('/', '.'));
    }

    static String className(String desc) {
        return refType(desc).orElse(null);
    }

    static String classUnit(String cls) {
        if (cls == null) {
            return null;
        }
        String[] parts = cls.split("\\$");
        if (parts.length == 1) {
            return cls;
        }
        StringBuilder unit = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].matches("\\d+.*")) {
                break;
            }
            unit.append('$').append(parts[i]);
        }
        return unit.toString();
    }

    static String apkTag(Path apk) {
        String file = apk.getFileName().toString();
        int dot = file.lastIndexOf('.');
        String base = dot > 0 ? file.substring(0, dot) : file;
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    static String pathText(List<Edge> path) {
        if (path.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder(path.get(0).from());
        for (Edge edge : path) {
            text.append(" --").append(edge.kind().label()).append("--> ").append(edge.to());
        }
        return text.toString();
    }

    static String js(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder s = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> s.append("\\\"");
                case '\\' -> s.append("\\\\");
                case '\b' -> s.append("\\b");
                case '\f' -> s.append("\\f");
                case '\n' -> s.append("\\n");
                case '\r' -> s.append("\\r");
                case '\t' -> s.append("\\t");
                default -> {
                    if (c < 0x20) {
                        s.append(String.format("\\u%04x", (int) c));
                    } else {
                        s.append(c);
                    }
                }
            }
        }
        return s.append("\"").toString();
    }
}
