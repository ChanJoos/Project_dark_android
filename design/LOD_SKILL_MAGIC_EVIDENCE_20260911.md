# PROJECT DARK — SKILL / MAGIC EVIDENCE INDEX — 2026-09-11

Status: RESEARCH INDEX / `[FAN]`, not official numeric canon
Source: user-supplied PDF snapshot `어둠의 전설/기술마법`, modified 2026-08-02.

This file records system structure and named action inventory from the supplied source. It does NOT promote fan-reported damage formulas, probabilities, MP costs, cooldowns, durations, requirements or current-patch behavior to official canon. PROJECT DARK current runtime scope still ends at first advancement; 2nd/3rd jobs are future inventory.

## 1. Fundamental action model
The source gives a critical distinction:
- `SKILL/기술`: generally no cast time; commonly has cooldown; predominantly used by physical/격수 jobs.
- `SPELL/마법`: generally has cast time; commonly little/no cooldown; source reports an activation-rate constraint; predominantly used by non-격수 jobs.

Therefore PROJECT DARK must not flatten every action into one generic cooldown button. `ActionDefinition` needs independent fields for at least:
- action kind: SKILL / SPELL
- targeting mode
- range/shape
- cast time
- cooldown
- resource costs (HP/MP/item/etc.)
- prerequisite/learning relation
- status/effect payload
- usable context restrictions
- animation/effect IDs
- evidence status / era.

All numeric values remain evidence-gated unless separately corroborated.

## 2. Learning / proficiency versioning
The source explicitly describes different client eras:
- old client: prerequisite skills/skill levels could gate later skills;
- new client: prerequisite-subject system reportedly removed and skill level replaced by proficiency;
- up through circle 4, modern level-up rewards reportedly make many actions freely learnable;
- level 99+ acquisition differs.

Design consequence: learning rules require an `era/ruleset` layer. Do not hard-code one historical/current acquisition model into immutable action data.

## 3. Common action inventory
Named common actions include:
- 기본공격
- 문열기
- 탐색
- 아들레스투 (item appraisal)
- 휴식
- 수영
- 노점개설
- 채집
- 낚시
- 쿠로토 (self heal)
- 일반제조
- 아이템 고치기
- 마나스페라 (advancement-context action)
- 성장 (later-job learning action)

This confirms action infrastructure is broader than combat. Doors, terrain capability, inspect, rest, commerce, gathering, fishing, crafting/repair and item inspection belong to the action-domain inventory.

## 4. Warrior — 1 to 99 / first-advancement inventory
### Pre-99 / circles
- 레스큐: taunt/aggro manipulation.
- 숏블레이드.
- 더블어택: basic-attack augmentation/extra hit relationship.
- 윈드블레이드: forward line/range attack.
- 디바투: movement-lock dispel.
- 트리플어택.
- 메가블레이드: four-neighbor area attack.
- 바투: movement immobilization without necessarily disabling all actions.
- 투핸드어택: equipment capability.
- 드래곤모드: self offensive mode with HP drain.
- 적무기쳐내기: PvP equipment disruption/restriction.
- 완전방어: physical defense/immunity family.
- 매드소울: HP-cost finisher family.
- 크래셔: low-HP conditional finisher family.
- 피닉스모드: upgraded offensive mode.

### First advancement / Fighter inventory
- 메가어택
- 돌진: forward multi-tile movement attack.
- 집중: finisher amplification buff.
- 포효: broad movement-lock/debuff.
- 매드소울진
- 데빌크래셔
- 스톰블레이드
- 룬블레이드

Design identity: tank/aggro + frontal/area melee + HP-risk finishers + physical defense. Preserve role distinction.

## 5. Thief — 1 to 99 / first-advancement inventory
### Pre-99 / circles
- 자물쇠열기
- 찌르기
- 센스몬스터: monster information revelation, progressively including HP/EXP/attack/defense attribute according to source.
- 표창날리기: ranged attack/pull.
- 밀기: positional displacement.
- 아무네지아: monster target/aggro reset.
- 연막탄 터뜨리기: consumable/context-linked control.
- 찔러휘비기
- 더블어택
- 품뒤져보기: inspect another character's inventory.
- 센스: player inspection.
- 라이트닝무브: evasion buff family.
- 하이드: stealth with detection/AoE exceptions.
- 함정찾기1/2
- 두번찌르기
- 습격: recognition/aggro-state-dependent burst attack.
- 함정해체
- 센서스: stealth detection.
- 적갑옷해체
- 암살격: HP-cost finisher/critical-variance family.

### First advancement / Master inventory
- 기습: stronger recognition-state attack + armor disruption relationship.
- 트리플어택
- 함정파기
- 습격진
- 백스텝
- 암살격진
- 마구찌르기
- 슬래쉬
- 백슬래쉬
- 하이더: stealth applied to another character.

Design identity: information asymmetry + stealth/detection + aggro manipulation + positioning + traps + conditional burst. This is substantially more than generic high-DPS melee.

## 6. Priest — 1 to 99 / first-advancement inventory
Core identity is the only base-job family explicitly centered on healing others; healing scales with WIS in this source.

### Pre-99 / circles
Dispel families:
- 디렌토
- 디바르도
- 디베노모
- 디나르콜리
- 디데프레카
- 일루메나
- 디프라바
- 디소루마
- 리베라토 (broad dispel with exceptions)
- 코마디아 (coma recovery)

Healing families:
- 쿠로
- 쿠러스 (group)
- 쿠라노
- 쿠라노소
- 쿠라누스 (group)
- 수페라쿠라노
- 쿠라네라 (group)
- 엑스쿠라노
- 엑스쿠라네라 (group)

Buff/protection/control/utility:
- 아지토
- 벨라르모
- 에나르마
- 베누스티
- 쿠랄툼
- 실드
- 콜라마
- 수페라벨라르모
- 로카메아
- 리치마나
- 호르라마
- 이모탈
- 리플렉토
- 디내추라
- 소모니아

Attack magic inventory:
- 블레스
- 홀리쇼크
- 홀리볼트
- 홀리블로우

### First advancement / Master inventory
- 홀리쿠라노
- 칸의 축복
- 신의 축복
- 홀리큐어
- 홀리드래곤
- 엑스벨라룸
- 수페라에나르마
- group curse/status dispel families: 디렌타/디바르디아/디데프레타/디프라베라/홀리큐레스, 디베노메라/일루메룸/디나르콜룸/디소루메라.

Design identity: rapid targeted healing + group healing + dense dispel responsibility + mitigation/buffs + limited offense. Party UI and target-selection UX must be designed around this role rather than treating healer actions as ordinary attack buttons.

## 7. Wizard — 1 to 99 / first-advancement inventory
### Elemental attack ladder
Water/earth/wind/fire families occur across tiers, with single-target, nearby/area and screen/broad-area variants. Named families include:
- 마레노 / 테라미코 / 아듀로 / 플라모
- 수페라* variants
- 마레누스 / 테라미쿠스 / 아듀로스 / 플라무스
- 엑스* variants
- 마레네라 / 테라미에라 / 아듀레나 / 플라메라
- 마네나로 / 테라미칼로 / 아듀랄로 / 플라미칼로

Control/debuff/utility:
- 아지토
- 렌토
- 콘푸지오
- 베노미
- 바르도
- 나르콜리: sleep/control with damage-interaction evidence.
- 로카테오
- 딜루메니
- 데프레코
- 소루마
- 프라보
- 데스

Dark/high-cost attack family:
- 세멜리아
- 아마게돈
- 숨마스텔라
- 라그나로크

### First advancement / Master inventory
- 메테오
- 속성강화
- 매직프로텍션
- 어둠의각인
- 아이스블러스트 / 퀘이크 / 플레쉬스톰 / 플레어
- 렌티아 / 바르데아 / 데프레타 / 프라베라 (broad curse variants)
- 델리스펠라스 / 포트리스

Design identity: elemental choice + curse/debuff sequencing + control + cast-time pressure + high-cost broad attacks. `targetingMode`, `element`, `castTime`, `areaShape` and `status interaction` are mandatory data concepts.

## 8. Martial Artist — 1 to 99 / first-advancement inventory
This is especially important because the current PROJECT DARK player visual reference is a martial artist.

### Pre-99 / circles
- 정권: initial unarmed attack skill.
- 단각: front kick; source ties damage to balanced STR/CON relationship.
- 이형환위: vault over target to its rear; source documents side/rear basic-attack bonuses.
- 양의신권: double-attack family.
- 통배권: poison/internal-injury style damage-over-time.
- 미종보법: evasion family.
- 일루메나 / 디베노모: dispels.
- 붕각: side-kick upgrade.
- 백보신권: forward 3-tile family.
- 일음지: frontal blind.
- 경신공: historical movement-speed action whose practical effect changed/was disabled by era.
- 흡정신공: low-target-HP conditional drain.
- 쿠랄툼: regeneration.
- 철포삼: self defense.
- 선풍각: four-neighbor kick attack.
- 발경: freeze/control strike.
- 소수신공: basic-attack amplification family.
- 장풍: ranged attack magic; source contains era/attribute wording that needs corroboration.
- 금강불괴: invulnerability/protection family; source notes later toggle behavior.
- 쿠라노토: self heal.
- 달마신공: frontal HP-cost fixed-damage style finisher.
- 구양신공: four-neighbor HP-cost finisher.
- 반탄신공: technique reflection family.
- 다라밀공: iconic ranged super attack consuming both HP/MP resources; cast/target phase evidence.

### First advancement / Master inventory
- 무영신공
- 자기보호
- 트리플펀치
- 연천단각
- 붕신선각
- 파천각

Design identity: unarmed kicks/punches + repositioning + hybrid dispel/defense + control + ranged ki attacks + HP/MP-risk finishers. Current character art must therefore support distinct punch/kick/cast-like action families later; it must not assume weapon attack animation.

## 9. Position and facing are combat mechanics
The source contains multiple actions whose semantics depend on facing/geometry:
- front line/range attacks (윈드블레이드, 백보신권, etc.)
- four-neighbor attacks (메가블레이드, 선풍각, 구양신공)
- multi-tile charge (돌진)
- vault-behind (이형환위)
- side/rear attack modifiers
- target-centered area attacks.

PROJECT DARK implication: the current exact `NW/NE/SW/SE` world-facing contract is not merely animation. Combat geometry must consume the same logical facing and tile topology. Do not implement combat hitboxes in screen pixels.

## 10. Status-effect taxonomy required
At minimum reserve data/runtime semantics for:
- movement lock/paralysis (`바투` family)
- poison (`베노미`/통배권 family)
- sleep (`나르콜리`)
- freeze (`소루마`/발경)
- blind (`딜루메니`/일음지)
- stealth (`하이드`)
- stealth detection (`센서스`)
- confusion/target disruption (`콘푸지오`, 아무네지아)
- silence (later Bard evidence)
- coma/death-recovery state
- physical/magic immunity/protection
- reflection
- defense/attack/HIT/evasion modifiers
- attribute modification.

Each status needs explicit dispel relationships and effect categories; do not implement them as arbitrary booleans in UI code.

## 11. Aggro / recognition is first-class
The source repeatedly relies on monster recognition state:
- Warrior 레스큐 deliberately acquires aggro.
- Thief 아무네지아 drops target recognition.
- 습격/기습 family depends on whether the monster recognizes the thief.
- ranged skills can be used to pull monsters.

Therefore future `MonsterAI` requires explicit threat/recognition state and events. This is gameplay canon inventory, not an incidental AI implementation detail.

## 12. Party combat is a sequencing system
The source repeatedly describes party chains such as:
- reveal/communicate attribute;
- curse/defense reduction;
- sleep/control;
- burst/finisher;
- healer recovery after HP-cost finishers;
- group buff/dispel maintenance.

Design consequence: party play depends on interoperable effects and timing. Avoid per-class isolated combat subsystems that cannot compose through common status/effect/action events.

## 13. Second/third jobs — future inventory only
The document includes later jobs such as:
- Warrior → 검투사 → 나이트
- Thief → 궁수 → 암살자
- Priest → 바드 → 클레릭
- Wizard → 소환사 → 정령사
- Martial Artist → 수인 → 기공사

These are recorded for future schema compatibility only. PROJECT DARK current implementation scope does NOT expand beyond first advancement.

## 14. Evidence cautions
The source itself warns official-site information can be stale, and this community document mixes historical and current-client behavior. Several entries explicitly describe changed/removed/broken mechanics. Therefore:
- names and structural relationships are useful `[FAN]` evidence;
- exact values are not official truth;
- every action should eventually carry `era` and `evidence` metadata;
- contradictory historical/current behavior must not be collapsed into one value.

## 15. Immediate implementation impact
Do NOT stop the current visual sprint to build these systems.

After the first playable visual slice is accepted, the next combat/data architecture should be checked against this evidence before implementation. Minimum future schema requirements are now known: `actionKind`, `targetingMode`, tile-space `range/shape`, `castTime`, `cooldown`, HP/MP/item cost, element, status payload, dispel category, prerequisite/proficiency, equipment/context restrictions, animation/effect ID, era/evidence tag.

For the current martial-artist sprite pipeline, preserve extensibility for at least: basic punch, front kick, side/spin kick, cast/ki pose, hit, dead, and the existing four diagonal directions. Exact frames remain visual-evidence gated.