# World PASS 32 validation

- Before fix, valid off-grid exploration targets could exhaust `MAX_EXPANSIONS=4096` because ground tolerance (6) was smaller than 16-unit lattice quantization error (~11.31 max).
- After fix, representative route searches converge in 12–49 expansions in the equivalent collision model.
- Exact target movement remains incremental through `Walker.tryWalkStep`; no teleport/collision bypass was introduced.
- `WorldMoveTargetExplorationAudit` is the repository regression gate for this behavior.
