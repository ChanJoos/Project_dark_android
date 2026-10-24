# V262 user equipment/HUD revision

**Facing section WITHDRAWN by user rollback V263.** Its two correction policies are historical, not approved current behavior. See [V263](FACING_ROLLBACK_V263.md). The other independent UI/equipment requests remain in effect.

Latest user correction: move the whole6x2 quickskill grid right one cell (39 logical pixels), not1.5. Attack visual radius31→37.2 (+20%). Keep button inside960x540 with no circular overlap against skill cells; input uses the same center. Remove independent equipment utility icon and close the rail gap.

Inventory button opens paired equipment LEFT and bag RIGHT; either close closes both. Preserve source artwork; affine transforms and inverse touch routing share constants. Equipped copies are removed from visible bag quantity; ownership/save map retains total owned quantity and equipment references (no schema change). Duplicate loose copies remain visible. Equipment slot selection and footer unequip return worn copy to bag. Paired item targeting remains explicit.

Quick necklace use swaps the actual tapped slot with the previously worn necklace. Automatic Sense uses the same path and replaces its chosen first matching slot. Save rejects/read-only preserve equipment and slot state. No extra ownership, grants or consumption.

DEVICE_FAILED_USER_REPORTED: direction rollback still fails when monsters are numerous; installed source UNKNOWN, latest delivered V261. Old ranged monster submitAction only updates exact adjacent tile heading, then locks stale walk direction for ranged attacks. New code locks actual opponent vector on accepted action only; no idle turning or positional damage. Reproduce old ranged failure and test four vectors plus dense player basic contacts. Player basic contact also follows a selected monster that changes sides before impact while the original pose stays locked. On actual HIT_FEEDBACK, align only to that resolver recipient (including lethal hit), not a newly selected monster. Reproduce this on old GameView before final checks. No idle tracking or damage rules change. These reproduced paths are scoped causes; physical device parity and user visual acceptance remain PENDING.

Verification scope: new layout/input/native captures, bag equip/unequip/duplicates/restart, quickslot duplicate-binding exchange/restart/read-only/consumables, direction old failure/final4vectors/dense player, adjacent basic1/2/3 AUTO/chase/save. Unrelated historical full-suite issues remain separate; branch candidate, not main acceptance.
