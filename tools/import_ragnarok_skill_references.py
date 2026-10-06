from pathlib import Path
from PIL import Image,ImageFilter
from scipy.ndimage import map_coordinates,label,binary_dilation
import numpy as np
import json,hashlib

def project(root,output,manifest):
    source=root/"master/source/skill_fx/user_ragnarok_20261002"
    proof=json.loads((source/"provenance.json").read_text())
    sources=proof["sources"]
    for row in sources:
        assert hashlib.sha256((root/row["path"]).read_bytes()).hexdigest()==row["sha256"]
    spec=[('04-1000057951.jpg',[(466,635),(535,673),(421,719),(545,768)],66),('03-1000057953.jpg',[(499,656),(566,692),(452,739),(563,790)],66),('02-1000057955.jpg',[(491,656),(559,692),(447,739),(561,795)],66),('01-1000057957.jpg',[(489,647),(558,683),(442,732),(558,787)],66)]
    side=144; yy,xx=np.mgrid[:side,:side];dx=xx-72;dy=yy-72;rr=np.sqrt(dx*dx+dy*dy)
    fits=[[(463.54,628.82,63.77),(542.24,772.76,63.3)],[(496.6,655.93,63.6),(563.14,789.45,63.37)],[(492.3,655.28,64.87),(560.75,793.54,64.79)],[(487.46,645.83,65.95),(557.31,785.17,65.72)]]
    results=[]
    for index,(name,centres,radius) in enumerate(spec):
     rgb0=np.array(Image.open(root/sources[index]['path']).convert('RGB'),dtype=float);r,g,b=rgb0[:,:,0],rgb0[:,:,1],rgb0[:,:,2];ui=np.zeros(r.shape,bool)
     green=(g>r*1.35)&(g>b*1.2)&(g>90);labels,n=label(green)
     for k in range(1,n+1):
      y,x=np.where(labels==k)
      if len(x)>12 and x.max()-x.min()>14 and y.max()-y.min()<12:
       x0,x1,y0,y1=x.min(),x.max(),y.min(),y.max();ui[max(0,y0-4):y1+5,max(0,x0-10):x1+20]=True
     cyan=(g>r*1.1)&(b>r*1.12)&(r>130)&(g>165)&(b>165);cyan[:550]=False;cyan[900:]=False
     labels,n=label(binary_dilation(cyan,structure=np.ones((3,5))))
     for k in range(1,n+1):
      y,x=np.where(labels==k)
      if len(x)>10 and x.max()-x.min()>8 and y.max()-y.min()<25:ui[max(0,y.min()-2):y.max()+3,max(0,x.min()-2):x.max()+3]=True
     centres=[tuple(fits[index][0][:2]),centres[1],centres[2],tuple(fits[index][1][:2])]
     frames=[];valids=[];alphas=[]
     for cx,cy,viewRadius in fits[index]:
      px=cx+dx*viewRadius/68;py=cy+dy*viewRadius/68
      rgb=np.stack([map_coordinates(rgb0[:,:,ch],[py,px],order=1,mode='nearest') for ch in range(3)],axis=2)
      valid=(rr<71)&(map_coordinates(ui.astype(float),[py,px],order=0,mode='nearest')<.5)
      for ox,oy in centres+[((centres[0][0]+centres[3][0])/2,(centres[0][1]+centres[3][1])/2)]:
       if (ox,oy)!=(cx,cy):valid &= (px-ox)**2+(py-oy)**2>(radius+6)**2
      r,g,b=rgb[:,:,0],rgb[:,:,1],rgb[:,:,2]
      gold=np.clip((r-135)/65,0,1)*np.clip((g-95)/65,0,1)*np.clip((r-b-12)/35,0,1)*np.clip((g-r*.69)/20,0,1)
      white=np.clip((np.min(rgb,axis=2)-185)/40,0,1)*np.clip((65-(np.max(rgb,axis=2)-np.min(rgb,axis=2)))/30,0,1)*(b<=r+5)
      alpha=np.maximum(gold,white)*np.clip((70-rr)/1.5,0,1);alpha[~valid]=0
      frames.append(rgb);valids.append(valid);alphas.append(alpha)
     # Each point uses the most legible clean view of the same source phase.
     stack=np.stack(alphas);which=np.where((dy<0)&((dx<0)|(dy<-45)),0,1);clean=np.stack(valids);other=1-which;which=np.where(np.take_along_axis(clean,which[None],axis=0)[0],which,other);alpha=np.take_along_axis(stack,which[None],axis=0)[0]
     fg=np.take_along_axis(np.stack(frames),which[None,:,:,None],axis=0)[0].astype('uint8')
     # The observed six-point star and inner rings have 180-degree symmetry.
     # Repair the UI-occluded upper core from the clean lower source core; outer rune text is never mirrored.
     my=np.clip(144-yy,0,143);mx=np.clip(144-xx,0,143);repair=(rr<45)&(dy<0)
     alpha[repair]=alpha[my,mx][repair];fg[repair]=fg[my,mx][repair]
     if index==0:
      # Trace measured source ring radii through HP-bar gaps (no runes generated).
      radial=np.floor(rr).astype(int);profile=np.zeros(73)
      for radius_bin in range(73):
       exposed=(radial==radius_bin)&(np.max(valids,axis=0)>0)&(np.abs(dy)>12)
       if np.any(exposed):profile[radius_bin]=np.quantile(alpha[exposed],.80)
      peaks=[lo+int(np.argmax(profile[lo:hi])) for lo,hi in [(40,48),(51,56),(59,64),(65,70)]]
      alpha=np.maximum.reduce([profile[r]*np.exp(-.5*((rr-r)/.65)**2) for r in peaks]);fg[:]=[250,222,145]
     field=np.zeros((side,side,4),dtype='uint8')
     if index>=2:
      field[:,:,:3]=[111,38,152];field[:,:,3]=np.round(np.clip((61.5-rr)/2,0,1)*(78 if index==2 else 66))
     lines=np.dstack((fg,np.round(alpha*255))).astype('uint8');lines[alpha==0]=0
     frame=Image.alpha_composite(Image.fromarray(field),Image.fromarray(lines)).resize((432,432),Image.Resampling.LANCZOS)
     frame.save(source/('enhanced_'+str(index)+'.png'));results.append(frame)
     print(name,'clean domain coverage',np.mean(np.max(valids,axis=0)[rr<68]),'source gold pixels',int(np.sum(alpha>.1)))

    # Crossfades are project-authored, never labelled original animation timing.
    def mix(a,b,t):
        a=np.asarray(a,dtype=float)/255;b=np.asarray(b,dtype=float)/255
        alpha=a[:,:,3]*(1-t)+b[:,:,3]*t
        prem=a[:,:,:3]*a[:,:,3,None]*(1-t)+b[:,:,:3]*b[:,:,3,None]*t
        rgba=np.dstack((prem/np.maximum(alpha[:,:,None],1e-8),alpha))
        return Image.fromarray(np.round(np.clip(rgba,0,1)*255).astype('uint8'))
    tiles=[results[0]];times=[80]
    for a,b in zip(results,results[1:]):
        for t in (1/3,2/3,1):tiles.append(mix(a,b,t));times.append(50)
    tiles.append(results[-1]);times.append(100)
    empty=Image.new('RGBA',results[-1].size)
    for t in (.35,.7,1):tiles.append(mix(results[-1],empty,t));times.append(60)
    # 2x runtime atlas keeps capture detail; 3x standalone GIF is the review export.
    atlas=Image.new('RGBA',(288*4,288*((len(tiles)+3)//4)))
    counts=[]
    for i,im in enumerate(tiles):
        tile=im.resize((288,288),Image.Resampling.LANCZOS);atlas.paste(tile,(i%4*288,i//4*288))
        counts.append(int(np.count_nonzero(np.asarray(tile)[:,:,3])))
    target=output/'ragnarok_user_v81.png';atlas.quantize(colors=256,method=Image.Quantize.FASTOCTREE).save(target,optimize=True)
    previews=[]
    for im in tiles:
        bg=Image.new('RGBA',im.size,(15,17,23,255));previews.append(Image.alpha_composite(bg,im).convert('RGB'))
    previews[0].save(source/'RAGNAROK_original_capture.gif',save_all=True,append_images=previews[1:],duration=times[:-1]+[times[-1]+300],loop=0,optimize=False,disposal=2)
    channel=dict(path='classic/ragnarok_user_v81.png',width=288,height=288,columns=4,durationsMs=times,frameIndices=list(range(len(tiles))),
        pivotX=144,pivotY=144,scale=.30,blend='NORMAL',filterBitmap=True,anchor='RECIPIENT',registration='VISUAL_CENTER',
        sourceTiming=proof['sourceTiming'],sourceGif=sources[0]['path'],sourceSha256=sources[0]['sha256'],sourceFrames=sources,
        atlasSha256=hashlib.sha256(target.read_bytes()).hexdigest(),opaquePixelCounts=counts,review=proof)
    manifest['skills']['SK_마법사_037']=dict(name='라그나로크',article='USER_PROVIDED_SCREENSHOTS_20261002',mapping='USER_LABELLED_STILL_SOURCE_DECLARED_REPAIR_PROJECT_TIMING',channels={'RECIPIENT_CONTACT':channel})
