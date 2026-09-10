# Pass 14 Director runtime gate

Base c1b23b4. Includes world worker's 801be36 layer contract without overwriting it.

IMPLEMENTED:
- `ui/TouchOwnership` owns joystick by pointer ID, handles independent pointer release and CANCEL.
- UI hit regions precede world entity taps. Popup/pause/back clear held input. NPC/Inventory modal pauses local gameplay.
- Existing RpgInteractionController now connects pickup/inventory/equip to actual View controls; no fabricated reward mapping.
- Dead player cast/skill/kick entry points reject before resources/cooldowns/damage.
- Removed fake GROUP members, quest 0/4 and 35% EXP. Unknown progression remains --.
- Removed network screenshot background. Geometry QA renderer exposes actual collision rectangles and clearly labels original map/sprites unavailable. It is NOT original visual completion.
- CI validates all Master bytes, pointer rule tests, compile/assembleDebug/assembleDebugAndroidTest, APK identity/signature, real Android instrumentation and compositor screenshot collection.
- Instrumentation explicitly uses QA-only ground drop fixture; it does NOT claim canonical monster rewards, quest, EXP, creation or persistence are complete.

User added official-site prefix to all 4 schedules: Director owns investigation through Master revision, code adoption, testing and future instructions. Updated automation results confirmed success; unchanged schedules are hourly :05/:20/:35/:50 Asia/Seoul display.

BUILD VERIFIED / RUNTIME VERIFIED: awaiting CI evidence at time of this commit. Subsequent handoff records exact SHA/run and outcomes.
