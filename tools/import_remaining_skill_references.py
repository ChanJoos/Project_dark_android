#!/usr/bin/env python3
"""Recover the labelled large-scene Holy Dragon crop without copying its training dummies."""
import hashlib, json, math
import numpy as np
from PIL import Image

def project(root, output, manifest):
    rows = json.loads((root/'master/source/skill_fx/naver_magic_recovered_20261001/definitions.json').read_text())
    row = next(r for r in rows if r['id'] == 'SK_성직자_040')
    demo = row['demonstration']
    assert hashlib.sha256((root/demo['path']).read_bytes()).hexdigest() == demo['sha256']
    im = Image.open(root/demo['path']); frames = []; times = []
    for i in range(im.n_frames):
        im.seek(i); frames.append(np.asarray(im.convert('RGB'), dtype=np.float32))
        times.append(max(10, im.info.get('duration', 100)))
    # The middle dummy is isolated from the other seven recipient demonstrations.
    # Frame zero is the same stationary dummy and floor, before the dragon appears.
    region = (215, 22, 313, 124); x0,y0,x1,y1 = region
    baseline = frames[0]; tiles = []; counts = []
    for rgb in frames:
        diff = rgb-baseline
        emission = np.clip(np.maximum(diff,0)/np.maximum(255-baseline,1),0,1)
        alpha = np.max(emission,axis=2)
        mask = (np.max(np.abs(diff),axis=2)>23) & (np.max(rgb,axis=2)>120)
        # Brown stationary dummy pixels are never part of the particle atlas.
        r,g,b=rgb[:,:,0],rgb[:,:,1],rgb[:,:,2]
        mask &= (b>r-25) & (b>g-15)
        alpha[~mask]=0
        fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255)
        tile=np.dstack((fg,np.round(alpha*255))).astype(np.uint8);tile[~mask]=0
        tile=tile[y0:y1,x0:x1];tiles.append(tile);counts.append(int(np.count_nonzero(tile[:,:,3])))
    assert counts[0]==0 and max(counts)>100, counts
    w=x1-x0;h=y1-y0;atlas=Image.new('RGBA',(w*4,h*math.ceil(len(tiles)/4)))
    for i,tile in enumerate(tiles):atlas.paste(Image.fromarray(tile),(i%4*w,i//4*h))
    target=output/'holy_dragon_recipient.png';atlas.save(target)
    channel=dict(path='classic/'+target.name,width=w,height=h,columns=4,durationsMs=times,
                 frameIndices=list(range(len(tiles))),pivotX=269-x0,pivotY=87-y0,scale=1.,
                 anchor='RECIPIENT',registration='VISUAL_CENTER',blend='SCREEN',
                 sourceGif=demo['path'],sourceSha256=demo['sha256'],
                 atlasSha256=hashlib.sha256(target.read_bytes()).hexdigest(),opaquePixelCounts=counts,
                 review=dict(region=list(region),baselineFrame=0,recipientCenter=[269,87],
                             reason='LABELLED_HOLY_DRAGON_TABLE_AND_GIF; SINGLE_RECIPIENT_FROM_EIGHT_DUMMY_SCENE',
                             limitations=['Actor-occluded pixels remain absent.','One recipient crop is repeated by the existing screen-target resolver; original multi-target staggering is not proven.']))
    manifest['skills'][row['id']]=dict(name=row['name'],article=row['article'],
        mapping='LABELLED_LARGE_SCENE_SINGLE_RECIPIENT_SOURCE',channels={'RECIPIENT_CONTACT':channel})
