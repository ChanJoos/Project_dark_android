"""Recover approved V154 object pixels after scratch loss. No new image generation.
Retained approved RGB atlas recovers completed silhouettes (corner/shop thumbnail
resampling is explicitly ADAPTED). Hall/pergola/grass recover original video pixels.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import numpy as np,json,hashlib,cv2,shutil,argparse,re
r=Path(__file__).resolve().parents[1];p=argparse.ArgumentParser();p.add_argument('--frames',type=Path,required=True);arg=p.parse_args();ab=r/'app/src/main/assets/abel-video-v147';ru=r/'app/src/main/assets/rucesion-v154';ru.mkdir(exist_ok=True);ad=json.loads((ab/'layout.json').read_text());old={a['id']:a for a in ad['assets']};spec={}
def poly(size,points):
 im=Image.new('L',size);ImageDraw.Draw(im).polygon(points,fill=255);return np.array(im)
def save(folder,n,im,anchor,status,**kw):
 a=np.array(im.convert('RGBA'));a[a[:,:,3]==0,:3]=0;Image.fromarray(a).save(folder/(n+'.png'));s=dict(id=n,size=list(im.size),anchor=anchor,status=status,sha256=hashlib.sha256((folder/(n+'.png')).read_bytes()).hexdigest(),**kw)
 if folder==ru:spec[n]=s
 else:
  ad['assets']=[a for a in ad['assets'] if a['id']!=n]+[s]
  for scene in ad['scenes'].values():
   for pr in scene.get('props',[]):
    if pr['asset']==n:pr.update(anchor=anchor,nativeSize=list(im.size),evidence=status)
 return s
atlas=Image.open(r/'master/assets/town_v154/approved_atlas.png').convert('RGBA')
for folder,n,box,size,anchor in [
 (ab,'building_two_storey_complete',(6,40,238,286),(232,246),old['building_two_storey_complete']['anchor']),
 (ab,'building_awning_corner_complete',(306,40,594,238),(328,226),old['building_awning_corner_complete']['anchor']),
 (ru,'building_yellow_shop',(906,40,1194,306),(298,275),[246,263]),
 (ru,'building_blue_house',(1206,40,1396,256),(190,216),[51,204]),
 (ru,'rowing_boat',(306,490,503,621),(197,131),[99,126]),
 (ru,'tree_broadleaf',(906,490,988,615),(82,125),[48,116]),
 (ab,'tree_broadleaf',(606,490,678,609),(72,119),[39,112])]:
 im=atlas.crop(box);a=np.array(im);bg=np.all(a[:,:,:3]==[70,82,91],axis=2);a[:,:,3]=np.uint8(~bg)*255;im=Image.fromarray(a)
 if im.size!=size:im=im.resize(size,Image.Resampling.NEAREST)
 a=np.array(im);a[:3,:,3]=0;a[-3:,:,3]=0;a[:,:3,3]=0;a[:,-3:,3]=0;kw={}
 if n.startswith('building_') and folder==ab:kw['groundPolygon']=old[n]['groundPolygon']
 if n=='building_yellow_shop':kw['groundPolygon']=[[25,181],[204,268],[294,230],[115,143]]
 if n=='building_blue_house':kw['groundPolygon']=[[15,174],[91,212],[180,173],[103,137]]
 save(folder,n,Image.fromarray(a),anchor,'RECOVERED_APPROVED_ADAPTED_SPRITE_ATLAS',atlasBox=list(box),**kw)
# Existing Abel rowboat ground anchor/extent are stable, new transparent wood mask.
im=Image.open(ru/'rowing_boat.png').resize(tuple(old['rowing_boat_complete']['size']),Image.Resampling.NEAREST);save(ab,'rowing_boat_complete',im,old['rowing_boat_complete']['anchor'],'ADAPTED_SOURCE_TRACED_ROWBOAT_REUSE')
# Runtime Java and metadata use exactly the same padded tree anchors.
for file in ['AbelUnifiedLayout.java','AbelVideoLayout.java']:
 p=r/'app/src/main/java/com/projectdark/mobile/world'/file;s=p.read_text();pat=r'(new AbelTownGeometry.Prop\("tree_broadleaf",)([^)]*)\)'
 def fix(m):
  v=m.group(2).split(',');v[2:]=['39f','112f','72f','119f'];return m.group(1)+','.join(v)+')'
 p.write_text(re.sub(pat,fix,s))
cams={1:[-30,559],5:[33.996,667.211],29:[-477.9997,741.302],33:[-708.0386,626.081],37:[-946.0157,506.853],41:[-1178.0196,390.633],45:[-486.0168,400.726],49:[-276.0131,393.688],53:[131.9934,562.011],57:[131.9918,674.211],61:[-100.0051,790.415],65:[-268.0137,902.641],69:[-673.9972,68.9975],73:[-673.9973,69.0002],77:[-674.0055,69.0024],89:[73.9983,428.7574],93:[-688.0264,299.5093],97:[-688.021,299.513],101:[-688.023,299.503],105:[-108.0148,373.5923],109:[-1.9967,264.4149]}
out=np.zeros((2200,5000,3),np.uint8);best=np.zeros((2200,5000),np.float32);yy,xx=np.indices((480,640));weight=np.minimum.reduce([xx-19,617-xx,yy-4,405-yy]).astype(np.float32);weight[(yy<60)&(xx<245)]=0;weight[140:260,270:350]=0;weight[360:,280:360]=0
for n,cam in cams.items():
 im=np.array(Image.open(arg.frames/f'{n:03d}.png').convert('RGB'));x,y=np.rint(np.array(cam)+[2300,300]).astype(int);m=weight>best[y:y+480,x:x+640];out[y:y+480,x:x+640][m]=im[m];best[y:y+480,x:x+640][m]=weight[m]
hall=Image.fromarray(out).crop((2216,923,2635,1327)).convert('RGBA');a=np.array(hall);a[:,:,3]=poly(hall.size,[(210,5),(65,72),(59,83),(36,94),(17,97),(9,111),(7,143),(10,160),(17,173),(17,263),(96,306),(210,247),(318,306),(404,264),(404,173),(413,159),(415,131),(411,111),(398,97),(373,89),(365,74)]);a[235:296,317:326,:3]=a[235:296,307:316,:3];im=Image.new('RGBA',(427,412));im.alpha_composite(Image.fromarray(a),(4,4));save(ru,'building_blue_hall',im,[69,284],'SOURCE_REGISTERED_RGB_WITH_ADAPTED_LAMP_OCCLUSION_PATCH',groundPolygon=[[21,267],[100,310],[214,251],[322,310],[408,268],[214,169]])
perg=hall.crop((88,175,323,399));a=np.array(perg);rgb=a[:,:,:3];m=poly(perg.size,[(0,67),(118,11),(231,67),(123,117)]);keep=(m>0)&(rgb.mean(2)>185)&(np.max(rgb,2)-np.min(rgb,2)<70)
for x,y,h in [(25,78,75),(69,100,35),(115,65,29),(179,96,52),(227,77,78)]:keep[y:y+h,x:x+3]=True
keep|=poly(perg.size,[(30,154),(115,117),(197,156),(197,183),(121,220),(28,184)])>0;a[:,:,3]=np.uint8(keep)*255;im=Image.new('RGBA',(243,232));im.alpha_composite(Image.fromarray(a),(4,4));save(ru,'pergola_garden',im,[126,224],'SOURCE_RGB_SEPARATE_PERGOLA_GARDEN')
for n in ['tile_white','tile_white_aged','tile_brick_rosette','tile_blue_water','tile_rock_paving','coping_up_edge','coping_down_edge','garden_curb_up_edge','garden_curb_down_edge','lamp_main','bench_front_up','bench_front_down','rail_up_clean','rail_down_clean','signpost_complete','flowerbed_orange']:
 save(ru,n,Image.open(ab/(n+'.png')),old[n]['anchor'],'REUSED_ISOLATED_SOURCE_MODULE')
im=Image.open(arg.frames/'073.png').crop((440,45,496,73)).convert('RGBA');a=np.array(im);a[:,:,3]=poly(im.size,[(28,0),(55,14),(28,27),(0,14)]);save(ru,'tile_sea_water',Image.fromarray(a),[28,14],'SOURCE_VISIBLE_TILE')
im=np.array(Image.open(arg.frames/'061.png').crop((213,169,231,181)).convert('RGBA'));yy,xx=np.indices((28,56));a=im[yy%12,xx%18];a[:,:,3]=np.uint8(abs(xx-28)+2*abs(yy-14)<=29)*255
for folder in [ab,ru]:save(folder,'tile_grass',Image.fromarray(a),[28,14],'ADAPTED_CLEAR_GRASS_MATERIAL_GAP_FREE_DIAMOND')
for p in [ab/'layout.json',r/'master/source/abel_video_v147/layout.json']:p.write_text(json.dumps(ad,ensure_ascii=False,indent=2)+'\n')
(ru/'assets.json').write_text(json.dumps(dict(assets=list(spec.values()),recovery='Original unpublished git/source directory was lost. Completed silhouette pixels recovered from the approved PNG atlas. Native hall/pergola recovered from original video. Not bit-identical to lost alpha/resampled thumbnails.'),indent=2)+'\n');print('Recovered',len(spec),'Rucesion asset families')
