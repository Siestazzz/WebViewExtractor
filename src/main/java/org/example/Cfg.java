package org.example;

import java.util.Set;

final class Cfg {
    static final String CLASS_TAG = "class";

    static final Set<String> WEBVIEWS = Set.of(
            "android.webkit.WebView",
            "com.tencent.smtt.sdk.WebView",
            "com.uc.webview.export.WebView",
            "com.miui.webkit.WebView",
            "com.huawei.webview.chromium.WebView"
    );

    static final Set<String> ACTIVITIES = Set.of(
            "android.app.Activity",
            "android.app.ListActivity",
            "android.app.TabActivity",
            "androidx.activity.ComponentActivity",
            "androidx.fragment.app.FragmentActivity",
            "android.support.v4.app.FragmentActivity",
            "androidx.appcompat.app.AppCompatActivity",
            "android.support.v7.app.AppCompatActivity",
            "androidx.appcompat.app.ActionBarActivity",
            "android.support.v7.app.ActionBarActivity"
    );

    static boolean isSystemLibrary(String cls) {
        return cls.startsWith("android.")
                || cls.startsWith("androidx.")
                || cls.startsWith("java.")
                || cls.startsWith("javax.")
                || cls.startsWith("kotlin.")
                || cls.startsWith("kotlinx.");
    }

    private Cfg() {
    }
}
