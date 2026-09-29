# Game skill book contract — v60

User direction (2026-09-30) supersedes v59: learning requires only final STR/INT/WIS/CON/DEX plus one-time Gold/material payment. Job, level, circle, advancement, mastery and previous skills are not learning blockers. Starting COMMONER may learn any skill meeting its stats and price.

Original Master catalog and capture bytes stay preserved. The reproducible generator tools/generate_mobile_skill_policy.py writes separate skills/mobile_learning.json for all 219 Master IDs and three hidden fixtures. Captured stat numbers are reused; unresolved numbers use explicit project tiers. Prices 50/150/500/1200/2500/5000 Gold and six reagent recipes are PROJECT_ADAPTED_V60 design, not original-server facts. Basic attack/door opening are free. CI regenerates and diffs.

SkillAcquisition.quote exposes all required/current stats, Gold/owned Gold and named materials/counts. RpgProgressionState validates all resources before paying. An explicit 습득 tap immediately pays/learns/checkpoints in the book; no NPC or extra confirmation. Opening the book never automatically spends money. Duplicate taps reject; save failure restores Gold/inventory/equipment/skills. Eight slots require learning; same-skill registration clears the slot; persistence failure restores the old assignment. Book slot taps select details; field HUD taps execute.

Twelve cards in four columns/three rows share centered icon/name baselines and at most two name lines. Explicit description/condition tabs are stable on body taps. Condition tab shows all five stats and exact costs/owned quantities. Description uses player-facing effects and actual implemented MP, power/heal, cooldown and target. Developer trial/capture audit prose is removed from the window.

135 source crops keep exact ID mapping. Cyan screenshot-background corners are masked only in native presentation cache and clipped; original atlas/provenance stay unchanged. Missing art is neutral, never borrowed.

Existing 29 B/ADAPTED attack/self-heal definitions and basic weapon attack remain the combat scope. Unsupported special effects show 전투 효과 준비 중 and no fake numeric values. Learning does not implement them. Elemental/AOE/utility/stealth/advanced effects remain pending. Existing shared resolver owns MP/hit timing/cooldown; duplicate slots share timers. Cooldowns remain session timers. Proficiency save compatibility remains but no learning condition uses it.

SkillWindowTest covers UI learn/register/clear, stat/Gold/material rejection, no job/prerequisite gate, duplicate payment, save-failure rollback, persisted Gold/items/skills restart, all policy IDs, stable descriptions, exact-ID rendering and cooldown blocking/expiry. Native Canvas evidence is not physical-device acceptance. Closure records exact SHA/CI/artifact/hash and v59 user rejection in PROJECT_STATE/handoff.

