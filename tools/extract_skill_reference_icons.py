"""Crop only visible source icons; map by exact job/name, never by guessed resemblance."""
import json,hashlib
from pathlib import Path
from PIL import Image
import numpy as np
root=Path(__file__).resolve().parents[1]
import argparse
parser=argparse.ArgumentParser();parser.add_argument('--source-dir',type=Path,required=True);args=parser.parse_args()
sources=args.source_dir
catalog=json.loads((root/'app/src/main/assets/skills/catalog.json').read_text())
records={ (x['job'],x['name']):x['id'] for x in catalog }
specs=[
('05-Screenshot_20260929_233857_NAVER.jpg','도적','상자트랩해체 연막탄터뜨리기 찔러휘비기 더블어택 품뒤져보기 함정해체 센스 두번찌르기 습격 소매치기 함정파기 적갑옷해체 암살격'.split()),
('06-Screenshot_20260929_233816_NAVER.jpg','성직자','디프라바 리치마나 쿠라네라 디소루마 소모니아 호르라마 이모탈 엑스쿠라노 디네츄라 엑스쿠라네라 리베라토 코마디아 디노센스 텔포라 리플렉토 디네츄라'.split()),
('07-Screenshot_20260929_233811_NAVER.jpg','성직자','쿠로토 쿠로 디렌토 아지토 벨라르모 쿠라노 디바르도 쿠러스 디베노모 에나르마 베누스티 쿠라노소 디나르콜리 디데프레카 쿠라누스 홀리볼트 일루메나 쿠랄툼 실드 콜라마 수페라벨라르모 로카메아 리치멘스 수페라쿠라노'.split()),
('08-Screenshot_20260929_233847_NAVER.jpg','도적','자물쇠열기 센스몬스터 표창날리기'.split()),
('09-Screenshot_20260929_233801_NAVER.jpg','마법사','마레노 아지토 렌토 쿠로토 테라미코 아듀로 플라모 콘푸지오 수페라마레나 수페라테라미카 수페라아듀라 수페라플라미카 베노미 바르도 마레누스 테라미쿠스 아듀로스 플라무스 나르콜리 엑스마레나 엑스테라미카 엑스아듀라 엑스플라미카 로카테오'.split())]
specs += [
('10-Screenshot_20260929_233754_NAVER-1-.jpg','무도가','쿠로토 일루메나 디베노모 경신공 흡정신공 쿠랄툼 철포삼 장풍 쿠라노토 금강불괴 구양신공 다라밀공'.split()),
('11-Screenshot_20260929_233743_NAVER.jpg',['전사']+['도적']*5,'쿠로토 쿠로토 수페라동엑스투 라이트닝무브 하이드 센스'.split()),
('12-Screenshot_20260930_052204_NAVER.jpg','무도가','단각 이형환위 양의신권 통배권'.split()),
('13-Screenshot_20260930_052212_NAVER.jpg','무도가','붕각 백보신권 일음지 선풍각 발경 소수신공 달마신공 반탄신공'.split())]
icons=[];audit=[];seen=set()
for filename,job,names in specs:
 p=sources/filename;im=Image.open(p).convert('RGB');a=np.array(im);h,w=a.shape[:2]
 b=a[:,:int(w*.115)].astype(float);mask=(b.max(2)-b.min(2)>35)&(b.min(2)<200);ys=np.where(mask.sum(1)>int(w*.015))[0]
 groups=np.split(ys,np.where(np.diff(ys)>4)[0]+1);boxes=[]
 for g in groups:
  if len(g)<13:continue
  xs=np.where(mask[g[0]:g[-1]+1].sum(0)>len(g)*.5)[0]
  if len(xs)>5:boxes.append([int(xs[0]),int(g[0]),int(xs[-1])+1,int(g[-1])+1])
 if filename.startswith('06-'):boxes[4:6]=[[100,943,256,1111]]
 if filename.startswith('12-'):boxes[1:3]=[[323,401,461,547]]
 assert len(names)==len(boxes),(filename,len(names),len(boxes))
 for row_index,(name,box) in enumerate(zip(names,boxes)):
  row_job=job[row_index] if isinstance(job,list) else job
  ident=records.get((row_job,name));row=dict(source=filename,source_sha256=hashlib.sha256(p.read_bytes()).hexdigest(),job=row_job,source_name=name,crop=box,skill_id=ident)
  if not ident:row['status']='UNMAPPED_EXACT_NAME_ONLY'
  elif ident in seen:row['status']='DUPLICATE_NOT_REGISTERED'
  else:
   seen.add(ident);row['status']='REGISTERED_SOURCE_CROP';row['atlas_index']=len(icons);icons.append(im.crop(box).resize((40,40),Image.Resampling.LANCZOS))
  audit.append(row)
cols=10;atlas=Image.new('RGB',(cols*40,((len(icons)+cols-1)//cols)*40),(32,23,18))
for i,im in enumerate(icons):atlas.paste(im,((i%cols)*40,(i//cols)*40))
out=root/'app/src/main/assets/skills';atlas.save(out/'source_icons.png')
(out/'source_icons.json').write_text(json.dumps(dict(cell=40,columns=cols,provenance='User-provided Naver screenshots; V reference crops, not verified official originals',icons={x['skill_id']:x['atlas_index'] for x in audit if x['status']=='REGISTERED_SOURCE_CROP'},audit=audit),ensure_ascii=False,indent=2)+'\n')
print('Registered exact mappings',len(icons),'unmapped',sum(x['status']=='UNMAPPED_EXACT_NAME_ONLY' for x in audit))
