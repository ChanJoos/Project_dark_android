# Classic martial / cleric source integration — v68 candidate

User request: resume PR176, review warrior progress and consume cafe13434008 articles415456 and416044 in detail. Source: https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/415456 and https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/416044 . Original linked posts: https://m.blog.naver.com/alsea01/222080978801 and https://m.blog.naver.com/alsea01/222090013052 . Articles contain scrapbook bodies; the article's own contentHtml is NOT the full source. Public first-party article gateway supplied complete scrapbook tables and image elements, with no login bypass. Source provenance retains article identity, date, labelled element order, image URL, original bytes and SHA256. Do not save reader profiles/comments/account data as source evidence.

## Reviewed scope

27 martial rows and34 cleric rows (61 total);132 unique source media,56 unique GIFs. All table cells, descriptions, materials, prerequisites, trainers and stats are retained. Dara's author correction INT33→53 overrides the old table. Files are FAN_SEO_CLASSIC_2020 references, not official/current-server values or authenticated archive sprites.

Source maps58 existing IDs; adds stable SK_무도가_033 경신공법 and SK_무도가_034 아지토 without renumbering existing IDs. Benusti is retained as source evidence but remains excluded by the existing user-approved design; a general request to import source material does not silently resurrect an expressly excluded tame-monster system. Catalog221 real entries plus3 internal fixtures; source describes only pre-advancement skills, not every advanced spell in the catalog.

## Applied delta

-55 IDs /85 independent capture channels replace adapted art where source effects are identifiable. Caster sparks emit at accepted ACTION_STARTED; recipient effects emit at HIT_FEEDBACK, after shared legality/contact. Self-centric capture effects anchor to caster; target-centric captures anchor to each actual recipient. Existing warrior/rogue capture sprites are retained. Classic martial sources take precedence over their older finisher capture set.
- Per-channel GIF frame indices/durations, pivots, alpha limits and source/atlas hashes live in classic/manifest.json. Original actors, floor, HP bars and status icons are removed. SCREEN inverse matting is approximate; hidden/dark pixels and clipping remain unresolved. These are capture-derived candidates, not native archive equivalence.
- Full source descriptions and corrected stats feed mobile learning policy; project Gold/items/MP/cooldowns and combat balance remain explicitly adapted. Original training costs/prerequisites remain source records, not silently enforced as new gameplay gates.
-58 source icon/name bindings replace low-resolution screenshot crops. Two newly imported martial icons add to those, producing60 labelled runtime icon bindings.
- Fix source-kind misclassifications for martial magic, including Dara and life drain. Preserve all268 male/female BODY bytes. All classic spells use source CAST poses; front/spin kicks retain the existing rig. One-facing fan captures do not prove the selected all-gender/all-facing pose registration is pixel-identical.
- Fix Curoos to GROUP; Shield to caster-centered SELF; martial self dispels and cleric ally dispels no longer incorrectly target enemies. Record I-hyeong-hwan-wi reach3 while keeping actual jump/landing blocked.
- Correct old CapturedSkillFxTest to native graphics. Its old black legacy-graphics images were not usable visual proof, despite passing event assertions.

## Concrete unfinished scope

Existing actual damage/healing Resolver paths are preserved; persistent poison/root/freeze/buffs/regeneration/reflection/invulnerability, HP-finisher formulas/charges, speed changes, four-tile Shield collision, jump/landing, national inn teleport, party summon/MP transfer, corpse coma and monster summon services are NOT implemented by this source/VFX change. Previewable status entries remain labelled range/presentation tests. Utility/summon/jump actions remain fail-closed. No party member entities or tame-monster system exist. Source durations/MP/cooldown details and higher-version skill visuals are absent. Do not claim61 complete mechanics or221 fully implemented original abilities.

Source side-by-side alpha inspection revealed unavoidable actor cut-outs; silhouette/palette/timing/anchor candidates were reviewed against retained GIFs. Native composited frames and exact-source CI must be reviewed before delivery. Physical device / user visual acceptance remain pending.

## Governing files consumed

AGENTS.md; docs/CHAT_HANDOFF.md, PROJECT_STATE.md/yaml, DECISION_LOG.md, DIRECTOR_GUIDE.md, DIRECTOR_BACKLOG.md; design/DESIGN_CONSTITUTION.md and DATA_CONTRACT.md; docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md; master/MASTER_MANIFEST.md, SOURCE_OF_TRUTH.md, RECONCILIATION.md; docs/SKILL_SPATIAL_CONTRACT.md and SKILL_ACTION_DECISIONS.csv; tools/generate_skill_catalog.py, generate_skill_presentation.py, generate_mobile_skill_policy.py; existing capture provenance/extractor, BODY metadata and current runtime/test files. v67 PR176 head308bc751faf8d7eaaf3c43220e17cdc536b64498 compared with mainbfd668d4e178fa82625d634b5a54be0e27ce30a3. Previous Actions36715127755/job109886070337 passed; it built synthetic PR checkout56a0538424d72136c8a34dcac1de382fb6a0b9b4, not head-only. Earlier physical-device/visual gates remain pending.
