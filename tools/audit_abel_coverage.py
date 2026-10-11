"""Report admitted coast coverage without certifying an incomplete original roster."""
import argparse, json, re, zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--require-complete', action='store_true')
parser.add_argument('--output', type=Path)
args = parser.parse_args()
coverage = json.loads((root/'master/source/abel_v142/coverage.json').read_text())
java = root/'app/src/main/java/com/projectdark/mobile'
spawn = (java/'AbelCoastMonsters.java').read_text()
roster = re.search(r'ALL=Collections.unmodifiableList\(Arrays.asList\(([^)]+)\)\)', spawn).group(1)
admitted = set(re.findall(r'"(ABEL_[A-Z_]+)"', roster))
names = {row['id']: row for row in coverage['entries']}
assert admitted <= names.keys(), 'Spawn identity missing from coverage inventory'
art = dict(re.findall(r'art.put\("(ABEL_[A-Z_]+)","([^"]+)"\)', (java/'PoteForestMonsterShowcase.java').read_text()))
rewards = set(re.findall(r'add\(m,"(ABEL_[A-Z_]+)"', (java/'AdaptedCampaignRewardCatalog.java').read_text()))
assert admitted == set(art) == rewards, 'Spawn, art and rewards disagree'
assets = set()
for version in (139, 140, 141, 142):
    with zipfile.ZipFile(root/f'master/assets/abel_v{version}/generated_assets.zip') as bundle:
        assets.update(bundle.namelist())
for ident in admitted:
    for state in ('idle','walk','attack'):
        for facing in ('nw','ne','sw','se'):
            path = f'pote_monsters_generated_v1/sprites/{art[ident]}/{state}_{facing}.png'
            assert path in assets, path
    assert names[ident]['status'] == 'CANDIDATE'
required = {row['id'] for row in coverage['entries'] if row['scope'].startswith('COAST')}
missing = sorted(required-admitted)
unresolved = [row for row in coverage.get('unresolvedSilhouettes', []) if not row.get('identity')]
complete = (coverage['rosterClosed'] and not missing and not unresolved
            and all(row['originalVisualAccepted'] for row in coverage['entries'] if row['id'] in required)
            and coverage['secondVideo']['frameReview'] == 'COMPLETE')
report = dict(wholeRosterComplete=bool(complete), runtimeCandidates=sorted(admitted),
              suppliedVideoVisualFamiliesCovered=coverage.get('candidateVisualRosterCovered',False),
              knownRequiredMissing=missing, candidateCount=len(admitted),
              knownCoastAndEventCount=len(required), inventoryExhaustive=coverage['rosterClosed'],
              regionChecks=[row['id'] for row in coverage['entries'] if row['scope']=='REGION_CHECK'],
              secondVideo=coverage['secondVideo'], unresolvedSilhouettes=unresolved, originalVisualAccepted=False,
              scope='Static source/12-pose/reward binding audit; not Android runtime or original visual verification')
if args.output:
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(report,ensure_ascii=False,indent=2))
if args.require_complete and not complete:
    raise SystemExit('ABEL WHOLE-ROSTER COMPLETION BLOCKED: missing species, source frames and visual acceptance')
