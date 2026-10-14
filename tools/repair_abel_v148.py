"""Extract complete observed bench feet; never generate missing sprite pixels.
Run after the V147 reconstruction. RGB remains exact to supplied frame crops.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json,hashlib,argparse,numpy as np
ap=argparse.ArgumentParser();ap.add_argument('--frames',type=Path,required=True);args=ap.parse_args()
root=Path(__file__).resolve().parents[1];folder=root/'app/src/main/assets/abel-video-v147';data=json.loads((folder/'layout.json').read_text());specs={a['id']:a for a in data['assets']}
items=[
('bench_front_down',46,[188,74,250,150],[(4,29),(16,20),(18,10),(22,8),(52,24),(56,23),(58,25),(55,37),(52,47),(52,56),(48,58),(47,54),(41,61),(35,62),(34,59),(40,54),(39,52),(12,40),(12,46),(4,47),(4,44),(8,41),(5,38),(4,37)],[38,63]),
('bench_front_up',54,[124,187,193,265],[(7,37),(9,34),(12,37),(15,42),(38,24),(44,20),(46,18),(49,19),(51,34),(56,37),(61,38),(63,42),(62,49),(57,52),(59,56),(60,57),(55,58),(54,54),(30,66),(30,68),(32,69),(27,70),(25,67),(19,64),(18,66),(15,66),(15,62),(13,58),(12,47)],[28,71]),
('bench_back_up',46,[172,302,237,374],[(0,35),(5,34),(7,38),(11,39),(13,32),(17,24),(20,25),(47,11),(49,9),(52,9),(54,11),(51,23),(49,27),(46,34),(42,38),(41,41),(36,42),(32,44),(29,46),(24,49),(19,51),(15,53),(12,57),(8,57),(6,52),(4,50),(0,48)],[10,58]),
('bench_back_down',45,[532,36,597,101],[(6,6),(9,5),(12,7),(39,23),(41,23),(43,26),(44,33),(48,34),(52,30),(55,30),(59,35),(59,42),(59,44),(61,45),(60,49),(55,50),(53,47),(50,46),(44,44),(41,43),(34,39),(28,37),(21,35),(15,31),(11,26),(10,22),(8,21),(7,15)],[57,51])]
for name,n,box,poly,anchor in items:
 old=specs[name].copy();a=np.array(Image.open(args.frames/f'{n:03d}.png').crop(box).convert('RGBA'));mask=Image.new('L',(a.shape[1],a.shape[0]));ImageDraw.Draw(mask).polygon(poly,fill=255);rgb=a[:,:,:3].astype(float)
 # Remove only bright neutral/blue floor within silhouette; unlike V147 retain grey iron.
 m=np.array(mask);m[((rgb.mean(2)>165)&(rgb.max(2)-rgb.min(2)<55))|((rgb.mean(2)>120)&(rgb[:,:,2]>rgb[:,:,0]*1.22))]=0
 a[:,:,3]=m;sole=anchor[1];out=np.zeros((sole+4,a.shape[1],4),np.uint8);out[:sole]=a[:sole];Image.fromarray(out).save(folder/(name+'.png'))
 specs[name].update(box=box,polygon=poly,anchor=anchor,size=[a.shape[1],sole+4],sourceFrame=n,status='SOURCE_VISIBLE_FULL_FEET',anchorContract='FULL_SOURCE_FOOT_CONTACT_PLUS_4PX_GUTTER',sha256=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest())
 for scene in data['scenes'].values():
  for prop in scene['props']:
   if prop['asset']==name:
    prop['sourceWorld']=[prop['sourceWorld'][i]-old['anchor'][i]+box[i]-old['box'][i]+anchor[i] for i in range(2)];prop['anchor']=anchor;prop['nativeSize']=specs[name]['size']
 # Record native support region below seat, to prevent the old vague dark-pixel test.
 specs[name]['footRegion']=[0,50 if name=='bench_front_down' else 60 if name=='bench_front_up' else 50 if name=='bench_back_up' else 43,a.shape[1],sole]
from shutil import copyfile
copyfile(folder/'bench_front_up.png',folder/'bench_pool.png')
pool=specs['bench_pool'];pool.update(anchor=specs['bench_front_up']['anchor'],size=specs['bench_front_up']['size'],sha256=specs['bench_front_up']['sha256'],footRegion=specs['bench_front_up']['footRegion'],status='SOURCE_CLEAR_FULL_FEET_SAME_FACING_REUSE')
for scene in data['scenes'].values():
 for prop in scene['props']:
  if prop['asset']=='bench_pool':prop['anchor']=pool['anchor'];prop['nativeSize']=pool['size']
for dst in [folder/'layout.json',root/'master/source/abel_video_v147/layout.json']:dst.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
# Change only registered bench constructor rows; preserve all other observed geometry.
p=root/'app/src/main/java/com/projectdark/mobile/world/AbelVideoLayout.java';s=p.read_text();import re
for key,method in [('main','mainProps'),('east','eastProps')]:
 start=s.index('public static List<AbelTownGeometry.Prop> '+method);end=s.index('return Collections.unmodifiableList(p);',start);segment=s[start:end];props=iter([a for a in data['scenes'][key]['props'] if a['asset'].startswith('bench_')])
 def sub(m):
  a=next(props);nums=a['sourceWorld']+a['anchor']+a['nativeSize'];return 'p.add(new AbelTownGeometry.Prop("'+a['asset']+'",'+','.join(f'{v:.3f}f' for v in nums)+'));'
 segment=re.sub(r'p.add\(new AbelTownGeometry.Prop\("bench_[^;]+;',sub,segment);s=s[:start]+segment+s[end:]
p.write_text(s)
print('Complete observed bench feet registered; source layouts remain partial')
