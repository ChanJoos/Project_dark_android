"""Recover only user-requested icons/descriptions; never admit acquisition columns."""
from pathlib import Path
import json,re,hashlib,urllib.request,urllib.parse,concurrent.futures
from lxml import html
from PIL import Image
R=Path(__file__).resolve().parents[1]
S=R/'master/source/mage_v235';S.mkdir(exist_ok=True)
raw=Path('/tmp/mage429943.json')
if not raw.exists():
 u='https://article.cafe.naver.com/gw/v4/cafes/13434008/articles/429943?useCafeId=true'
 raw.write_bytes(urllib.request.urlopen(urllib.request.Request(u,headers={'User-Agent':'Mozilla/5.0'}),timeout=30).read())
article=json.loads(raw.read_text())['result']['article']
root=html.fromstring(article['contentHtml'])
cat={r['name']:r['id'] for r in json.loads((R/'app/src/main/assets/skills/catalog.json').read_text()) if r['job']=='마법사'}
rows=[]
for table,t in enumerate(root.xpath('//table')):
 trs=t.xpath('.//tr');cells=[r.xpath('./td') for r in trs]
 names=[' '.join(''.join(p.itertext()).split()) for p in cells[2][0].xpath('.//p')];names=[n for n in names if n]
 urls=cells[1][0].xpath('.//img/@src')
 desc=[' '.join(''.join(p.itertext()).split()) for p in cells[1][2].xpath('.//p')];desc=[d for d in desc if d]
 stage=' '.join(cells[0][0].text_content().split())
 assert len(names)==len(urls),(table,names,urls)
 for index,(name,url) in enumerate(zip(names,urls)):
  sourceName=name;resolution=None
  if name=='스킬명':
   name={13:'데프레코',15:'소루마'}[table]
   filename=urllib.parse.unquote(urllib.parse.urlparse(url).path.rsplit('/',1)[-1])
   assert filename==name+'.png';assert name in root.text_content()
   resolution='SOURCE_PLACEHOLDER_RESOLVED_BY_ICON_FILENAME_AND_NEARBY_PROSE'
  assert name in cat,name
  description=desc[index] if len(names)>1 and len(desc)==len(names) and all('속성' in d for d in desc) else '\n'.join(desc)
  rows.append(dict(id=cat[name],name=name,sourceName=sourceName,sourceTable=table,sourceIconIndex=index,stage=stage,description=description,iconUrl=url,labelResolution=resolution))
assert len(rows)==54 and len({r['id'] for r in rows})==54
def fetch(row):
 p=S/(hashlib.sha256(row['iconUrl'].encode()).hexdigest()[:16]+'.png')
 if not p.exists():
  with urllib.request.urlopen(urllib.request.Request(row['iconUrl'],headers={'User-Agent':'Mozilla/5.0'}),timeout=30) as response:p.write_bytes(response.read())
 im=Image.open(p);assert im.width==40 and 35<=im.height<=41
 return dict(row,iconPath=str(p.relative_to(R)),iconSha256=hashlib.sha256(p.read_bytes()).hexdigest(),width=im.width,height=im.height)
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:rows=list(pool.map(fetch,rows))
doc=dict(revision='V235_MAGE_GUIDE_ICONS_DESCRIPTIONS_ONLY',article='https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/429943',title=article['subject'],evidence='FAN_GUIDE_2021_UPDATED2023_USER_SELECTED',observedThrough='Public browser DOM and documented public gateway; table content matched',excluded=['acquisition gold','acquisition materials','prerequisite mastery','recommendations to delete skills'],rows=rows)
(S/'guide.json').write_text(json.dumps(doc,ensure_ascii=False,indent=2)+'\n')
print('Admitted54 exact source icons+descriptions; acquisition columns omitted')
