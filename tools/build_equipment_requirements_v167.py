from pathlib import Path
from html.parser import HTMLParser
import json,re
r=Path(__file__).resolve().parents[1]
j=r/'app/src/main/java/com/projectdark/mobile'
class Text(HTMLParser):
 def __init__(self):super().__init__();self.parts=[]
 def handle_data(self,s):self.parts.append(s)
cat=json.load(open(r/'master/source/equipment/identity_v128/catalog.json'));names={k:v['name'] for k,v in cat['existing'].items()};names.update({x['itemId']:x['name'] for x in cat['additions']});receipts=[];baseNames={}
for num,job in [(244,'MAGE'),(268,'MARTIAL_ARTIST'),(269,'WARRIOR'),(270,'ROGUE')]:
 path=r/f'master/source/equipment/functions_v129/nexon_{num}.html'
 if not path.exists():path=r/f'master/source/equipment/nexon_{num}.html'
 p=Text();p.feed(path.read_text());t='\n'.join(p.parts);t=t[t.index('일반 아이템(Set 1)'):t.index('특수 아이템(Set 2)')]
 for m in re.finditer(r'([^\n:]+?)\s*:\s*Ac\s*(-\d+),\s*레벨\s*(\d+)\s*이상',t,re.I):
  for name in m[1].split('/'):
   name=name.strip();baseNames[name]=(job,int(m[3]),int(m[2]),str(path.relative_to(r)),m[0].strip())
for id,name in names.items():
 base=name;variant=False
 for prefix in ['수오미','밀레스','뤼케시온','루어스','오렌']:
  if name.startswith(prefix) and name[len(prefix):] in baseNames:base=name[len(prefix):];variant=True;break
 if base not in baseNames:continue
 job,level,ac,path,excerpt=baseNames[base];receipts.append(dict(itemId=id,name=name,baseName=base,job=job,level=level,ac=ac,variantAdapted=variant,source=path,excerpt=excerpt))
lines=['package com.projectdark.mobile;','/** Exact historical class-armour joins; town palette options are explicitly ADAPTED. */','final class SourceEquipmentRequirements {',' static void install(RpgProgressionState r){']
for x in receipts:lines.append(f'  r.installSourceJob("{x["itemId"]}","{x["job"]}",{x["level"]},{x["ac"]},{str(x["variantAdapted"]).lower()});')
lines+=[' }',' private SourceEquipmentRequirements(){}','}'];(j/'SourceEquipmentRequirements.java').write_text('\n'.join(lines)+'\n')
(r/'master/changes/V167_EQUIPMENT_JOB_RECEIPTS.json').write_text(json.dumps({'status':'ACCEPTED','policy':'Exact-name historical class armour joins. Town-named palette variants share only base AC/class/level as ADAPTED, never original special effects. No original bytes overwritten. Existing options/levels take precedence.','rows':receipts},ensure_ascii=False,indent=2)+'\n')
print('SOURCE_REQUIREMENTS_V167',len(receipts))
