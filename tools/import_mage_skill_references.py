#!/usr/bin/env python3
"""Labelled Mage capture pixels with explicitly authored still-frame timing."""
import hashlib, json, math
import numpy as np
from PIL import Image

def project(root, output, manifest):
    source=root/'master/source/skill_fx/naver_magic_recovered_20261001'
    lookup={(m['articleId'],m['index']):m for m in json.loads((source/'media.json').read_text())['media']}
    cat={r['name']:r['id'] for r in json.loads((root/'app/src/main/assets/skills/catalog.json').read_text()) if r['job']=='마법사'}
    rows=[]
    def bind(name,article,indices,region,pivot,color,blocked=None,anchor='RECIPIENT',scale=1.):
        rows.append(dict(id=cat[name],name=name,articleId=article,indices=list(indices),region=region,pivot=pivot,
                         color=color,blocked=blocked or [],anchor=anchor,scale=scale))
    groups=[('COLD',[3,4,5],['마레노','마레누스','마레네라']),('NEUTRAL',[9,10,11],['테라미코','테라미쿠스','테라미에라']),
            ('COLD',[15,16,17],['아듀로','아듀로스','아듀레나']),('WARM',[21,22,23],['플라모','플라무스','플라메라'])]
    for color,indices,names in groups:
        for i,name in zip(indices,names):bind(name,221564,[i],[0,33,100,96],[50,58],color,[[41,46,62,83]])
    for i,name,color in zip([4,5,6,7],['렌토','바르도','데프레코','프라보'],['PURPLE','YELLOW','PURPLE','RED']):
        bind(name,221555,[i],[8,32,94,96],[50,59],color,[[42,47,62,84]])
    bind('메테오',254766,[0,1,2],[30,102,103,224],[67,184],'WARM',[[55,140,88,153],[56,156,79,215]])
    bind('매직프로텍션',254766,range(3,9),[142,80,242,143],[190,105],'ROCK',[[177,61,204,126]])
    bind('속성강화',254766,range(9,13),[2,52,110,142],[48,88],'BRIGHT',[[33,72,64,116]])
    for name,indices,color in [('아이스블러스트',range(13,17),'COLD'),('퀘이크',range(17,21),'DIFFERENCE'),
                              ('플레쉬스톰',range(21,25),'GREEN'),('플레어',range(25,29),'WARM')]:
        bind(name,254766,indices,[76,75,191,198],[134,153],color,[[125,139,149,190],[124,129,155,138]])
    bind('어둠의각인',254766,range(29,35),[70,64,173,147],[125,107],'DARK_DIFFERENCE',[[55,47,77,126]])
    for i,name,color in [(35,'렌티아','PURPLE'),(36,'바르데아','YELLOW'),(37,'데프레타','PURPLE'),(38,'프라베라','RED')]:
        bind(name,254766,[i],[10,99,90,246],[56,174],color,scale=.8)
    bind('델리스펠라스',254766,[39,40,41],[7,34,107,135],[55,89],'COLD',[[38,62,69,113]],anchor='CASTER')
    bind('포트리스',254766,[42,43,44],[7,34,107,135],[55,89],'COLD',[[38,62,69,113]],anchor='CASTER')
    def read(m):
        assert hashlib.sha256((root/m['path']).read_bytes()).hexdigest()==m['sha256']
        return np.array(Image.open(root/m['path']).convert('RGB'),dtype=np.float32)
    # The first fire capture exposes the idle actor/floor except its small left spark.
    # Register that matting plate against clean outer floor pixels in each 100px crop.
    circle_base=read(lookup[221564,21])
    curse_base=np.median(np.stack([read(lookup[254766,i]) for i in [35,36,37,38]]),axis=0)
    room_base=np.median(np.stack([read(lookup[254766,i]) for i in range(13,29)]),axis=0)
    for row in rows:
        originals=[lookup[row['articleId'],i] for i in row['indices']];frames=[read(m) for m in originals]
        base=circle_base if row['articleId'] in [221564,221555] else room_base if row['name']=='퀘이크' else np.minimum.reduce(frames) if len(frames)>1 else curse_base
        if row['articleId'] in [221564,221555] and row['name']!='플라모':
            floor=np.zeros((100,100),dtype=bool);floor[45:94,4:20]=True;floor[45:94,82:96]=True
            candidates=[(float(np.median(np.abs(frames[0][floor]-np.roll(circle_base,(dy,dx),(0,1))[floor]))),dx,dy)
                        for dy in range(-4,5) for dx in range(-4,5)]
            _,dx,dy=min(candidates);base=np.roll(circle_base,(dy,dx),(0,1))
            row['mattingPlate']=dict(path=lookup[221564,21]['path'],sha256=lookup[221564,21]['sha256'],dx=dx,dy=dy)
        x0,y0,x1,y1=row['region'];w=x1-x0;h=y1-y0;tiles=[];counts=[]
        for rgb in frames:
            r,g,b=rgb[:,:,0],rgb[:,:,1],rgb[:,:,2];lo=np.min(rgb,axis=2);hi=np.max(rgb,axis=2)
            diff=rgb-base;changed=np.max(np.abs(diff),axis=2)>18;white=(lo>150)&(hi-lo<100);color=row['color']
            if color=='COLD':mask=((b>r+12)&(g>r+5)&(b>115)&(g>95))|((lo>140)&(hi-lo<55))
            elif color=='NEUTRAL':mask=(hi>95)&(hi-lo<95)&(np.max(diff,axis=2)>25)
            elif color=='WARM':mask=((r>205)&(g>80)&(r>=g)&(g>b+20))|white
            elif color=='PURPLE':mask=((r>g*1.25)&(b>g*1.3)&(b>100)&(r>95))|white
            elif color=='GREEN':mask=((g>r*1.25)&(g>b*1.15)&(g>130))|white
            elif color=='YELLOW':mask=((r>180)&(g>160)&(b<g*.8))|white
            elif color=='RED':mask=((r>175)&(r>g*1.35)&(r>b*1.2))|white
            elif color=='BRIGHT':mask=(hi>190)&((hi-lo>35)|white)
            elif color=='ROCK':mask=changed&(r>g*.9)&(r>b*1.25)&(g>25)
            elif color=='DARK_DIFFERENCE':mask=(hi<90)&(hi-lo<18)
            else:mask=changed
            if color=='DIFFERENCE':mask&=(np.max(np.abs(diff),axis=2)>40)
            valid=np.zeros_like(mask);valid[y0:y1,x0:x1]=True;mask&=valid
            for bx0,by0,bx1,by1 in row['blocked']:
                allowed=((lo>150)&(np.max(diff,axis=2)>25))|(np.min(diff,axis=2)>25)|((lo>110)&(np.max(diff,axis=2)>50))
                mask[by0:by1,bx0:bx1]&=allowed[by0:by1,bx0:bx1]
            if row['articleId'] in [221564,221555]:mask[:41,:]=False
            if color in ['DIFFERENCE','DARK_DIFFERENCE','ROCK']:fg=rgb;alpha=mask.astype(np.float32)
            else:
                # Colour-gated source emission keeps the captured pillar/core.
                # Subtracting a different active spell used to erase these pixels.
                emission=np.clip(np.maximum(diff,0)/np.maximum(255-base,1),0,1) if row.get('mattingPlate') else rgb/255
                alpha=np.max(emission,axis=2);alpha[~mask]=0;fg=np.clip(emission/np.maximum(alpha[:,:,None],.001)*255,0,255)
            tile=np.dstack((fg,np.round(alpha*255))).astype(np.uint8);tile[~mask]=0;tile=tile[y0:y1,x0:x1]
            tiles.append(tile);counts.append(int(np.count_nonzero(tile[:,:,3])))
        assert max(counts)>3,(row['name'],'No recoverable particle')
        if len(tiles)==1:
            original=tiles[0];tiles=[];counts=[]
            for fade in [.35,1.,.75,.25]:
                t=original.copy();t[:,:,3]=np.round(t[:,:,3]*fade);tiles.append(t);counts.append(int(np.count_nonzero(t[:,:,3])))
            durations=[80,120,100,80];timing='PROJECT_STATIC_HOLD_FADE'
        else:durations=[100]*len(tiles);timing='PROJECT_CAPTURE_FRAME_ORDER_NOT_ORIGINAL_CADENCE'
        atlas=Image.new('RGBA',(w*4,h*math.ceil(len(tiles)/4)))
        for i,t in enumerate(tiles):atlas.paste(Image.fromarray(t),(i%4*w,i//4*h))
        slug='mage_'+hashlib.sha256(row['id'].encode()).hexdigest()[:16]+'.png';target=output/slug;atlas.save(target)
        channel=dict(path='classic/'+slug,width=w,height=h,columns=4,durationsMs=durations,frameIndices=row['indices'],
                     pivotX=row['pivot'][0]-x0,pivotY=row['pivot'][1]-y0,scale=row['scale'],
                     blend='NORMAL' if row['color'] in ['DIFFERENCE','DARK_DIFFERENCE','ROCK'] else 'SCREEN',
                     anchor=row['anchor'],registration='VISUAL_CENTER',sourceTiming=timing,
                     sourceGif=originals[0]['path'],sourceSha256=originals[0]['sha256'],
                     sourceFrames=[dict(path=m['path'],sha256=m['sha256'],index=m['index']) for m in originals],
                     atlasSha256=hashlib.sha256(target.read_bytes()).hexdigest(),opaquePixelCounts=counts,review=row)
        manifest['skills'][row['id']]=dict(name=row['name'],article=f"https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/{row['articleId']}",
                                         mapping='LABELLED_MAGE_SOURCE_CAPTURE_PROJECT_TIMING',channels={'RECIPIENT_CONTACT':channel})
    (source/'mage_bindings.json').write_text(json.dumps(dict(revision='MAGE_SOURCE_V77',rows=rows,
        limitations=['Still captures have project timing; original animation cadence is not known.','Occluded effect pixels remain absent.',
                     'No different spell receives a similar-looking source or invented icon.']),ensure_ascii=False,indent=2)+'\n')
    print('Mage source capture routes:',len(rows))
