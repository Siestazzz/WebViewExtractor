package org.example;
import java.util.Set;
/** Public framework transaction contracts; receiver identity is handled by the engine. */
final class FragmentTransactions {
 static final Set<String> PREFIXES=Set.of("android.app","androidx.fragment.app","android.support.v4.app");
 static boolean manager(String signature){
  for(String p:PREFIXES)if(signature.endsWith("->getFragmentManager()L"+p.replace('.','/')+"/FragmentManager;")||signature.endsWith("->getSupportFragmentManager()L"+p.replace('.','/')+"/FragmentManager;")||signature.endsWith("->getChildFragmentManager()L"+p.replace('.','/')+"/FragmentManager;"))return true;return false;
 }
 static boolean begin(String signature){for(String p:PREFIXES){String d="L"+p.replace('.','/')+"/";if(signature.equals(d+"FragmentManager;->beginTransaction()"+d+"FragmentTransaction;"))return true;}return false;}
 static boolean commit(String signature){for(String p:PREFIXES){String d="L"+p.replace('.','/')+"/FragmentTransaction;->";if(Set.of(d+"commit()I",d+"commitAllowingStateLoss()I",d+"commitNow()V",d+"commitNowAllowingStateLoss()V").contains(signature))return true;}return false;}
 static boolean remove(String signature){for(String p:PREFIXES){String d="L"+p.replace('.','/')+"/";if(signature.equals(d+"FragmentTransaction;->remove("+d+"Fragment;)"+d+"FragmentTransaction;"))return true;}return false;}
 static boolean operation(String signature){if(commit(signature)||remove(signature))return true;for(String p:PREFIXES){String d="L"+p.replace('.','/')+"/",t=d+"FragmentTransaction;";for(String name:Set.of("add","replace"))for(String shape:Set.of("(I"+d+"Fragment;)"+t,"(I"+d+"Fragment;Ljava/lang/String;)"+t,"("+d+"Fragment;Ljava/lang/String;)"+t))if(signature.equals(t+"->"+name+shape))return true;}return false;}
}
