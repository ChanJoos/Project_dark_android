# Naver Cafe offline source archive — 11 of 11 articles

## Completed collection — 2026-10-01

Both Rogue bodies 245450/245456 were read from the user's existing authenticated browser session, without interacting with authentication origins. All 11 requested article bodies are retained. The complete page-observed media inventory has 350 image references, 288 distinct URLs, 276 unique byte files and 96 GIFs. Offline SHA-256/byte-size verification passes 288/288 URLs. Original response bytes are not reencoded.

Open `index.html` for an offline review of all 11 articles with locally linked images/GIFs. Original text/HTML snapshots remain in article JSON; the Warrior 1-circle snapshot has text/images but no original HTML. `skill_evidence.json` has 133 sections/table rows and eight explicit conflicts. `rogue_evidence.json`/`ROGUE_ANALYSIS.md` retain detailed per-section bindings and ambiguity boundaries. Comment-account/profile data are not retained; all comment pages are not part of the collection-complete claim.

The authentication blocker in the historical checkpoint below is resolved for this session. Runtime icon coverage and unfinished mechanics remain unchanged. Six Rogue magic-kind corrections are recorded in `master/changes/ROGUE-2015-SOURCE-REVIEW.json` as PROPOSED; they are not yet runtime changes.

Reproduce with `python tools/collect_cafe_archive_media.py`, `python tools/index_cafe_skill_evidence.py` and `python tools/build_cafe_archive_viewer.py`. The explicit media `--fetch` now reuses verified URL records and downloads only missing/corrupt retained observations.

## Historical 9-of-11 checkpoint (superseded collection status)

Cafe access remains authorized during collection; future revisits are allowed when necessary. The user requested sufficiently complete retention for offline analysis.

Nine bodies are retained: 191105, 191110, 191114, 191241, 191445, 191777, 401229, 415456, 416044. All observed media URLs from those bodies are retained: 225 URLs, 213 unique files, including 65 GIFs. 86 new binary files are included here; other entries reference already tracked identical bytes. HTTP response bytes were not reencoded. Warrior CDN variants remain labelled PAGE_OBSERVED_CDN_VARIANT rather than asserted original-resolution files. Text/HTML snapshots, SHA-256, media dimensions/frame durations and source row bindings are available offline.

`skill_evidence.json` retains 95 labelled sections/table rows and two explicit source conflicts. See ANALYSIS.md. Historical fan descriptions and measured formulas are preserved separately from accepted project balance. This archive checkpoint does not accept new Master mechanics or complete missing icons, BODY poses, effects or the skill window.

## Remaining collection

Rogue articles 245450 and 245456 redirect to Naver login. Automatic approval review denied authentication-origin access. No alternative route around that denial was attempted. Explicit permission for the Naver login procedure is required to continue these two pages; credentials must use the secure authentication handoff.

## Offline verification

Run `python tools/collect_cafe_archive_media.py` for integrity checks without network, and `python tools/index_cafe_skill_evidence.py` to regenerate the evidence index. Only an explicit `--fetch` permits media collection.

## Verification boundary

Runtime/test source 4f8bec32c2d8d8c54d87c919c2a884317c39c0b5 passed the complete configured GitHub Actions workflow, run 36776004466. This archive checkpoint changes sources, analysis and documentation only. Physical-device behavior and original visual acceptance remain pending. Original icon coverage remains 140/221 (81 gaps); all skills are not complete.
