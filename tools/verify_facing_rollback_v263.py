"""Prove a removal-only rollback to the pre-positional facing source; no runtime policy."""
import hashlib,json,re,subprocess
from pathlib import Path
BASE='255a104c69cb09bfc4c760d9c0c98210502bae05'
PARENT='13308ab19df39d841d3f9cf1d92fe9f4009bd0a7'
ROOT=Path(__file__).resolve().parents[1]
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=ROOT)
def method(text,name):
    match=re.search(r'(?m)^  (?:public |private |protected |static |final )*[^\n;=]*\b'+name+r'\([^\n;]*?\)\s*\{',text)
    assert match,name
    start=text.index('{',match.start());depth=1;i=start+1
    while depth:
        if text[i]=='{':depth+=1
        elif text[i]=='}':depth-=1
        i+=1
    return text[match.start():i]
whole=['MonsterAIController','CanonicalActorFacing','CombatController','CanonicalMeleeTileContract','CharacterRenderer','SkillBodyRenderer','PoteForestMonsterShowcase','RuntimeCombatSession','CombatResolver']
checks={}
for name in whole:
    path='app/src/main/java/com/projectdark/mobile/'+name+'.java';raw=(ROOT/path).read_bytes();assert raw==old(path),path
    checks[path]=hashlib.sha256(raw).hexdigest()
p='app/src/main/java/com/projectdark/mobile/GameView.java';now=(ROOT/p).read_text();before=old(p).decode()
# The entire combat-event consumer is restored, including all preexisting skill behavior.
start='  private void tickSkillCombat(float dt){';end='  private void consumeSkillNotice(){'
assert now[now.index(start):now.index(end)]==before[before.index(start):before.index(end)]
for name in ['snapshotAttackFacingTarget','prepareAutomaticFacing','attack','submitLearnedAction','characterDirection','setFacingDirection','applyWorldFacing','faceTarget','tickAutoAttack']:
    assert method(now,name)==method(before,name),name
assert 'recipient.x-state.player().x' not in now
for name in ['BasicPositionDamageV259','MonsterCombatFacingV260']:assert not (ROOT/'app/src/main/java/com/projectdark/mobile'/f'{name}.java').exists()
for name in ['RuntimeState','RuntimeCombatPortAdapter']:
    path='app/src/main/java/com/projectdark/mobile/'+name+'.java';a=(ROOT/path).read_text();b=old(path).decode()
    methods=['beginMonsterAttack','cancelMonsterAttack','resolveMonsterAttack','tickMonsterMovement'] if name=='RuntimeState' else ['lockDirection','facing']
    for key in methods:assert method(a,key)==method(b,key),name+'.'+key
changed=set(subprocess.check_output(['git','diff','--name-only',PARENT,'--','app/src/main'],cwd=ROOT,text=True).splitlines())
assert changed=={'app/src/main/java/com/projectdark/mobile/GameView.java','app/src/main/java/com/projectdark/mobile/MonsterAIController.java'},changed
result=dict(baseline=BASE,parent=PARENT,wholeFiles=checks,gameView='combat event consumer and9 direction/action methods match pre-position source',runtimeDelta=sorted(changed),scope='removal-only rollback; handset video failure root UNKNOWN; no claim that source equality proves phone success')
out=ROOT/'app/build/reports/horror-v179/v263/rollback-source.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
