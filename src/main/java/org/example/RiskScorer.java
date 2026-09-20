package org.example;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RiskScorer {
    private RiskScorer() {
    }

    static List<ActivityRisk> score(Path apk, List<Hit> hits) throws IOException {
        Map<String, ClassRisk> classRisks = classRisks(apk);
        Map<String, GroupBuilder> groups = groups(hits);
        List<ActivityRisk> risks = new ArrayList<>();
        for (GroupBuilder group : groups.values()) {
            LinkedHashSet<String> classes = new LinkedHashSet<>();
            for (Hit hit : group.paths()) {
                for (Edge edge : hit.path()) {
                    classes.add(Util.classUnit(edge.from()));
                    classes.add(Util.classUnit(edge.to()));
                }
            }
            List<ClassRisk> matched = classes.stream()
                    .map(classRisks::get)
                    .filter(risk -> risk != null && risk.score() > 0)
                    .sorted(Comparator.comparingInt(ClassRisk::score).reversed().thenComparing(ClassRisk::name))
                    .toList();
            int score = matched.stream().mapToInt(ClassRisk::score).sum();
            risks.add(new ActivityRisk(group.outer(), score, group.paths().size(), classes.size(), matched));
        }
        return risks.stream()
                .sorted(Comparator.comparingInt(ActivityRisk::score).reversed()
                        .thenComparing(ActivityRisk::activity))
                .toList();
    }

    private static Map<String, GroupBuilder> groups(List<Hit> hits) {
        LinkedHashMap<String, GroupBuilder> groups = new LinkedHashMap<>();
        hits.forEach(hit -> {
            String outer = hit.path().get(0).from();
            String key = outer + "\t" + hit.base();
            groups.computeIfAbsent(key, ignored -> new GroupBuilder(outer, hit.base())).paths().add(hit);
        });
        return groups;
    }

    private static Map<String, ClassRisk> classRisks(Path apk) throws IOException {
        MultiDexContainer<? extends DexBackedDexFile> dexes =
                DexFileFactory.loadDexContainer(apk.toFile(), Opcodes.getDefault());
        Map<String, LinkedHashMap<String, ApiHit>> hits = new LinkedHashMap<>();
        for (String dexName : dexes.getDexEntryNames()) {
            MultiDexContainer.DexEntry<? extends DexBackedDexFile> dex = dexes.getEntry(dexName);
            for (ClassDef def : dex.getDexFile().getClasses()) {
                String className = Util.classUnit(Util.className(def.getType()));
                LinkedHashMap<String, ApiHit> apis = hits.computeIfAbsent(className, ignored -> new LinkedHashMap<>());
                for (Method method : def.getMethods()) {
                    scan(method, apis);
                }
            }
        }
        Map<String, ClassRisk> risks = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, ApiHit>> entry : hits.entrySet()) {
            List<ApiHit> apis = entry.getValue().values().stream()
                    .sorted(Comparator.comparingInt(ApiHit::weight).reversed().thenComparing(ApiHit::name))
                    .toList();
            int score = apis.stream().mapToInt(ApiHit::weight).sum();
            risks.put(entry.getKey(), new ClassRisk(entry.getKey(), score, apis));
        }
        return risks;
    }

    private static void scan(Method method, Map<String, ApiHit> apis) {
        MethodImplementation impl = method.getImplementation();
        if (impl == null) {
            return;
        }
        List<Instruction> instructions = new ArrayList<>();
        impl.getInstructions().forEach(instructions::add);
        Map<Integer, Integer> constants = new HashMap<>();
        for (Instruction instruction : instructions) {
            if (!(instruction instanceof ReferenceInstruction ref) || !(ref.getReference() instanceof MethodReference methodRef)) {
                trackConstant(instruction, constants);
                continue;
            }
            match(methodRef, instruction, constants).forEach(api -> apis.putIfAbsent(api.name(), api));
            trackConstant(instruction, constants);
        }
    }

    private static List<ApiHit> match(MethodReference method, Instruction instruction, Map<Integer, Integer> constants) {
        String name = method.getName();
        String owner = Util.className(method.getDefiningClass());
        return switch (name) {
            case "addJavascriptInterface" -> List.of(new ApiHit("addJavascriptInterface", 10));
            case "setAllowUniversalAccessFromFileURLs" -> trueArg(instruction, constants)
                    ? List.of(new ApiHit("setAllowUniversalAccessFromFileURLs(true)", 9)) : List.of();
            case "setAllowFileAccessFromFileURLs" -> trueArg(instruction, constants)
                    ? List.of(new ApiHit("setAllowFileAccessFromFileURLs(true)", 8)) : List.of();
            case "setWebContentsDebuggingEnabled" -> trueArg(instruction, constants)
                    ? List.of(new ApiHit("setWebContentsDebuggingEnabled(true)", 7)) : List.of();
            case "setAllowFileAccess" -> trueArg(instruction, constants)
                    ? List.of(new ApiHit("setAllowFileAccess(true)", 5)) : List.of();
            case "setAllowContentAccess" -> trueArg(instruction, constants)
                    ? List.of(new ApiHit("setAllowContentAccess(true)", 4)) : List.of();
            case "setJavaScriptEnabled" -> trueArg(instruction, constants)
                    ? List.of(new ApiHit("setJavaScriptEnabled(true)", 4)) : List.of();
            case "loadUrl" -> List.of(new ApiHit("loadUrl(variable/untrusted-looking)", 3));
            case "loadDataWithBaseURL" -> List.of(new ApiHit("loadDataWithBaseURL", 3));
            case "setCookie", "getCookie" -> isCookieManager(owner)
                    ? List.of(new ApiHit("CookieManager." + name, 2)) : List.of();
            default -> List.of();
        };
    }

    private static boolean isCookieManager(String owner) {
        return "android.webkit.CookieManager".equals(owner)
                || "com.tencent.smtt.sdk.CookieManager".equals(owner)
                || (owner != null && owner.endsWith(".CookieManager"));
    }

    private static boolean trueArg(Instruction instruction, Map<Integer, Integer> constants) {
        int register = lastArgumentRegister(instruction);
        return constants.getOrDefault(register, 0) == 1;
    }

    private static void trackConstant(Instruction instruction, Map<Integer, Integer> constants) {
        if (instruction instanceof OneRegisterInstruction one && instruction instanceof NarrowLiteralInstruction literal) {
            Opcode opcode = instruction.getOpcode();
            if (Set.of(Opcode.CONST_4, Opcode.CONST_16, Opcode.CONST, Opcode.CONST_HIGH16).contains(opcode)) {
                constants.put(one.getRegisterA(), literal.getNarrowLiteral());
            }
        }
    }

    private static int lastArgumentRegister(Instruction instruction) {
        if (instruction instanceof Instruction35c invoke) {
            int count = invoke.getRegisterCount();
            return switch (count) {
                case 1 -> invoke.getRegisterC();
                case 2 -> invoke.getRegisterD();
                case 3 -> invoke.getRegisterE();
                case 4 -> invoke.getRegisterF();
                case 5 -> invoke.getRegisterG();
                default -> -1;
            };
        }
        if (instruction instanceof Instruction3rc invoke) {
            int count = invoke.getRegisterCount();
            return count > 0 ? invoke.getStartRegister() + count - 1 : -1;
        }
        return -1;
    }
}
