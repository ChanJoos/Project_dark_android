"""Scoped user corrections; all accepted martial and other icon bytes stay unchanged."""
from pathlib import Path
from PIL import Image
import json,csv,hashlib
import numpy as np
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets';S=R/'master/source/rogue_v232';O=A/'skill-presentation/rogue-v232';O.mkdir(exist_ok=True)
p=json.loads((R/'master/changes/V232_ROGUE_REPAIR.json').read_text());assert p['status']=='ACCEPTED'
def save(f,d):f.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
d=json.loads((A/'skill-presentation/catalog.json').read_text());d['profiles']['ROGUE_STAB']=p['motions']
for row in d['skills']:
 if row['motion']=='ROGUE_STAB':row['poseEvidence']='USER_V232_GIF_MATCH_C8_C13_PROJECT_TIMING'
save(A/'skill-presentation/catalog.json',d)
with (R/'docs/SKILL_PRESENTATION_MAPPING.csv').open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=list(d['skills'][0]),lineterminator='\n');w.writeheader();w.writerows(d['skills'])
f=R/'docs/SKILL_ACTION_DECISIONS.csv'
with f.open() as src:reader=csv.DictReader(src);fields=reader.fieldnames;rules=list(reader)
for r in rules:r.update(p['rules'].get(r['id'],{}))
with f.open('w',newline='') as out:w=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');w.writeheader();w.writerows(rules)
keys=['id','name','mode','pattern','reach','minReach','mechanic','hits','contact','spatialEvidence','note','kind'];lines=['package com.projectdark.mobile;','','/** Generated from the reviewed per-ID decisions; do not infer rules from skill names. */','final class SkillActionData {','  static final String[][] ROWS={']
for r in rules:lines.append('    {'+', '.join(json.dumps(str(r[k]),ensure_ascii=False) for k in keys)+'},')
lines+=[' };','}'];(R/'app/src/main/java/com/projectdark/mobile/SkillActionData.java').write_text('\n'.join(lines)+'\n')
# Recolor only Slash from its immutable existing source using already accepted double-stab palette.
f=A/'skill-presentation/rogue/references.json';refs=json.loads(f.read_text());by={r['id']:r for r in refs['rows']};ref=by['SK_도적_014'];palette=np.asarray(Image.open(R/ref['sourcePath']).convert('RGBA').crop(ref['crop']))[:,:,:3].reshape(-1,3);palette=palette[np.argsort(palette@np.array([.299,.587,.114]))];row=by['SK_도적_022'];im=np.array(Image.open(R/row['sourcePath']).convert('RGBA').crop(row['crop']));lum=im[:,:,:3]@np.array([.299,.587,.114]);lumref=palette@np.array([.299,.587,.114]);im[:,:,:3]=np.round(np.stack([np.interp(lum,lumref,palette[:,c]) for c in range(3)],axis=2)).astype('uint8');Image.fromarray(im).save(A/'skill-presentation/rogue'/row['iconAssetPath']);row['displayTransform']='V232_DOUBLE_STAB_BROWN_PALETTE_ONLY';save(f,refs)
# Actual captured frame/header/scrollbar/Close; replace only stale field text and mouse pointer with clean source texture/border.
frame=Image.open(S/'sense-frame-capture.png').convert('RGB');
strip=frame.crop((60,250,730,310))
for y in (90,150,210):frame.paste(strip.crop((0,0,670,min(60,240-y))),(60,y))
frame.paste(frame.crop((200,397,250,423)),(600,397));frame.save(O/'sense-frame.png');meta={'path':'skill-presentation/rogue-v232/sense-frame.png','sha256':hashlib.sha256((O/'sense-frame.png').read_bytes()).hexdigest(),'source':'master/source/rogue_v232/sense-frame-capture.png','fieldTextReplacement':{'source':[60,250,730,310],'destinations':[[60,90],[60,150],[60,210]],'lastHeight':30},'cursorReplacement':{'source':[200,397,250,423],'destination':[600,397]},'originalFrameAndHeaderRetained':True};save(O/'sense-frame.json',meta)
print('V232 exact-source rogue bent-knee pose,Slash-only palette,original Sense frame and2tile rule')
