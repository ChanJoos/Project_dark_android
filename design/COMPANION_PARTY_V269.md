# V269 companion party candidate

User authorization (2026-10-24 KST): implement the revised profession-aware party proposal.
Accepted V266 runtime/main remains the base. V268 retired checks remain retired.

## First playable scope
One selected WARRIOR or PRIEST companion; later professions and three-member parties remain future work.
Recruit/dismiss/recover in existing towns after job choice and Lv3. HUD management replaces no existing combat controls.
Follow, explicit focus, wait; select ally/self for support. Existing source BODY/clothing/actions only.

## Combat contract
- All player, monster and companion submissions use the same RuntimeCombatSession/Resolver, SkillAbilityCatalog formulas, status families and defeat ledger.
- Companion actors have separate HP/MP, action snapshots and cooldowns. No player-equipment cast/MP discounts are borrowed.
- Ally heal/protection/clear skills address the selected party actor; hostile damage cannot hit friendly actors. Self-centered offensive shapes retain the existing player contract.
- Warrior learns a bounded PROJECT_ADAPTED progression by stage. Normal strikes and blades at early stages; actual SOUL/CRASH costs and <=2% HP gate at 99. No invented automatic SOUL->CRASH chain, consumable grants, or free low-health setup.
- Priest uses affordable single healing sized to missing HP, danger-aware healing, matching dispels and protection. No transfusion, curse or blanket dispel in AI. Self immunity/reflection is self-directed.
- Sleep is not broken by a companion basic hit; eligible warrior burst can claim the hit. Bounded burst reservations prevent conflicting companion attacks or routine heals during a player CRASH. Rejection/death/expiry/contact releases reservation. Manual player inputs remain available.
- Monster threat can target the active companion; death/dismissal returns targeting to player. Hidden-player semantics do not make a visible companion invisible.
- Companion kills enter the same player reward ledger once; player-only skill-use quests/practice remain player-only.

## Movement and lifecycle
BFS is capped at2048 visited cells per decision, decisions at0.25s, existing tile cadence/terrain/traversal used. No combat teleport; map arrival places on an existing traversable nearby tile. Existing world navigation tiles are cached per adapter. Party members do not block player movement.
HP/MP/death/order/cooldowns and companion status durations are checkpointed. Old saves without party fields start with no companion. Rejoin/reload preserves resources. Town recovery is explicit. Corrupt optional party data disables writes rather than overwriting the save.

## Evidence boundaries
PROJECT_ADAPTED growth table and decision timing are new single-player rules, not claimed original values. Skill source PDFs contain version differences; no approved base coefficient was changed. Source art bytes are unchanged.
Domain tests cover actual shared combat, party resources, CRASH gating, support policies, cooldown/death persistence, reservation, monster target, movement, sleep and recovery decisions.
Native Android rendering/input/restart, adjacent combat/spatial/timing/inventory checks and exact-source APK build are required before delivery. User phone/visual acceptance is a separate step.
