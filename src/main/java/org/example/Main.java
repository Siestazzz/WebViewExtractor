package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        Path apk = apkPath(args);
        Path outDir = cwd.resolve("output");
        int pathLimit = pathCount(args);

        if (!Files.isRegularFile(apk)) {
            throw new IllegalArgumentException("APK not found: " + apk);
        }

        if (hasArg(args, "--read-apk-only")) {
            long start = System.nanoTime();
            int classCount = DexReader.read(apk).size();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("APK: " + apk);
            System.out.println("Classes read: " + classCount);
            System.out.println("Read APK only elapsed ms: " + elapsedMs);
            return;
        }

        long start = System.nanoTime();
        Result res = Analyzer.run(apk, pathLimit);
        long analyzeElapsedMs = (System.nanoTime() - start) / 1_000_000;
        Files.createDirectories(outDir);

        String tag = Util.apkTag(apk);
        Path containers = outDir.resolve(tag + "_webview_container_classes.json");
        Path paths = outDir.resolve(tag + "_webview_outer_paths.json");
        Path names = outDir.resolve(tag + "_webview_outermost_classes.json");
        Path risks = outDir.resolve(tag + "_webview_activity_risk_scores.json");
        java.util.List<ActivityRisk> riskScores = RiskScorer.score(apk, res.paths());

        JsonSink.containers(containers, res.why());
        JsonSink.paths(paths, res.paths());
        JsonSink.names(names, res.paths(), riskScores);
        JsonSink.riskScores(risks, riskScores);

        System.out.println("APK: " + apk);
        System.out.println("Container classes: " + res.why().size());
        System.out.println("Outer paths: " + res.paths().size());
        System.out.println("Path count per activity: " + pathLimit);
        System.out.println("Analyze elapsed ms: " + analyzeElapsedMs);
        System.out.println("Wrote: " + containers);
        System.out.println("Wrote: " + paths);
        System.out.println("Wrote: " + names);
        System.out.println("Wrote: " + risks);
    }

    private static Path apkPath(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--apkpath".equals(args[i])) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--apkpath requires an APK file path");
                }
                return Path.of(args[i + 1]).toAbsolutePath().normalize();
            }
        }
        throw new IllegalArgumentException("Missing required argument: --apkpath <apk-file>");
    }

    private static int pathCount(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--pathcount".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException(arg + " requires a positive integer value");
                }
                return positiveInt(args[++i], arg);
            }
        }
        return Analyzer.DEFAULT_PATH_LIMIT;
    }

    private static boolean hasArg(String[] args, String expected) {
        for (String arg : args) {
            if (expected.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static int positiveInt(String value, String option) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        throw new IllegalArgumentException(option + " must be a positive integer, got: " + value);
    }
}
