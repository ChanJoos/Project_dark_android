# Offline skill source analysis checkpoint

## Coverage

Nine requested article bodies are retained: six Warrior circle/promotion posts, the finisher measurement post, Martial Artist and Cleric tables. Rogue technique article 245450 and magic article 245456 redirect this browser to Naver sign-in; their bodies are not retained in this checkpoint. Automatic approval review rejected authentication-origin access. Neither a title nor a redirect is treated as evidence of the missing skill information.

`skill_evidence.json` indexes 95 sections/table rows: 25 Warrior sections (including shared Kuroto and item repair), nine finisher measurement sections, and 61 existing labelled Martial/Cleric rows. It preserves exact source sections, stats, costs/prerequisites where explicitly present, motion/effect descriptions, formula lines and catalog ID candidates. This source index does not itself alter canonical Master or runtime behavior.

## Source differences requiring explicit handling

| Topic | Retained evidence | Application rule |
|---|---|---|
| Crasher/DevilCrasher | Warrior narrative describes element-dependent multipliers; finisher post explicitly measures at AC0, attack water / defense wind and distinguishes maximum HP. Its values are 2× / 4× maximum HP and 12.5s cooldown. | Keep source, measurement context and historical variant together. Do not overwrite runtime balance merely by choosing the largest multiplier. |
| MadSoul/MadSoulJin | Finisher post states 0.5× / 0.55× current HP, 27s cooldown; older Warrior narrative uses different approximate coefficients. | Record current vs maximum HP explicitly; older approximations are not identical measurements. |
| Assassination/AssassinationJin | Two / five coefficient variants are stated. The post does not establish the probability or precise condition for each variant. | Preserve all listed coefficients; selection conditions remain unresolved. |
| Dara | `(current HP + current MP - 1440) × 2`, 12s; Martial table still prints INT33 but its header explicitly corrects to INT53. | Keep original table and correction. Existing labelled definition already uses INT53. |
| Guyang/Dalma | Guyang is four-side/current-HP based. Dalma explicitly ignores buffs/debuffs and can penetrate Immortal in the measurement post. | Do not generalize Dalma's exception to every finisher. Original damage/service behavior still needs accepted implementation and verification. |
| Complete Defense | Warrior source states physical protection, 15s duration, 27s delay, jump-attack pose and capsule-like shield effect. | Keep physical-only protection separate from Cleric Immortal/Martial invulnerability; visual source is not proof of implemented protection. |
| Batu/Roar | Batu blocks movement but allows other actions. Roar is a monster-only screen-wide application. | Preserve movement-only restriction, recipient type and visibility scope independently. |
| Dragon/Phoenix | Source gives duration, HP drain per second and DAM/HIT bonuses. | A one-shot effect or animation does not implement persistent ticking state. |
| Double/Triple/Mega/TwoHand | Basic attack-linked hit structures and equipment eligibility are described. | Do not present them as independently cast active skills just because an icon exists. |
| Acquisition | Source includes historical class, trainer, prerequisite/proficiency, materials and Gold. | Preserve source rules, while retaining the user's approved project learning policy (stats + Gold/materials, without reinstating old class/proficiency gates). |

## Icons, body motion and effects

- `media.json` binds each source URL to article ID and image ordinal, exact retained bytes, SHA-256, format, dimensions, frame count and per-frame duration. Existing identical source files are referenced instead of recompressed copies.
- Nested original media URLs explicitly present in rendered thumbnail URLs are retained. Warrior CDN variants are labelled as page-observed variants; source-resolution identity is not invented.
- Classic labelled rows retain their exact icon/demonstration indices and original source URLs. Table images, trainer maps, overview panels and formula graphs must not all be mistaken for skill icons.
- Source words such as two-hand or jump-attack are pose evidence. Matching male/female BODY groups and equipment registrations requires source/frame comparison; copying one observed facing does not verify all four directions.
- Caster/recipient effect separation, contact time, anchors, blend and floor/actor masking remain distinct from the original GIF bytes. The existing classic renderer uses classic channels ahead of finisher fallback where both are available; source/version choice must stay explicit.
- Current source-icon coverage is still 140/221 IDs. No new icon crop, Master acceptance or runtime motion/effect binding is claimed by this archival checkpoint.

## Offline reproduction

Run `python3 tools/collect_cafe_archive_media.py` to verify retained bytes without network access, and `python3 tools/index_cafe_skill_evidence.py` to rebuild the source-section index. The explicit `--fetch` switch performs downloads only from URLs already preserved in source snapshots. Cafe bodies are not requested by either command.

## Runtime verification boundary

The remaining classic-source tests were corrected to drain queued ACTION_STARTED events through the production frame tick before checking caster pulses, and to expect the already-selected Classic Capture recipient art for Kurus. This changes test timing/expectations, not damage or runtime rendering logic. Source `4f8bec32c2d8d8c54d87c919c2a884317c39c0b5`, Actions run36776004466 passes the relevant unit-test step; full workflow result must be recorded separately. Physical-device and user visual acceptance remain pending.

## Final CI observation

Actions run36776004466 completed with conclusion SUCCESS for runtime/test source4f8bec32c2d8d8c54d87c919c2a884317c39c0b5. This does not establish physical-device or original visual acceptance.
