package org.example;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class DexReader {
    private DexReader() {
    }

    static Map<String, Cls> read(Path apk) throws IOException {
        MultiDexContainer<? extends DexBackedDexFile> dexes =
                DexFileFactory.loadDexContainer(apk.toFile(), Opcodes.getDefault());
        Map<String, Set<String>> locals = SootReader.locals(apk);
        Map<String, Cls> classes = new LinkedHashMap<>();

        for (String dexName : dexes.getDexEntryNames()) {
            MultiDexContainer.DexEntry<? extends DexBackedDexFile> dex = dexes.getEntry(dexName);
            for (ClassDef def : dex.getDexFile().getClasses()) {
                String name = Util.className(def.getType());
                classes.put(name, new Cls(
                        name,
                        Util.className(def.getSuperclass()),
                        fields(def),
                        locals.getOrDefault(name, Set.of())
                ));
            }
        }
        return classes;
    }

    private static Set<String> fields(ClassDef def) {
        Set<String> fields = new TreeSet<>();
        for (Field field : def.getFields()) {
            Util.refType(field.getType()).ifPresent(fields::add);
        }
        return fields;
    }
}
