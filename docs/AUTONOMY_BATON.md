# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: DIRECTOR_BOOTSTRAP

HEAD / WORK LINE:
- Control-plane files were bootstrapped on `main`.
- Each scheduled run must rediscover the current shared integration line before coding; do not assume `main` or any historical PR is the active development line.

TASK:
- Bootstrap the autonomous development control plane and migrate scheduled agents to it.

SOURCES USED:
- `master/MASTER_MANIFEST.md`
- `master/data/` inventory and known Master/Source/Audit tables
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- existing DEV_HISTORY and runtime paths
- known user reference/device evidence summarized in `docs/PROJECT_STATE.yaml`

RUNTIME DELTA:
- None. This baton only establishes the new autonomous-development control plane. Subsequent scheduled runs must produce runtime deltas rather than continuing control-plane documentation work.

TESTS:
- N/A for documentation bootstrap.

ACCEPTANCE STATE:
- PROJECT control plane: IMPLEMENTED on main.
- Runtime: unchanged by this bootstrap.

STATE UPDATED:
- Added `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.
- Added `docs/PROJECT_STATE.yaml` with resource registry, scene integration targets, device/reference evidence, work packages, and claim/lease policy.

NEXT BEST ACTION:
- First scheduled agent: sync repository, verify current integration line and current changes after the latest tested device build, then claim the highest-value eligible READY package in `PROJECT_STATE.yaml` and implement an actual end-to-end runtime delta.

BLOCKERS:
- None for autonomous execution.

## Baton write format for future agents

Keep future updates short and replace the `Current baton` section with:

- `AGENT`
- `HEAD / WORK LINE`
- `TASK / WORK PACKAGE`
- `SOURCES USED`
- `RUNTIME DELTA`
- `TESTS`
- `ACCEPTANCE STATE`
- `STATE UPDATED`
- `NEXT BEST ACTION`
- `BLOCKERS`

Do not copy the full project state into this file.