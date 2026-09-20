# Imgo handler status transition

The 56 registration facts across seven Imgo hosts remain present with their original registration names, expressions, and source evidence. Their status changes from `registered-target-unknown` to `registered-no-compatible-endpoint` after the fixed-class, declared-only `(String)` reflection audit in `imgo-unknown-handler-audit.md`.

| status | before | after |
|---|---:|---:|
| `registered-target-unknown` | 56 | 0 for this Imgo set |
| `registered-no-compatible-endpoint` | 0 | 56 |
| registration facts retained | 56 | 56 |

Each row now carries `audit_reference=docs/validation/mango/imgo-unknown-handler-audit.md`. Endpoint signature and implementation remain blank because no compatible callable endpoint exists. This is a classification correction, not deletion of a failed fact.
