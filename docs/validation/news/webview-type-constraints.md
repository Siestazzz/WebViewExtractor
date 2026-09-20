# Tencent News source-authored WebView constraints

`webview-type-constraints.json` maps all 14 public canonical WebView labels to concrete receiver classes using layout tags, constructor allocation sites, and field assignment/getter chains. It does not use extractor report receiver types. All 14 labels are source-confirmed; none currently remains unresolved. The mapping also contains the two labels used by the v12h non-blind development append.

The mapping names the WebView object on which the capability executes. Wrapper views are therefore excluded: `NovelWebView` and `NovelLoadingWebView` lead to their nested `AdWebView`; `AdLoadingWebView` leads to its nested `AdWebView`; `APWebView` and `APX5WebView` lead to Android and X5 WebViews. `midas_pay_web` is the only canonical label with two allowed concrete types because the Activity explicitly selects either backend. YSP uses its actual anonymous `YspMediaPlayer$attachTo$1` subclass, and mosaic uses the actual `e$a` subclass rather than their declared field supertypes.

The migration script always writes a separate JSONL file:

```bash
python3 docs/validation/news/apply_webview_constraints.py \
  docs/validation/news/canonical-facts.jsonl \
  /tmp/news-canonical-with-webview-constraints.jsonl
```

It can be replayed on `v12h-disappeared-development-facts.jsonl` in the same way. Unknown labels remain unchanged and appear in the script summary. The script does not alter canonical files and does not infer Activity ownership or subclass compatibility.
