package org.example;

import soot.ArrayType;
import soot.Body;
import soot.G;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.options.Options;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

final class SootReader {
    private SootReader() {
    }

    static Map<String, Set<String>> locals(Path apk) throws IOException {
        configure(apk);
        Scene.v().loadNecessaryClasses();

        Map<String, Set<String>> result = new LinkedHashMap<>();
        Iterator<SootClass> classes = Scene.v().getApplicationClasses().snapshotIterator();
        while (classes.hasNext()) {
            SootClass cls = classes.next();
            Set<String> locals = new TreeSet<>();
            for (SootMethod method : cls.getMethods()) {
                addLocals(method, locals);
            }
            result.put(cls.getName(), locals);
        }
        return result;
    }

    private static void configure(Path apk) throws IOException {
        G.reset();
        Options options = Options.v();
        options.set_src_prec(Options.src_prec_apk);
        options.set_process_dir(java.util.List.of(apk.toString()));
        options.set_process_multiple_dex(true);
        options.set_allow_phantom_refs(true);
        options.set_ignore_resolving_levels(true);
        options.set_output_format(Options.output_format_none);
        options.set_whole_program(false);

        Path androidJars = androidJars();
        if (androidJars != null) {
            options.set_android_jars(androidJars.toString());
        }
    }

    private static void addLocals(SootMethod method, Set<String> locals) {
        if (!method.isConcrete()) {
            return;
        }

        Body body;
        try {
            body = method.retrieveActiveBody();
        } catch (RuntimeException ignored) {
            return;
        }

        try {
            for (soot.Unit unit : body.getUnits()) {
                if (unit instanceof IdentityStmt || !(unit instanceof DefinitionStmt def)) {
                    continue;
                }
                Value right = def.getRightOp();
                if (right instanceof CastExpr
                        || right instanceof Local
                        || right instanceof ParameterRef
                        || right instanceof ThisRef
                        || right instanceof FieldRef) {
                    continue;
                }
                Value left = def.getLeftOp();
                if (left instanceof Local local) {
                    refType(local.getType(), locals);
                }
            }
        } finally {
            method.releaseActiveBody();
        }
    }

    private static void refType(Type type, Set<String> out) {
        while (type instanceof ArrayType array) {
            type = array.baseType;
        }
        if (type instanceof RefType ref) {
            out.add(ref.getClassName());
        }
    }

    private static Path androidJars() throws IOException {
        String sdk = firstNonBlank(System.getenv("ANDROID_HOME"), System.getenv("ANDROID_SDK_ROOT"));
        if (sdk == null) {
            Path propsFile = Path.of("local.properties").toAbsolutePath().normalize();
            if (Files.isRegularFile(propsFile)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(propsFile)) {
                    props.load(in);
                }
                sdk = props.getProperty("sdk.dir");
            }
        }
        if (sdk == null) {
            return null;
        }

        Path platforms = Path.of(sdk).resolve("platforms").normalize();
        if (!Files.isDirectory(platforms)) {
            throw new IOException("Android SDK platforms not found: " + platforms);
        }
        return platforms;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null && !second.isBlank() ? second : null;
    }
}
