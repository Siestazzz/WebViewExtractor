# Sina callback explicit-super review

Conclusion: restore all four ancestor callback facts.

`com.sina.weibo.sdk.web.a.a.onPageFinished` calls `super.onPageFinished` at source line 52 and `a.onPageStarted` calls `super.onPageStarted` at line 88. `com.sina.weibo.sdk.web.a.d` makes the corresponding calls at lines 40 and 45. Their immediate superclass is `com.sina.weibo.sdk.web.a.b`; its concrete implementations are at `b.java` lines 75 and 80 and themselves continue to the platform superclass.

The independent DEX symbol table contains the exact owners and descriptors:

- `Lcom/sina/weibo/sdk/web/a/b;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V`
- `Lcom/sina/weibo/sdk/web/a/b;->onPageStarted(Landroid/webkit/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V`

Therefore runtime execution includes the subclass override and the explicit ancestor implementation. The four restored rows carry `executed_via_explicit_super=true` and source evidence for both the call and target implementation. This review does not use extractor output as evidence.
