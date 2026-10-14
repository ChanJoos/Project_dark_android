"""Require five successful exact-source independent native hunting simulations before APK assembly."""
import json, os
from pathlib import Path
root=Path('app/build/reports/abel-v153')
source=os.environ['PROJECT_DARK_SOURCE_SHA']
jobs={'WARRIOR','ROGUE','MAGE','CLERIC','MARTIAL_ARTIST'}
files=list((root/'jobs').rglob('five-jobs-level70.json'))
assert len(files)==5, files
joined={}
for p in files:
    rows=json.loads(p.read_text())
    assert len(rows)==1, p
    job,row=next(iter(rows.items()))
    assert job in jobs and job not in joined, (p,job)
    assert row.get('status')!='FAILED' and row['sourceCommit']==source, (p,row)
    assert row['level']>=70 and row['kills']>=117 and row['proficiency']>0 and row['gold']>0, (p,row)
    joined[job]=row
assert set(joined)==jobs
(root/'five-jobs-level70.json').write_text(json.dumps(joined,ensure_ascii=False,indent=2)+'\n')
print('FIVE_NORMAL_JOBS_NATIVE_HUNTING_VERIFIED',source,{j:(r['level'],r['kills'],r['gold']) for j,r in joined.items()})
