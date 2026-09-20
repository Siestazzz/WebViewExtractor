import java.util.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.value.*;
import com.google.gson.Gson;
/** Independent raw DEX symbol table for checking JADX names, NOT extractor predictions. */
public class DexSymbols {
 public static void main(String[] args)throws Exception {
  Gson json=new Gson();var dexes=DexFileFactory.loadDexContainer(new java.io.File(args[0]),Opcodes.getDefault());
  for(String dex:dexes.getDexEntryNames())for(ClassDef c:dexes.getEntry(dex).getDexFile().getClasses()){
   List<Object> methods=new ArrayList<>(),fields=new ArrayList<>();
   for(Method m:c.getMethods())methods.add(Map.of("name",m.getName(),"signature",m.getDefiningClass()+"->"+m.getName()+"("+String.join("",m.getParameterTypes())+")"+m.getReturnType(),"flags",m.getAccessFlags(),"annotations",m.getAnnotations().stream().map(Annotation::getType).toList()));
   for(Field f:c.getFields()){
    Map<String,Object> v=new LinkedHashMap<>();v.put("name",f.getName());v.put("type",f.getType());v.put("flags",f.getAccessFlags());
    if(f.getInitialValue() instanceof StringEncodedValue s)v.put("value",s.getValue());fields.add(v);
   }
   Map<String,Object> row=new LinkedHashMap<>();row.put("type",c.getType());row.put("parent",c.getSuperclass());row.put("interfaces",c.getInterfaces());row.put("methods",methods);row.put("fields",fields);System.out.println(json.toJson(row));
  }
 }
}
