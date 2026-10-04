package org.example;

import java.util.*;
import org.jf.dexlib2.iface.Method;
import static org.example.DexFlow.*;

/** Structural candidates only. Installation and same-object dispatch remain runtime obligations. */
final class TransportProtocols {
    private TransportProtocols() {}

    record Registration(String method, int offset, String field, V owner, V name, V handler) {}

    /** Preserve expressions rather than assuming (String, Handler) argument positions. */
    static List<Registration> registrations(CapabilityIndex index, Method method,
                                             Summary summary, Set<String> transportFields) {
        List<Registration> result = new ArrayList<>();
        for (Call call : summary.calls()) {
            if (call.isStatic() || !call.method().endsWith("->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") ||
                    !index.map(CapabilityEngine.owner(call.method())) || call.args().size() != 3) continue;
            for (V receiver : alternatives(call.args().get(0))) {
                receiver = unwrap(receiver);
                if (!receiver.kind().equals("field") || receiver.args().size() != 1 ||
                        !transportFields.contains(receiver.id())) continue;
                // Registration names may be dynamic or computed; unknown never means absent.
                result.add(new Registration(CapabilityIndex.key(method), call.offset(), receiver.id(),
                        receiver.args().get(0), call.args().get(1), call.args().get(2)));
            }
        }
        return List.copyOf(result);
    }

    /** Substitute a concrete call's arguments without reading fields or inventing allocations. */
    static V bind(V expression, List<V> arguments) {
        if (expression.kind().equals("param")) {
            try {
                int index = Integer.parseInt(expression.id());
                return index >= 0 && index < arguments.size() ? arguments.get(index) : UNKNOWN;
            } catch (NumberFormatException ex) { return UNKNOWN; }
        }
        if (expression.args().isEmpty()) return expression;
        List<V> bound = expression.args().stream().map(v -> bind(v, arguments)).toList();
        if (expression.kind().equals("union")) {
            V result = null;
            for (V item : bound) result = union(result, item);
            return result == null ? UNKNOWN : result;
        }
        return new V(expression.kind(), expression.type(), expression.id(), expression.literal(), bound);
    }

    static V unwrap(V value) {
        while (value.kind().equals("cast") && value.args().size() == 1) value = value.args().get(0);
        return value;
    }

    /** Exact SDK contracts, including console callbacks that carry no WebView parameter. */
    static boolean clientEntry(CapabilityIndex index, Method method) {
        if ((method.getAccessFlags() & 1) == 0 || (method.getAccessFlags() & 8) != 0) return false;
        String owner = CapabilityIndex.cls(method.getDefiningClass()), shape = CapabilityIndex.shape(method);
        for (String family : List.of("android.webkit.", "com.tencent.smtt.sdk.")) {
            String descriptor = "L" + family.replace('.', '/'), view = descriptor + "WebView;";
            if (index.subtype(owner, family + "WebChromeClient") && Set.of(
                    "onConsoleMessage(" + descriptor + "ConsoleMessage;)Z",
                    "onConsoleMessage(Ljava/lang/String;ILjava/lang/String;)V",
                    "onJsPrompt(" + view + "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;" + descriptor + "JsPromptResult;)Z"
            ).contains(shape)) return true;
            if (index.subtype(owner, family + "WebViewClient") && Set.of(
                    "shouldOverrideUrlLoading(" + view + "Ljava/lang/String;)Z",
                    "shouldOverrideUrlLoading(" + view + descriptor + "WebResourceRequest;)Z",
                    "onPageFinished(" + view + "Ljava/lang/String;)V"
            ).contains(shape)) return true;
        }
        return false;
    }
}
