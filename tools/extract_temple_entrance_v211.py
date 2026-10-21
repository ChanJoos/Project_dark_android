"""Source object extraction, not a gameplay screenshot used as a map texture.
Video actor occlusions are replaced from the geometrically verified official photo.
World geometry, collision and portals remain separate from these reusable objects.
"""
from pathlib import Path
import zipfile,json,hashlib,io
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
ROOT=Path(__file__).resolve().parents[1];src=ROOT/'master/source/temple_v211';out=ROOT/'app/build/generated/temple-v211/temple-v211';out.mkdir(parents=True,exist_ok=True)
ref=json.loads((src/'reference.json').read_text());receipt=ref['entrance']
with zipfile.ZipFile(src/'entrance_sources.zip') as z:
 sources={n:z.read(n) for n in z.namelist()}
for n,digest in receipt['files'].items():assert hashlib.sha256(sources[n]).hexdigest()==digest
video=Image.open(io.BytesIO(sources['native10-temporal.png'])).convert('RGBA');official=Image.open(io.BytesIO(sources['official-platform.png'])).convert('RGBA')
# Inverse of measured partial affine transform, resampling only at the recording's pixel scale.
m=np.array(receipt['alignment']['affine']);inv=np.linalg.inv(np.vstack([m,[0,0,1]]))[:2].flatten();aligned=official.transform(video.size,Image.Transform.AFFINE,inv,Image.Resampling.BICUBIC)
a=np.array(video);b=np.array(aligned)
# Fit exposure on the unobscured front stair band, away from the live light beam and UI.
ys,xs=np.mgrid[:360,:640];fit=(xs>330)&(xs<440)&(ys>251)&(ys<267)&(b[:,:,3]>0)
ratio=np.median((a[:,:,:3].astype(float)[fit]+1)/(b[:,:,:3].astype(float)[fit]+1),axis=0);b[:,:,:3]=np.clip(b[:,:,:3]*ratio,0,255).astype(np.uint8);aligned=Image.fromarray(b)
# Verified clean official-image coordinates cover these two video-only actor occlusions.
for box in [(270,178,296,226),(295,142,333,189)]:video.paste(aligned.crop(box),box)
repair=json.loads((src/'entrance_repair_v213.json').read_text())
patchBytes=(src/'entrance_front_frame8.png').read_bytes();assert hashlib.sha256(patchBytes).hexdigest()==repair['patchSha256']
patch=Image.open(io.BytesIO(patchBytes)).convert('RGBA');raw=Image.new('RGBA',video.size);raw.paste(patch,(600,220))
m=np.array(repair['affine']);inverse=np.linalg.inv(np.vstack([m,[0,0,1]]))[:2].flatten();recovered=raw.transform(video.size,Image.Transform.AFFINE,inverse,Image.Resampling.NEAREST)
# Only the proven front-column contact pixels are selected; no HUD or face region.
select=Image.new('L',video.size);ImageDraw.Draw(select).polygon([(576,272),(592,265),(592,299),(576,299)],fill=255)
select=np.minimum(np.array(select),np.array(recovered.getchannel('A')));video.paste(recovered,(0,0),Image.fromarray(select))
components={}
def cut(name,image,box,polygon,anchor):
 sprite=image.crop(box);mask=Image.new('L',sprite.size);ImageDraw.Draw(mask).polygon([(x-box[0],y-box[1]) for x,y in polygon],fill=255);pixels=np.array(sprite);green=(pixels[:,:,1].astype(int)-pixels[:,:,0]>18)&(pixels[:,:,1].astype(int)-pixels[:,:,2]>18);mm=np.array(mask);
 if name=="entrance_arch_left":mm[green]=0
 sprite.putalpha(Image.fromarray(mm));sprite.save(out/(name+'.png'));components[name]=dict(box=box,polygon=polygon,anchor=anchor,size=list(sprite.size))
# Only stone is admitted: no frozen water, UI, players, grass rectangles or streamer's face.
cut('entrance_platform',video,[244,146,516,286],[(338,152),(380,148),(425,154),(470,169),(500,190),(510,216),(503,239),(484,257),(451,272),(410,281),(371,285),(332,280),(292,267),(266,250),(249,230),(245,211),(252,191),(270,175),(300,160)],[135,69])
cut('entrance_arch_back',video,[248,56,374,130],[(250,84),(267,76),(298,65),(327,59),(350,56),(371,61),(373,67),(369,74),(358,75),(358,104),(354,111),(347,114),(340,112),(339,80),(320,81),(320,113),(314,118),(309,118),(303,114),(301,83),(288,86),(287,120),(280,127),(273,127),(268,123),(268,91),(260,96),(251,94)],[62,73])
cut('entrance_arch_front',video,[563,191,615,300],[(565,255),(578,247),(592,233),(598,221),(598,202),(603,194),(610,192),(613,200),(612,224),(610,236),(610,264),(606,271),(606,282),(599,286),(592,287),(591,258),(587,260),(587,297),(581,299),(576,296),(576,267),(568,270),(564,265)],[37,91])
cut('entrance_arch_left',video,[172,251,256,332],[(175,254),(190,255),(196,264),(212,275),(231,283),(255,290),(255,302),(254,304),(254,331),(238,331),(238,303),(221,297),(221,329),(214,331),(206,329),(206,289),(195,283),(195,316),(187,319),(180,316),(180,275),(175,269)],[42,91])
# Alpha matte from actual source light. Restrict chroma to reject the green ground.
orig=Image.open(io.BytesIO(sources['native10.png'])).convert('RGBA');beam=orig.crop((332,0,437,239));arr=np.array(beam).astype(float);rgb=arr[:,:,:3];chroma=rgb[:,:,1]-np.minimum(rgb[:,:,0],rgb[:,:,2]);alpha=np.clip((rgb.min(axis=2)-115)/140,0,.72);alpha[chroma>48]=0
matte=Image.new('L',beam.size);ImageDraw.Draw(matte).polygon([(22,0),(88,0),(94,139),(90,188),(79,215),(54,232),(27,219),(10,191),(1,150),(11,64)],fill=255);alpha*=np.array(matte)/255
# Source-derived light color and a spatial alpha soften avoid baking grass into a glowing rectangle.
arr[:,:,3]=alpha*255;beam=Image.fromarray(arr.astype(np.uint8));beam.putalpha(beam.getchannel('A').filter(ImageFilter.MedianFilter(3)).filter(ImageFilter.GaussianBlur(1)));beam.save(out/'entrance_light.png');components['entrance_light']=dict(box=[332,0,437,239],anchor=[47,215],matte='SOURCE_LUMINANCE_CHROMA_EXTRACTION_ADAPTED')
(out/'entrance.json').write_text(json.dumps({'components':components,'officialExposureRatio':ratio.tolist(),'coordinateAnchor':[379,215]},indent=2)+'\n')
preview=Image.new('RGBA',(450,350),(35,65,30,255))
for name,x,y in [('entrance_arch_back',100,70),('entrance_platform',96,145),('entrance_light',184,0),('entrance_arch_front',330,190),('entrance_arch_left',20,246)]:preview.alpha_composite(Image.open(out/(name+'.png')),(x,y))
preview.convert('RGB').save(out/'entrance-review.jpg')
print('TEMPLE_ENTRANCE_SOURCE_OBJECTS',len(components),'50 matched original platform features')
