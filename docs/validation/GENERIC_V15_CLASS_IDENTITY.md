# Generic v15: known Class identity factory selection

This change addresses a generic failure reproduced by ClassKeyFactoryProbe against frozen v13: an actual Product0 Class passed to a two-branch selector retained Product0, Product1 and null, while an 80-branch selector collapsed its returned union to unknown. This document does not claim that any application factory or callback surface is recovered.

DexFlow equality and inequality now compare two known Class values by their resolved type. Unknown values and Class unions retain both branches. Numeric guards retain their previous behavior. Contextual eligibility recognizes a previously unknown Class operand becoming a known Class, so it no longer skips the refinement as a non-numeric guard.

Ordinary methods retain the 500-instruction refinement boundary. A separate bounded eligibility applies only to a forward selector with a Class parameter, const-class instructions, and binary forward if-eq/if-ne branches. Goto and switch control flow is excluded. This family may contain at most 4,096 instructions; exceeding it reports summary_class_factory_budget and retains the conservative base summary. Refinement still uses the same per-method shared 16-variant cap, decode work limit, and deadline. Existing variants remain usable after exhaustion; a seventeenth value falls back to the unpruned base summary with summary_refinement_budget. No app, service or product name is matched, and no factory is added as a global relevance root.

ClassIdentityFactoryFixture checks two, 80 and 398 branch factories with first and last actual Class values; each returns only the selected allocation. It checks unknown/union inputs without creating new variants, distinct Class identities, the shared 16-variant limit and reuse, and an 850-branch factory exceeding the instruction boundary. Full capabilitySelfTest passed in 16 seconds: test/runs/generic-v15-class-tests.log.

The actual Fanqie generated dispatcher contains 1,992 instructions and 398 Class comparisons according to the independent packaged-DEX evidence. Its full control flow and the actual activity object chain still need measurement; this synthetic 398-branch positive is not application validation.

Separately, v14 Txws lost 78 setting and 130 callback oracle matches while bridge matches stayed constant. Investigation of its actual Fragment installation protocol is a higher-priority v15 validation item. Reintroducing lifecycle activation for every constructed Fragment would hide that boundary and is not part of this change.

The independent DEX shape audit additionally reports zero goto/switch/other-if instructions and zero nonpositive conditional offsets for the actual 1,992-instruction dispatcher, so it satisfies the bounded forward-selector control-flow eligibility. This still does not prove the full host call chain reaches it within budget.
