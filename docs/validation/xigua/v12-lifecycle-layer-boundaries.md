# Frozen v12 lifecycle layer isolation

The installed Fragment controls recover bridge, settings and callback registrations from directly allocated content in both `onCreate(Bundle)` and `onActivityCreated(Bundle)`. Therefore the missing full factory chain does not establish that the platform Fragment callback itself is unsupported.

| Case | Bridge/settings/callback | Source expectation | Result |
| --- | --- | --- | --- |
| direct-navigation | 0/0/0 | positive | missing |
| fragment-create | 1/1/1 | positive | matched |
| fragment-activity-created | 1/1/1 | positive | matched |
| fragment-uninstalled | 1/1/1 | negative | wrong |

The uninstalled case has the same callback body as the installed activity-created control, but removes the actual Fragment transaction installation. It still emits three candidate facts and `xml_lookup_unresolved_candidate`. The earlier fourteen-case matrix returned zero for its uninstalled variant because it also depended on the broken captured factory chain. That zero was not evidence of sound installation gating. Preserve this independent negative when repairing factory identity.

The direct-navigation case bypasses the dispatcher while retaining the actual constructor-held factory and interface return. Frozen v12 still reports unresolved RootFactory.create and ContentContract lifecycle dispatch. This is consistent with the already isolated constructor-field identity loss; it does not justify a Scene or Navigation private rule.

Reproduce from the repository root:

```sh
bash test/run-lifecycle-layer-probe.sh test/runs/generic-v12.jar test/runs/xigua-layer-probe-v12
```

The actual source basis remains `scene-installation-source-chain.json`: actual Activity installs the lifecycle Fragment; callback/dispatcher reaches the factory-returned Scene. All artifact hashes, raw reports and per-case timings are in `v12-lifecycle-layer-boundaries.json`. This run took at most 0.292 seconds per case against a 30-second analysis budget. It neither proves actual App recovery nor three-run performance acceptance. The fourteen-case factory matrix and frozen oracle are unchanged.
