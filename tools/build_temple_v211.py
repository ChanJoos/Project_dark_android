"""Decode retained original room tiles/NPCs into one immutable atlas; verify every source byte."""
from pathlib import Path
import zipfile,json,hashlib,numpy as np
from PIL import Image
from horror_original_formats import palette,decompress,table,mpf,render_frame
ROOT=Path(__file__).resolve().parents[1];src=ROOT/'master/source/temple_v211';out=ROOT/'app/build/generated/temple-v211/temple-v211';out.mkdir(parents=True,exist_ok=True)
ref=json.loads((src/'reference.json').read_text())
with zipfile.ZipFile(src/'original_sources.zip') as z:entries={n:z.read(n) for n in z.namelist()}
for n,d in ref['files'].items():assert hashlib.sha256(entries[n]).hexdigest()==d,n
class A:
 def __init__(self,p):self.entries={n[len(p)+1:]:b for n,b in entries.items() if n.startswith(p+'/')}
 def __getitem__(self,n):return self.entries[n]
ia=A('ia');seo=A('seo');ft=table(ia,'stc');bt=table(seo,'mpt');images={};anchors={}
for n,b in entries.items():
 if n.endswith('.hpf'):
  i=int(n[6:11]);data=decompress(b)[8:];images[f'stc{i}']=Image.fromarray(palette(ia[f'stc{ft.get(i+1,0)%1000:04}.pal'])[np.frombuffer(data,np.uint8).reshape(-1,28)])
 elif n.startswith('seo/bg'):
  i=int(n[6:-4]);a=palette(seo[f'mpt{bt.get(i+1,0)%1000:04}.pal'])[np.frombuffer(b,np.uint8).reshape(27,56)].copy();a[:,:,3]=255
  for y in range(27):
   margin=abs(13-y)*2;a[y,:margin,3]=0
   if margin:a[y,-margin:,3]=0
  images[f'bg{i}']=Image.fromarray(a)
for i in [61,84]:
 m=mpf(entries[f'hades/mns{i:03}.mpf']);idx=m['stand'] if m['standcount'] else m['walk'];f=m['frames'][idx];images[f'npc{i}']=render_frame(m,palette(entries[f"hades/mns{m['palette']:03}.pal"]),idx);anchors[f'npc{i}']=[f['cx']-f['left'],f['cy']-f['top']]
# Deterministic shelf pack, source pixels unmodified.
width=1024;x=y=row=0;regions={}
for name,im in sorted(images.items()):
 if x+im.width>width:x=0;y+=row+2;row=0
 regions[name]=dict(x=x,y=y,width=im.width,height=im.height,anchor=anchors.get(name));x+=im.width+2;row=max(row,im.height)
height=y+row;atlas=Image.new('RGBA',(width,height))
for name,im in images.items():r=regions[name];atlas.alpha_composite(im,(r['x'],r['y']))
atlas.save(out/'atlas.png');(out/'atlas.json').write_text(json.dumps(dict(regions=regions,width=width,height=height),sort_keys=True));print('TEMPLE_ORIGINAL_ATLAS',len(images),width,height,width*height*4)
# Generated Java grid uses original cell bytes and source-collision mask.
java=['package com.projectdark.mobile.world;','import java.util.*;','public final class TempleOriginalData {','public static final class Grid { public final int sourceId,width,height,npc,npcU,npcV,spawnU,spawnV,exitU,exitV;private final byte[] cells,walk;','Grid(int id,int w,int h,int npc,int nu,int nv,int su,int sv,int eu,int ev,String c,String b){sourceId=id;width=w;height=h;this.npc=npc;npcU=nu;npcV=nv;spawnU=su;spawnV=sv;exitU=eu;exitV=ev;cells=Base64.getDecoder().decode(c);walk=Base64.getDecoder().decode(b);}','public int value(int u,int v,int layer){int p=((v*width+u)*3+layer)*2;return(cells[p]&255)|((cells[p+1]&255)<<8);}','public boolean walkable(int u,int v){if(u<0||v<0||u>=width||v>=height)return false;int i=v*width+u;return(walk[i/8]&(1<<(i%8)))!=0;}}','private static final Map<String,Grid> MAPS=new HashMap<>();static {']
for m in ref['maps']:
 nums=[m['sourceId'],m['width'],m['height'],m['npc'],*m['npcTile'],*m['spawn'],*m['exit']];java.append('MAPS.put("'+m['id']+'",new Grid('+','.join(map(str,nums))+',"'+m['cells']+'","'+m['walk']+'"));')
java+=['}','public static Grid get(String id){return MAPS.get(id);}','private TempleOriginalData(){}','}'];dest=ROOT/'app/src/main/java/com/projectdark/mobile/world/TempleOriginalData.java';text='\n'.join(java)+'\n'
if '--check' in __import__('sys').argv:assert dest.read_text()==text,'Temple grid drift'
else:dest.write_text(text)
