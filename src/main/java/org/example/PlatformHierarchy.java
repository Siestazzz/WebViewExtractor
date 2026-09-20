package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;

/** Read SDK class headers only; no platform method bodies or whole-program scene. */
final class PlatformHierarchy {
    static Map<String,List<String>> read(Path path,long deadline)throws IOException {
        Map<String,List<String>> result=new HashMap<>();
        try(JarFile jar=new JarFile(path.toFile())){
            var entries=jar.entries();
            while(entries.hasMoreElements()){
                if(System.nanoTime()>deadline)throw new IOException("platform_header_deadline");
                var entry=entries.nextElement();
                if(!entry.getName().startsWith("android/")||!entry.getName().endsWith(".class"))continue;
                try(var input=jar.getInputStream(entry)){
                    var reader=new ClassReader(input);List<String> parents=new ArrayList<>();
                    if(reader.getSuperName()!=null)parents.add(reader.getSuperName().replace('/','.'));
                    for(String iface:reader.getInterfaces())parents.add(iface.replace('/','.'));
                    result.put(reader.getClassName().replace('/','.'),List.copyOf(parents));
                }
            }
        }return result;
    }
}
