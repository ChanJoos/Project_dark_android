"""Reproducible asset normalization for the V150 complete silhouettes.
Requires the three retained built-in image_gen outputs and original video frame 041.
No generation, video upload, or invented ORIGINAL classification is performed here.
Run this tool then unify_abel_v149.py; source observation scenes stay unchanged.
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageOps
import argparse,hashlib,json,numpy as np
p=argparse.ArgumentParser();p.add_argument('--generated-dir',type=Path,required=True);p.add_argument('--source-frames',type=Path,required=True);args=p.parse_args()
r=Path(__file__).resolve().parents[1];folder=r/'app/src/main/assets/abel-video-v147';d=json.loads((folder/'layout.json').read_text());receipts=[]
specs=[('building_two_storey_complete','exec-283f9937-9e11-46ce-bf7d-90aa6f17342b.png',224,(430,1040),[(245,958),(634,1116),(1240,858),(848,708)]),('building_awning_corner_complete','exec-6744b317-ebb9-4ce9-87b0-f14b25bcaae9.png',320,(1300,943),[(87,705),(340,838),(640,736),(1210,990),(1450,885),(910,631),(640,515),(330,615)]),('sailing_ship_complete','exec-8cf1b7a7-ee93-4a34-a5a0-ee3bd82e9880.png',390,None,None)]
for name,file,width,anchor,polygon in specs:
 src=args.generated_dir/file;im=Image.open(src).convert('RGBA');a=np.array(im)[:,:,3];ys,xs=np.where(a>=128);box=(int(xs.min()),int(ys.min()),int(xs.max()+1),int(ys.max()+1));scale=width/(box[2]-box[0]);height=round((box[3]-box[1])*scale);im=im.crop(box).resize((width,height),Image.Resampling.LANCZOS);out=Image.new('RGBA',(width+8,height+8));out.paste(im,(4,4));out.save(folder/(name+'.png'))
 def point(point):return [round((point[0]-box[0])*scale+4,3),round((point[1]-box[1])*scale+4,3)]
 spec=dict(id=name,size=list(out.size),anchor=point(anchor) if anchor else [width/2+4,height],status='ADAPTED_GENERATED_COMPLETE_SILHOUETTE',sourceParent=name.removesuffix('_complete'),generation='built-in image_gen reference-preserving completion; missing pixels reconstructed, not recovered original art',originalGenerationFile=file,originalGenerationSha256=hashlib.sha256(src.read_bytes()).hexdigest(),sha256=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest())
 if polygon:spec['groundPolygon']=[point(pt) for pt in polygon]
 receipts.append(spec)
name='rowing_boat_complete';source=args.source_frames/'041.png';a=np.array(Image.open(source).crop((430,225,603,345)).convert('RGBA'));rgb=a[:,:,:3].astype(float);a[:,:,3]=((rgb[:,:,0]>rgb[:,:,2]*1.35)&(rgb[:,:,1]>rgb[:,:,2]*1.08)).astype('uint8')*255;out=Image.new('RGBA',(181,128));out.paste(Image.fromarray(a),(4,4));out.save(folder/(name+'.png'));receipts.append(dict(id=name,size=[181,128],anchor=[90,119],sourceFrame=41,sourceFrameSha256=hashlib.sha256(source.read_bytes()).hexdigest(),box=[430,225,603,345],status='SOURCE_VISIBLE_COMPLETE_SILHOUETTE',sourceParent='rowing_boat',sha256=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest()))
# Complete original basin including its lower wall; trace source edges and jets instead
# of retaining the surrounding rectangular pool screenshot.
def source_cut(name,frame,box,polygons,anchor):
 source=args.source_frames/f'{frame:03d}.png';im=Image.open(source).crop(box).convert('RGBA');mask=Image.new('L',im.size);draw=ImageDraw.Draw(mask)
 for poly in polygons:draw.polygon([(x-box[0],y-box[1]) for x,y in poly],fill=255)
 a=np.array(im);rgb=a[:,:,:3].astype(float);keep=np.array(mask)>0
 if name=='signpost_complete':
  yy,xx=np.indices(keep.shape);keep&=((rgb[:,:,0]>rgb[:,:,1]*1.30)&(rgb[:,:,0]>rgb[:,:,2]*1.50))|((yy>=22)&(xx>=14)&(xx<=22)&(rgb.mean(2)<125))
 if name=='fountain_complete':
  yy,xx=np.indices(keep.shape);keep&=~((yy+box[1]<260)&(rgb[:,:,2]>rgb[:,:,0]*1.35)&(rgb[:,:,1]>rgb[:,:,0]*1.2)&(rgb[:,:,0]<110))
 if name=='signpost_complete':
  visited=set();groups=[]
  for y,x in zip(*np.where(keep)):
   if (y,x) in visited:continue
   group={(y,x)};queue=[(y,x)];visited.add((y,x))
   while queue:
    yy,xx=queue.pop()
    for dy,dx in [(-1,-1),(-1,0),(-1,1),(0,-1),(0,1),(1,-1),(1,0),(1,1)]:
     ny,nx=yy+dy,xx+dx
     if 0<=ny<keep.shape[0] and 0<=nx<keep.shape[1] and keep[ny,nx] and (ny,nx) not in visited:visited.add((ny,nx));group.add((ny,nx));queue.append((ny,nx))
   groups.append(group)
  keep[:]=False
  for y,x in max(groups,key=len):keep[y,x]=True
 a[:,:,3]=keep.astype('uint8')*255;im=Image.fromarray(a);out=Image.new('RGBA',(im.width+8,im.height+8));out.paste(im,(4,4));out.save(folder/(name+'.png'));receipts.append(dict(id=name,size=list(out.size),anchor=[anchor[0]+4,anchor[1]+4],sourceFrame=frame,sourceFrameSha256=hashlib.sha256(source.read_bytes()).hexdigest(),box=list(box),sourcePolygons=polygons,status='SOURCE_VISIBLE_COMPLETE_SILHOUETTE',sha256=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest()));return out
source_cut('fountain_complete',28,(140,180,258,333),[
 [(146,276),(151,265),(169,253),(190,249),(216,252),(239,268),(251,282),(250,301),(242,313),(222,323),(193,328),(171,323),(154,312),(146,299)],
 [(195,233),(186,245),(177,248),(174,283),(180,300),(214,300),(224,281),(220,254),(205,242),(203,232)],
 [(197,192),(199,191),(200,217),(207,230),(205,243),(196,246),(191,241),(193,228),(196,218)],
 [(165,239),(168,239),(169,286),(176,302),(166,301)],
 [(227,241),(230,241),(231,285),(226,302),(221,301)],
 [(158,251),(161,251),(163,279),(158,285)],
 [(238,255),(240,254),(240,286),(236,286)]
 ],(58,148))
source_cut('signpost_complete',28,(546,337,585,381),[
 [(0+546,10+337),(17+546,8+337),(18+546,0+337),(24+546,1+337),(26+546,9+337),(38+546,11+337),(38+546,20+337),(25+546,23+337),(17+546,26+337),(0+546,25+337)],
 [(562,358),(568,358),(567,380),(560,380)]
 ],(17,42))
# One proper source railing interval, background water/floor excluded by its existing
# traced alpha. Horizontal mirroring is a deterministic native atlas transformation.
base=Image.open(folder/'pool_rail_down.png').convert('RGBA')
for name,image,anchor in [('rail_down_clean',base,(0,29)),('rail_up_clean',ImageOps.mirror(base),(0,45))]:
 out=Image.new('RGBA',(image.width+8,image.height+8));out.paste(image,(4,4));out.save(folder/(name+'.png'));receipts.append(dict(id=name,size=list(out.size),anchor=[anchor[0]+4,anchor[1]+4],sourceParent='pool_rail_down',status='ADAPTED_SOURCE_RAIL_INTERVAL_WITHOUT_SCREENSHOT_BACKGROUND',sha256=hashlib.sha256((folder/(name+'.png')).read_bytes()).hexdigest()))
# An observed hanging flag is a fixture, not a freestanding cropped picture. Register
# it on a complete source lamp pole; no leaves/floor or adjacent bench are retained.
source=args.source_frames/'014.png';flag=Image.open(source).crop((299,12,333,113)).convert('RGBA');mask=Image.new('L',flag.size);draw=ImageDraw.Draw(mask);draw.polygon([(29,13),(32,14),(29,37),(25,62),(22,78),(20,83),(17,76),(14,61),(10,44),(10,31),(18,22)],fill=255);a=np.array(flag);rgb=a[:,:,:3].astype(float);red=(rgb[:,:,0]>rgb[:,:,1]*1.18)&(rgb[:,:,0]>rgb[:,:,2]*1.13)&(np.array(mask)>0);cloth=np.zeros(red.shape,np.uint8)
for y in range(red.shape[0]):
 xs=np.where(red[y])[0]
 if len(xs):cloth[y,max(0,int(xs.min())-1):min(red.shape[1],int(xs.max())+2)]=255
rod=Image.new('L',flag.size);ImageDraw.Draw(rod).line([(0,41),(29,13)],fill=255,width=2);cloth[(np.array(rod)>0)&(rgb.mean(2)<150)]=255;a[:,:,3]=cloth;flag=Image.fromarray(a);lamp=Image.open(folder/'lamp_main.png').convert('RGBA');out=Image.new('RGBA',(58,132));out.alpha_composite(lamp,(18,4));out.alpha_composite(flag,(0,15));padded=Image.new('RGBA',(66,140));padded.alpha_composite(out,(4,4));out=padded;out.save(folder/'lamp_banner_complete.png');lampSpec=next(a for a in d['assets'] if a['id']=='lamp_main');receipts.append(dict(id='lamp_banner_complete',size=list(out.size),anchor=[22+lampSpec['anchor'][0],8+lampSpec['anchor'][1]],sourceParents=['banner','lamp_main'],sourceFrame=14,status='ADAPTED_WHOLE_SOURCE_LAMP_WITH_OBSERVED_FLAG',sha256=hashlib.sha256((folder/'lamp_banner_complete.png').read_bytes()).hexdigest()))
ids={a['id'] for a in receipts};d['assets']=[a for a in d['assets'] if a['id'] not in ids]+receipts
for path in [folder/'layout.json',r/'master/source/abel_video_v147/layout.json']:path.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
lines=['package com.projectdark.mobile.world;','/** Registered native ground silhouettes, not sprite roof rectangles. */','final class AbelObjectFootprints {','static float[] vertices(String asset){switch(asset){']
for a in receipts:
 if 'groundPolygon' in a:lines.append('case '+json.dumps(a['id'])+':return new float[]{'+','.join(str(v)+'f' for pt in a['groundPolygon'] for v in pt)+'};')
lines+=['default:return null;}}','private AbelObjectFootprints(){}','}'];(r/'app/src/main/java/com/projectdark/mobile/world/AbelObjectFootprints.java').write_text('\n'.join(lines)+'\n')
p=r/'master/assets/abel_v150';p.mkdir(parents=True,exist_ok=True);(p/'provenance.json').write_text(json.dumps(dict(assets=receipts,scope='Three silhouettes have generated repairs; source tree and small boat are original visible pixels. No original full Abel topology claim.'),ensure_ascii=False,indent=2)+'\n');print('V150 normalized',len(receipts),'complete assets')
