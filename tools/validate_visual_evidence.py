from PIL import Image
from pathlib import Path
import math, sys

R=Path('app/src/main/res/drawable-nodpi')
FW,FH=36,48
DIRS=['NW','NE','SW','SE']
ROWS={'NW':0,'NE':1,'SW':2,'SE':3}
SRC={'NE':0,'SE':1,'NW':2,'SW':3}
BW=[17,18,19,21]; BH=[51,52,51,49]; BX=[8,2,6,4]; BY=[8,8,9,3]
RW=[14,19,15,20]; RH=[33,22,30,21]; RX=[8,3,6,6]; RY=[20,27,23,22]
CX=[[-6,-10,10,-6,-1],[6,10,-10,6,1],[-7,-10,10,-7,9],[7,10,-10,7,-9]]
CY=[[21,21,23,18,26],[21,21,23,18,26],[19,19,24,17,21],[19,19,24,17,21]]
CA=[[-56,-62,-48,-59,-52],[-64,-58,-72,-61,-68],[-72,-78,-62,-75,-68],[-62,-56,-72,-59,-66]]
ORDER={'NW':0,'NE':1,'SW':2,'SE':3}

def im(name): return Image.open(R/name).convert('RGBA')
def bounds(img):
    a=img.getchannel('A'); b=a.getbbox()
    if not b:return None
    l,t,r,bt=b; return (l,t,r-1,bt-1)
def cell(img,row,col): return bounds(img.crop((col*FW,row*FH,(col+1)*FW,(row+1)*FH)))
def h(b): return 0 if b is None else b[3]-b[1]+1
def cx(b): return (b[0]+b[2])/2
def trans(b,x,y): return None if b is None else (b[0]+x,b[1]+y,b[2]+x,b[3]+y)
def union(a,b):
    if a is None:return b
    if b is None:return a
    return (min(a[0],b[0]),min(a[1],b[1]),max(a[2],b[2]),max(a[3],b[3]))
def clip(b):
    if b is None:return None
    q=(max(0,b[0]),max(0,b[1]),min(FW-1,b[2]),min(FH-1,b[3]))
    return None if q[2]<q[0] or q[3]<q[1] else q

body=im('player_peasant_idle_walk.webp'); robe=im('player_armor_mu0000058_idle_walk.webp'); weapon=im('player_weapon_mw001.webp')
assert body.size==(180,192) and robe.size==(180,192) and weapon.size==(16,8)
print('=== ROBE WALK 4x5 ACTUAL PIXELS ===')
walk_ok=True
for d in DIRS:
    dfs=[]; dcs=[]
    for c in range(5):
        bb=cell(body,ROWS[d],c); rb=cell(robe,ROWS[d],c)
        if bb is None or rb is None: walk_ok=False; print(d,c,'EMPTY'); continue
        df=rb[3]-bb[3]; dc=cx(rb)-cx(bb); dfs.append(df); dcs.append(dc)
        print(f'{d} c{c}: body={bb} robe={rb} dFoot={df:+d} dCenter={dc:+.1f}')
    fr=(max(dfs)-min(dfs)) if dfs else 999; cr=(max(dcs)-min(dcs)) if dcs else 999
    ok=fr<=1 and cr<=2
    walk_ok &= ok
    print(f'{d}: footDeltaRange={min(dfs) if dfs else None}..{max(dfs) if dfs else None} jitter={fr}; centerDeltaRange={min(dcs) if dcs else None}..{max(dcs) if dcs else None} jitter={cr:.1f}; PASS={ok}')

print('\n=== ATTACK BODY + FULL_BODY ROBE ACTUAL PIXELS ===')
attack_ok=True; body_source_ok=True
for d in DIRS:
    s=SRC[d]; ib=cell(body,ROWS[d],0); ir=cell(robe,ROWS[d],0); idle=union(ib,ir)
    ba=im(f'player_body_mm001_action02_{s}.webp'); ra=im(f'player_robe_mu0000058_action02_{s}.webp')
    assert ba.size==(BW[s],BH[s]) and ra.size==(RW[s],RH[s])
    bb=bounds(ba); rb=bounds(ra)
    raw_h=abs(h(ib)-h(bb)); body_source_ok &= raw_h<=1
    gb=BY[s]+bb[3]; gc=BX[s]+cx(bb); dy=ib[3]-gb; dx=round(cx(ib)-gc)
    pb=trans(bb,BX[s]+dx,BY[s]+dy); pr=trans(rb,RX[s]+dx,RY[s]+dy); ac=clip(union(pb,pr))
    he=abs(h(idle)-h(ac)); fe=abs(idle[3]-ac[3]); ce=round(abs(cx(idle)-cx(ac)))
    ok=he<=1 and fe<=1 and ce<=2
    attack_ok &= ok
    print(f'{d}: idle={idle} rawBody={bb} rawBodyHeightErr={raw_h} normalize=({dx:+d},{dy:+d}) attack={ac} err(h/f/c)={he}/{fe}/{ce} PASS={ok} rendererBodyEligible={raw_h<=1}')

print('\n=== MW001 ACTUAL TIP / CARRY / ATTACK ===')
wb=bounds(weapon); px,py=2,4; tip=None; md=-1
for y in range(weapon.height):
  for x in range(weapon.width):
    if weapon.getpixel((x,y))[3]==0: continue
    q=(x-px)**2+(y-py)**2
    if q>md: md=q; tip=(x,y)
print('weaponBounds=',wb,'pivot=',(px,py),'tipLocal=',tip,'tipDistance=',round(math.sqrt(md),3))
weapon_ok=tip is not None and math.sqrt(md)>=5
for d in DIRS:
    i=ORDER[d]; lx=tip[0]-px; ly=tip[1]-py
    if d in ('NW','SW'): lx=-lx
    vals=[]
    for c in range(5):
        a=math.radians(CA[i][c]); rx=lx*math.cos(a)-ly*math.sin(a); ry=lx*math.sin(a)+ly*math.cos(a)
        vals.append((round(CX[i][c]+rx,2),round(CY[i][c]+ry,2),round(rx,2),round(ry,2)))
    print(d,'carry hand+tip / vector=',vals)
    def aa(p):
      env=p/.55 if p<.55 else (1-p)/.45
      return {'NW':-12-34*env,'NE':-25-30*env,'SW':10+34*env,'SE':22+28*env}[d]
    av=[]
    for p in (0,.55,1):
      a=math.radians(aa(p)); rx=lx*math.cos(a)-ly*math.sin(a); ry=lx*math.sin(a)+ly*math.cos(a)
      av.append((p,round(aa(p),1),round(rx,2),round(ry,2)))
    print(d,'attack phase/angle/tipVector=',av)

print('\nSUMMARY',{'walk':walk_ok,'attackComposite':attack_ok,'rendererBodySourceEligible':body_source_ok,'weaponGeometry':weapon_ok})
if not (walk_ok and attack_ok and body_source_ok and weapon_ok): sys.exit(1)
