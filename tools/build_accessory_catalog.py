"""Deterministic historical equipment catalog: preserve decoded source pixels and field receipts."""
from pathlib import Path
from html.parser import HTMLParser
from urllib.parse import urlparse
import json,re,hashlib,zipfile
from PIL import Image
from build_equipment_source_review import display_name,CATEGORIES
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'master/source/equipment/atwiki_20261009';OUT=ROOT/'master/assets/equipment/accessories_20261009';APP=ROOT/'app/src/main/assets/equipment-icons'
class Tables(HTMLParser):
 def __init__(self):super().__init__();self.rows=[];self.row=None;self.cell=None;self.img=None
 def handle_starttag(self,t,a):
  if t=='tr':self.row=[]
  if t in ('td','th'):self.cell='';self.img=None
  if t=='img' and self.cell is not None:self.img=dict(a).get('src')
  if t=='br' and self.cell is not None:self.cell+=' '
 def handle_data(self,d):
  if self.cell is not None:self.cell+=d
 def handle_endtag(self,t):
  if t in ('td','th') and self.cell is not None:
   if self.row is not None:self.row.append((self.cell.strip(),self.img))
   self.cell=None
  if t=='tr' and self.row:self.rows.append(self.row);self.row=None

def norm(s):return re.sub(r'\s','',s)
def jstr(s):return json.dumps(s,ensure_ascii=False)
def main():
 OUT.mkdir(parents=True,exist_ok=True)
 with zipfile.ZipFile(SRC/'original_sources.zip') as z:
  for name in z.namelist():
   assert Path(name).name==name
   if not (SRC/name).exists():(SRC/name).write_bytes(z.read(name))
 entries=json.loads((SRC/'manifest.json').read_text());byalias={norm(a):e for e in entries for a in e['sourceAliases']};catalog=[];icons=json.loads((APP/'manifest.json').read_text());used=set()
 known={'가죽각반':'IT_LEGGING_LEATHER','가죽장갑':'IT_GLOVE_LEATHER','쌍은귀걸이':'IT_EARRING_DOUBLE_SILVER','홍옥반지':'IT_RING_REDJADE','고루반지':'IT_RING_GORU','실버아쿠아링':'IT_RING_SILVERAQUA'}
 for e in entries:
  source=SRC/e['path'];assert hashlib.sha256(source.read_bytes()).hexdigest()==e['sha256'];Image.open(source).convert('RGBA').save(OUT/(Path(e['path']).stem+'.png'))
 for page in sorted(SRC.glob('page_*.html')):
  if page.stem=='page_217':continue
  parser=Tables();parser.feed(page.read_text());headers=[]
  for row in parser.rows:
   texts=[c[0] for c in row]
   if texts[0]=='画像':headers=texts;continue
   if len(texts)!=len(headers) or len(texts)<6:continue
   label=texts[1];e=byalias.get(norm(label))
   if not e:continue
   fields=dict(zip(headers,texts));name=display_name(dict(e,labelSource=label,sourceAliases=[label])).replace('(원문)','');slot=CATEGORIES[e['categorySourceLabel']][1].replace('장갑·팔찌','장갑')
   if name=='?':name='이름 미확인 신발'
   stats={};ac=fields.get('AC','')
   if re.fullmatch(r'[+-]?\d+',ac):stats['AC']=int(ac)
   comment=fields.get('コメント','');unparsed=comment
   for m in re.finditer(r'(HIT|DAM|DMG|HP|MP|STR|INT|WIS|CON|DEX|AC|MDEF|ALL)\s*([+−-]?\s*\d+)(?![\d％%])',comment,re.IGNORECASE):
    key={'DMG':'DAM','MDEF':'MAGIC_DEFENSE'}.get(m[1].upper(),m[1].upper());value=int(m[2].replace(' ','').replace('−','-'));unparsed=unparsed.replace(m[0],'')
    if key=='ALL':stats.update({k:value for k in ['STR','INT','WIS','CON','DEX']})
    else:stats[key]=value
   element=None
   for pattern,val in [('火','화염'),('風','바람'),('地','대지'),('水','바다'),('海','바다'),('木','숲'),('金の','금속'),('暗黒','암흑'),('生命','생명')]:
    if label.startswith(pattern):element=val;break
   ident=known.get(name)
   if slot=='목걸이' and '진주목걸이' in name and element in ('화염','바람','대지','바다'):ident='IT_NECK_'+{'화염':'FIRE','바람':'WIND','대지':'EARTH','바다':'WATER'}[element]+'_PEARL'
   if slot=='벨트' and '가죽벨트' in name and element in ('화염','바람','대지','바다'):ident='IT_BELT_'+{'화염':'FIRE','바람':'WIND','대지':'EARTH','바다':'WATER'}[element]+'_LEATHER'
   ident=ident or 'IT_SOURCE_'+Path(e['path']).stem.upper()+('_'+hashlib.sha256(norm(label).encode()).hexdigest()[:8].upper())
   if ident in used:continue
   used.add(ident);level=fields.get('Lv','');level=int(level) if level.isdigit() else None
   asset=Path(e['path']).stem+'.png';raw=OUT/asset;(APP/asset).write_bytes(raw.read_bytes())
   record=dict(itemId=ident,name=name,slot=slot,sourceMinLevel=level,sourceJobRestriction='MARTIAL_ARTIST' if '武道家専用' in comment else 'UNSPECIFIED',attackElement=element if slot=='목걸이' else None,defenseElement=element if slot=='벨트' else None,stats=stats,sourceFields=fields,unprojectedComment=unparsed,sourceLabel=label,sourceUrl=e['sourceUrl'],sourceImage=e['path'],asset=asset,sourceEra='HISTORICAL_FAN_TABLE',nameAuthority='SOURCE_KOREAN_OR_REVIEW_TRANSLATION',evidence='FAN',testRequirementPolicy='ADAPTED_USER_ALL_EQUIPMENT_LV1')
   catalog.append(record)
   icons['items'][ident]=dict(itemId=ident,asset=asset,sourcePath=str((SRC/e['path']).relative_to(ROOT)),sourceSha256=e['sha256'],assetSha256=hashlib.sha256(raw.read_bytes()).hexdigest(),crop=[0,0,*Image.open(raw).size],projection='DECODED_FIRST_FRAME_EXACT_RGBA',sourceArchive=str((SRC/'original_sources.zip').relative_to(ROOT)),sourceMember=e['path'],evidence='HISTORICAL_KOREAN_GAME_FAN_TABLE',sourceUrl=e['sourceUrl'])
 # All image-only receipts must remain playable even when their row cannot be parsed.
 assert {e['path'] for e in entries}=={r['sourceImage'] for r in catalog},'Missing image row'
 old=ROOT/'master/source/equipment/accessories_20261009';previous=json.loads((old/'image_manifest.json').read_text())
 extras=[(0,'골든플레이트각반 [2012]','각반',99,{'AC':-9,'HIT':3,'DAM':15,'HP':100,'MP':100},None,None),(2,'숲의잼스톤목걸이','목걸이',70,{'HIT':5,'DAM':10},'숲',None),(None,'흑요석워리어목걸이(수)','목걸이',99,{'AC':-1,'HIT':5,'DAM':8},'바다',None),(5,'금벨트 [2012]','벨트',11,{'WIS':1,'CON':1},None,None),(7,'다크그레이벨트(수)+1','벨트',99,{'MP':50,'WIS':1,'CON':1},None,'바다'),(9,'쥬얼십자가목걸이FX','목걸이',1,{'HIT':10,'DAM':10},None,None)]
 for ix,(imageIndex,name,slot,level,stats,atk,defense) in enumerate(extras):
  ident='IT_SOURCE_MINIMOB_'+str(ix);record=dict(itemId=ident,name=name,slot=slot,sourceMinLevel=level,sourceJobRestriction='WARRIOR' if ix==2 else 'UNSPECIFIED',attackElement=atk,defenseElement=defense,stats=stats,sourceUrl='https://minimob.tistory.com/99',sourceEra='2012_TOOLTIP',evidence='V',testRequirementPolicy='ADAPTED_USER_ALL_EQUIPMENT_LV1',unprojectedComment='Durability, expiry, trade restrictions retained in tooltip; not simulated',iconStatus='PENDING_SOURCE_BYTES' if imageIndex is None else 'CONNECTED')
  if imageIndex is not None:
   e=previous[imageIndex];source=old/e['path'];asset='minimob_'+str(imageIndex)+'.png';Image.open(source).convert('RGBA').save(OUT/asset);(APP/asset).write_bytes((OUT/asset).read_bytes());record['asset']=asset
   icons['items'][ident]=dict(itemId=ident,asset=asset,sourcePath=str(source.relative_to(ROOT)),sourceSha256=e['sha256'],assetSha256=hashlib.sha256((OUT/asset).read_bytes()).hexdigest(),crop=[0,0,*Image.open(source).size],projection='DECODED_FIRST_FRAME_EXACT_RGBA',evidence='LABELLED_2012_CAPTURE')
  catalog.append(record)
 icons['revision']='SOURCE_ACCESSORY_CATALOG_V125';(APP/'manifest.json').write_text(json.dumps(icons,ensure_ascii=False,indent=2)+'\n')
 (ROOT/'master/canonical/Accessory_Catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
 (OUT/'manifest.json').write_text(json.dumps(dict(sourceImages=152,projectedImages=157,logicalItems=len(catalog),policy='Exact first decoded frame, no repaint or recolor; historical fields only, unspecified fields remain pending',items=catalog),ensure_ascii=False,indent=2)+'\n')
 code=['package com.projectdark.mobile;','import java.util.*;','/** Generated by build_accessory_catalog.py from retained historical field receipts. */','final class SourceAccessoryCatalog {',' static void register(RpgProgressionState r){']
 for item in catalog:
  level='null' if item['sourceMinLevel'] is None else str(item['sourceMinLevel']);mods=','.join(jstr(k)+','+str(v) for k,v in item['stats'].items());jobs='Collections.singleton("WARRIOR")' if item['sourceJobRestriction']=='WARRIOR' else 'Collections.singleton("MARTIAL_ARTIST")' if item['sourceJobRestriction']=='MARTIAL_ARTIST' else 'Collections.emptySet()'
  code.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join([jstr(item['itemId']),jstr(item['name']),jstr(item['slot']),level,jobs,'false' if item['sourceJobRestriction']=='UNSPECIFIED' else 'true','null' if item['attackElement'] is None else jstr(item['attackElement']),'null' if item['defenseElement'] is None else jstr(item['defenseElement']),'stats('+mods+')','RpgProgressionState.Evidence.'+item['evidence']])+'));')
 code+=[' }',' private static Map<String,Integer> stats(Object... kv){Map<String,Integer> m=new LinkedHashMap<>();for(int i=0;i<kv.length;i+=2)m.put((String)kv[i],(Integer)kv[i+1]);return m;}','}']
 (ROOT/'app/src/main/java/com/projectdark/mobile/SourceAccessoryCatalog.java').write_text('\n'.join(code)+'\n');print('ACCESSORY_CATALOG',len(catalog),'items / 157 pixel-preserved icons')
if __name__=='__main__':main()
