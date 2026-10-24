# V271 five profession companions and agents

User authorization 2026-10-24 KST: implement all professions and an agent for each.
Continue PR198/codex/companion-party-v269 from957bc03, mainf86d961 unchanged.

## Runtime contract
All five playable professions have a selectable companion: warrior, priest, rogue, mage and martial artist. The existing one-active-companion capacity is retained. Temporary APK access starts all five at99 with all own-job non-retired skills learned, independent of player quests/job/level. Player progression remains unchanged. Normal mode retains recruitment/growth rules.

CompanionController owns lifecycle, target selection, map index and bounded navigation; it dispatches to separate WarriorCompanionAgent, PriestCompanionAgent, RogueCompanionAgent, MageCompanionAgent and MonkCompanionAgent implementations. CompanionAgentContext exposes shared observations and Resolver submissions, not direct HP/damage mutations. Decisions are deterministic local game agents, without external AI services. Each receives actual resources, statuses, threats, geometry and cooldowns.

- Warrior: retain accepted taunt/blades, supported soul and actual low-HP crash conditions, sleep/burst reservations.
- Priest: urgent healing, matching dispels and protections. Never curse/transfusion or offensive basic attacks, including WAIT.
- Rogue: companion-origin first-target shuriken ray, hide->ambush, evasion and bounded retreat under threat. Setup interval prevents repeated hide loops. Health-consuming assassination requires high HP and protection; no invented rear/flank multiplier.
- Mage: self healing, danger freeze/armor, sleep on an additional enemy, bounded retreat, strongest nonduplicating curse for durable enemies, actual element table and recipient-shape-aware damage choice. Avoid waking sleepers, stealing burst reservations, pulling idle bystanders or dumping all MP into weak targets. Spell cadence and costs remain shared-engine gates.
- Martial artist: self blind/poison dispel, own heal/armor/protection/regen, freeze strike and area selection for multiple actual recipients. Dara requires sufficient resources, high HP and active invincibility; real release leaves its own HP/MP1. Its original3s contact is retained. No resource/cost or source coefficients invented.

WAIT never moves or acquires new enemies; priest may support, mage/monk may defend against their own attacker. Menus/death/control suppress agent actions. New professions use the existing job equipment and original renderers; no new art bytes. Original utility/service gaps remain fail-closed. Companion LEAP/ADVANCE player-only movement services explicitly reject until an actor-specific adapter exists; AI does not select them. All-skill knowledge does not imply automatic use of every utility.

## Shared corrections needed by the new actors
Rogue rays use the companion position/facing, respecting the first occupied actor. Learned rogue/martial double/triple passives affect actual companion basic hit count, capped at3. Self heals cannot target another party actor. Companion stealth is respected by enemy begin/contact guards and cancels only attacks targeting that companion; player stealth behavior is preserved. Reflection works for the companion recipient. Skill source bytes, damage coefficients and player facing controls remain unchanged.

## Save compatibility
Party snapshot2 stores five members; snapshot1 must contain exactly the legacy warrior/priest pair and migrates new professions with fresh defaults. Validate the complete staged roster before changing live state. HP/MP/death/order/cooldowns remain per member. Legacy two-member effects migrate to five; malformed partial rosters fail closed. Inactive effects remain paused across selection rather than deleted as nonexistent monsters. Test re-entry preserves resources, death and cooldowns. Original warrior/priest stat distribution retained; rogue favors DEX, mage INT and martial balanced STR/CON. New companion growth/policy thresholds are PROJECT_ADAPTED.

## Verification scope
Changed: party/agent/controller/UI, companion hit/target/stealth gates, optional party save schema. Focused: all current companion tests plus real native five-job UI/entry/restart, shared spatial/timing and HUD/inventory adjacency. Full current32gate regression is required because party save migration and shared combat adapters changed; retired V268 groups remain retired. Host/native evidence is separate from user phone/visual acceptance. APK must retain exact source/run/signature/hash proof.
