"""Recover visible source FX beyond obsolete rectangular masks; never invent occluded pixels."""
from pathlib import Path
import json,hashlib,math
from PIL import Image
import numpy as np
from scipy import ndimage
R=Path(__file__).resolve().parents[1];A=R/'app/src/main/assets/skill-presentation'
def save(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
# Visible full flare envelopes in the retained labelled source GIFs, excluding UI.
regions={'007':[52,35,125,112],'014':[52,35,125,112],'021':[44,28,140,133],'022':[44,28,140,133],'023':[58,40,125,118],'028':[61,30,124,118],'029':[57,25,129,98],'013':[20,36,105,114],'016':[15,30,115,114]}
f=A/'rogue/manifest.json';m=json.loads(f.read_text())
for n,rect in regions.items():
 sid='SK_도적_'+n;ch=m['skills'][sid]['channels']['RECIPIENT_CONTACT'];im=Image.open(R/ch['sourceGif']);frames=[]
 for i in range(im.n_frames):im.seek(i);frames.append(np.array(im.convert('RGB'),dtype=float))
 base=np.min(frames,axis=0);h,w=base.shape[:2];valid=np.zeros((h,w),bool);x0,y0,x1,y1=rect;valid[y0:y1,x0:x1]=True;tiles=[];counts=[];left=[]
 for c in frames:
  diff=np.maximum(c-base,0);em=np.clip(diff/np.maximum(255-base,1),0,1);alpha=np.max(em,2);r,g,b=c[:,:,0],c[:,:,1],c[:,:,2];white=(np.min(c,2)>170)&(np.min(diff,2)>24)
  if n in ('007','014','022'):colour=(r>g+30)&(b>g+30)&(b>100)
  elif n=='021':colour=(g>r+25)&(b>r+25)&(g>80)&(b>100)
  elif n=='029':colour=(np.min(c,2)>125)&(r>=g)&(g>=b)
  elif n=='028':colour=(np.min(c,2)>145)&(np.max(c,2)-np.min(c,2)<55)
  else:colour=(r>170)&(g>120)&(r>b+25)&(g>b+15)
  mask=valid&(np.max(diff,2)>23)&(colour|white)
  if n in ('013','016'):
   # Only actual white glow can be distinguished from golden source clothes here.
   mask[43:94,43:66]&=white[43:94,43:66]
  # Keep fine original particles; discard newly exposed isolated actor/UI flecks.
  labels,count=ndimage.label(mask);oldregion=ch['review']['region'];old=np.zeros_like(mask);old[oldregion[1]:oldregion[3],oldregion[0]:oldregion[2]]=True
  for label in range(1,count+1):
   component=labels==label
   if component.sum()<15 and not (component&old).any():mask[component]=False
  alpha[~mask]=0;fg=np.clip(em/np.maximum(alpha[:,:,None],.001)*255,0,255);tile=np.dstack((fg,np.round(alpha*255))).astype('uint8');tile[~mask]=0;tiles.append(tile);counts.append(int(mask.sum()));oldx=ch['review']['region'][0];left.append(int(mask[:,:oldx].sum()))
 atlas=Image.new('RGBA',(w*4,h*math.ceil(len(tiles)/4)))
 for i,t in enumerate(tiles):atlas.paste(Image.fromarray(t),(i%4*w,i//4*h))
 atlas.save(A/ch['path']);ch.update(atlasSha256=sha(A/ch['path']),opaquePixelCounts=counts,displayRepair={'revision':'V233_SOURCE_CHROMA_FULL_ENVELOPE','region':rect,'restoredLeftPixels':left,'noRectangularActorHole':True,'limitations':'Visible source pixels only; truly occluded pixels remain unresolved.'})
# Stab uses the same retained source effect family; no fallback caster slash.
m['skills']['SK_도적_001']={'channels':m['skills']['SK_도적_007']['channels'],'displayPolicy':'USER_V233_STAB_SAME_DAGGER_CONTACT'}
save(f,m)
# Regenerate only the two rogue captured spheres; old actor polygons cut out real emission.
f=A/'captured/manifest.json';m=json.loads(f.read_text())
for sid in ('SK_도적_020','SK_도적_027'):
 ch=m['skills'][sid];im=Image.open(R/ch['sourceGif']);frames=[]
 for i in range(im.n_frames):im.seek(i);frames.append(np.array(im.convert('RGB'),dtype=float))
 base=frames[-1];h,w=base.shape[:2];tiles=[];counts=[]
 for i in ch['frameIndices']:
  c=frames[i];diff=c-base;r,g,b=c[:,:,0],c[:,:,1],c[:,:,2];bright=(np.min(c,2)>170)&(np.min(diff,2)>24);colour=(r-b>14)&(r>140)&(g>50);mask=(colour|bright)&(np.max(np.abs(diff),2)>22)
  # Cyan dotted hidden source actor is not part of the warm/white sphere.
  em=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1);alpha=np.max(em,2);alpha[~mask]=0;fg=np.clip(em/np.maximum(alpha[:,:,None],.0001)*255,0,255);rgba=np.dstack((fg,np.round(alpha*255))).astype('uint8');rgba[~mask]=0;tiles.append(rgba);counts.append(int(mask.sum()))
 atlas=Image.new('RGBA',(w*4,h*math.ceil(len(tiles)/4)))
 for i,t in enumerate(tiles):atlas.paste(Image.fromarray(t),(i%4*w,i//4*h))
 atlas.save(A/ch['path']);ch.update(scale=.52,atlasSha256=sha(A/ch['path']),opaquePixelCounts=counts,displayRepair='V233_WARM_WHITE_SOURCE_EMISSION_WITHOUT_ACTOR_POLYGON_OR_TOP_CUT')
save(f,m)
# Retain Hide's source glyph. Discard only the capture's white lower margin and cyan corners.
f=A/'rogue/references.json';refs=json.loads(f.read_text());refs['rows']=[r for r in refs['rows'] if r['id']!='SK_도적_011'];meta=json.loads((R/'app/src/main/assets/skills/source_icons.json').read_text());index=meta['icons']['SK_도적_011'];box=[index%10*40,index//10*40,index%10*40+40,index//10*40+35];im=Image.open(R/'app/src/main/assets/skills/source_icons.png').convert('RGBA').crop(box).resize((40,40),Image.Resampling.NEAREST);data=np.array(im);rgb=data[:,:,:3].astype(float);palette=np.array(Image.open(A/'rogue/icons/d3e88d3b9c127da5.png').convert('RGB')).reshape(-1,3);palette=palette[np.argsort(palette@np.array([.299,.587,.114]))];lum=rgb@np.array([.299,.587,.114]);lr=palette@np.array([.299,.587,.114]);data[:,:,:3]=np.round(np.stack([np.interp(lum,lr,palette[:,c]) for c in range(3)],axis=2)).astype('uint8');path='icons/'+hashlib.sha256('SK_도적_011'.encode()).hexdigest()[:16]+'.png';Image.fromarray(data).save(A/'rogue'/path);refs['rows'].append({'id':'SK_도적_011','iconAssetPath':path,'sourcePath':'app/src/main/assets/skills/source_icons.png','sourceSha256':sha(R/'app/src/main/assets/skills/source_icons.png'),'crop':box,'displayTransform':'V233_HIDE_REMOVE_WHITE_CAPTURE_MARGIN_AND_BROWN_PALETTE'});save(f,refs)
print('V233 full visible rogue flare envelopes,source sphere emission/.52,Hide margin only')
