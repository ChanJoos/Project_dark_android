"""Source surface and moving highlight; actor occlusion is interpolated in image space."""
import json, hashlib, math
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from scipy.ndimage import distance_transform_edt, gaussian_filter
from scipy.sparse import coo_matrix
from scipy.sparse.linalg import spsolve

def project(root, output):
    source = root/'master/source/skill_fx/user_defense_20261001'
    meta = json.loads((source/'provenance.json').read_text())
    path = source/'source_frames.png'
    assert hashlib.sha256(path.read_bytes()).hexdigest() == meta['sourceFrameSha256']
    image = Image.open(path).convert('RGB')
    w,h,n = meta['width'],meta['height'],meta['frameCount']
    frames = [np.array(image.crop((i%6*w,i//6*h,i%6*w+w,i//6*h+h)),dtype=np.float32) for i in range(n)]
    # First two samples precede the sphere. Background is registered to the same floor.
    base = np.minimum(frames[0],frames[1])
    yy,xx = np.ogrid[:h,:w]
    radius = ((xx-120)/110)**2+((yy-185)/118)**2
    blocked=Image.new('1',(w,h));d=ImageDraw.Draw(blocked)
    d.rectangle((20,96,190,149),fill=1) # captured name/HP bars
    d.polygon([(68,144),(133,144),(148,192),(147,263),(133,298),(86,298),(73,252)],fill=1)
    d.polygon([(125,161),(218,204),(211,229),(130,214)],fill=1) # moving sword
    d.rectangle((0,120,57,277),fill=1) # foreign left actor
    blocked=np.array(blocked,dtype=bool)
    # Left actor extends beyond its idle silhouette during the recording.
    blocked[:,:155]=True
    blocked[145:280,155:193]=True # moving recipient/weapon beyond the idle mask
    radial=np.sqrt(radius)
    # Retain the observed 2D surface, including the translucent interior.
    # Do not collapse it to a radial profile or erase the centre as in the rejected trace.
    reference=np.median(np.stack(frames[6:48]),axis=0)
    referenceEmission=np.clip(np.maximum(reference-base,0)/np.maximum(255-base,1),0,1)
    domain=radial<1.08
    blue_surface=(reference[:,:,2]>reference[:,:,0]+12)&(reference[:,:,2]>reference[:,:,1]+3)
    surface=referenceEmission.copy()
    surface[~blue_surface]=0
    known=~blocked
    # The visible right arc supplies the left arc hidden by a foreign actor.
    mirror=np.clip(240-np.arange(w),0,w-1)
    reflected=surface[:,mirror]
    donor=known[:,mirror]
    missing=blocked&domain&donor
    surface[missing]=reflected[missing]
    known[missing]=True
    known[~domain]=True
    surface[~domain]=0
    holes=~known
    nearest=distance_transform_edt(holes,return_distances=False,return_indices=True)
    surface[holes]=surface[tuple(nearest[:,holes])]
    # Harmonic interpolation fills only occluded pixels; source pixels stay fixed.
    coords=np.argwhere(holes)
    ids=np.full((h,w),-1,dtype=int);ids[holes]=np.arange(len(coords))
    rows=[];cols=[];values=[];rhs=np.zeros((len(coords),3))
    for k,(py,px) in enumerate(coords):
        rows.append(k);cols.append(k);values.append(4.)
        for ny,nx in [(py-1,px),(py+1,px),(py,px-1),(py,px+1)]:
            if ids[ny,nx]>=0:
                rows.append(k);cols.append(ids[ny,nx]);values.append(-1.)
            else:rhs[k]+=surface[ny,nx]
    if len(coords):
        system=coo_matrix((values,(rows,cols)),shape=(len(coords),len(coords))).tocsr()
        surface[holes]=spsolve(system,rhs)
    traced=gaussian_filter(surface,sigma=(1,1,0))
    atlas = Image.new('RGBA',(w*6,h*math.ceil(n/6)))
    counts=[]
    for i,c in enumerate(frames):
        diff = c-base
        r,g,b = c[:,:,0],c[:,:,1],c[:,:,2]
        blue = (b-r>12)&(b-g>3)&(b>90)
        # Moving white/pink highlight belongs to the outer rim, away from the actors/labels.
        core = (np.min(c,axis=2)>180)&(np.min(diff,axis=2)>35)&(radius>.50)
        mask = (blue|core)&(np.max(diff,axis=2)>22)&(radius<1.30)
        mask[:100,:]=False
        mask[blocked]=False
        emission=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1)
        glint=(np.min(c,axis=2)>215)&(np.min(diff,axis=2)>70)&(radius>.25)&(radius<1.3)
        glint[(yy<125)&(xx<150)]=False
        # Select compact luminous core; reject tiny label glyphs and tall actor edges.
        remaining=glint.copy();keep=np.zeros_like(glint);best=[]
        for sy,sx in zip(*np.where(glint)):
            if not remaining[sy,sx]:continue
            stack=[(sy,sx)];remaining[sy,sx]=False;component=[]
            while stack:
                py,px=stack.pop();component.append((py,px))
                for ny,nx in [(py-1,px),(py+1,px),(py,px-1),(py,px+1)]:
                    if 0<=ny<h and 0<=nx<w and remaining[ny,nx]:remaining[ny,nx]=False;stack.append((ny,nx))
            if len(component)<100:continue
            cy,cx=zip(*component);cw=max(cx)-min(cx)+1;ch=max(cy)-min(cy)+1
            if max(cw,ch)>min(cw,ch)*2.5:continue
            if len(component)>len(best):best=component
        for py,px in best:keep[py,px]=True
        glint=keep
        halo=np.array(Image.fromarray(glint.astype("uint8")*255).filter(ImageFilter.MaxFilter(17)))>0
        halo[:125,:195]=False
        halo&=(np.max(c,axis=2)-np.min(c,axis=2)<65)
        emission[~halo]=0
        # Feather the recovered bloom at source resolution before alpha quantization.
        # This removes hard segmentation steps without changing its track or sphere shape.
        emission=gaussian_filter(emission,sigma=(.65,.65,0))
        energy=float(np.max(np.maximum(c-base,0)))
        active=1.0 if i>=3 and i<61 and energy>35 else 0.0
        # Preserve the source highlight and animate only the visible source cadence.
        emission=np.maximum(emission,traced*active)
        alpha=np.max(emission,axis=2)
        fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255)
        rgba=np.dstack((fg,np.round(alpha*255))).astype(np.uint8);rgba[alpha==0]=0
        atlas.paste(Image.fromarray(rgba),(i%6*w,i//6*h));counts.append(int(np.count_nonzero(rgba[:,:,3])))
    assert max(counts)>1000
    target=output/'defense_video.png';atlas.save(target)
    return dict(path='warrior/defense_video.png',width=w,height=h,columns=6,
        durationsMs=[round((i+1)*1000/30)-round(i*1000/30) for i in range(n)],
        frameIndices=list(range(n)),pivotX=120,pivotY=185,scale=meta['scale'],
        anchor='CASTER',registration='VISUAL_CENTER',blend='SCREEN',filterBitmap=True,
        sourceTiming='USER_VIDEO_SAMPLED_30FPS',reconstruction='SOURCE_2D_SURFACE_WITH_MIRRORED_ARC_AND_HARMONIC_OCCLUSION_FILL',sourceGif=str(path.relative_to(root)),
        sourceSha256=meta['sourceFrameSha256'],atlasSha256=hashlib.sha256(target.read_bytes()).hexdigest(),
        opaquePixelCounts=counts,review=meta)
