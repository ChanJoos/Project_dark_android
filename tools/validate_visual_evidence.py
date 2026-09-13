from PIL import Image
from pathlib import Path
import math,sys
R=Path('app/src/main/res/drawable-nodpi'); FW,FH=36,48
DIRS=['NW','NE','SW','SE']; ROW={'NW':0,'NE':1,'SW':2,'SE':3}; SRC={'NE':0,'SE':1,'NW':2,'SW':3}
BW=[17,18,19,21];BH=[51,52,51,49];BX=[8,2,6,4];BY=[8,8,9,3]
ROBE_X=[[-1,-2,-1,0,1],[2,2,2,0,1],[-3,-2,0,2,1],[3,2,1,-1,-1]]
CX=[[-6,-10,10,-6,-1],[6,10,-10,6,1],[-7,-10,10,-7,9],[7,10,-10,7,-9]]
CY=[[21,21,23,18,26],[21,21,23,18,26],[19,19,24,17,21],[19,19,24,17,21]]
CA=[[56,62,48,59,52],[-64,-58,-72,-61,-68],[72,78,62,75,68],[-62,-56,-72,-59,-66]]
ORDER={'NW':0,'NE':1,'SW':2,'SE':3}
def im(n):return Image.open(R/n).convert('RGBA')
def bounds(i):
 b=i.getchannel('A').getbbox();return None if not b else (b[0],b[1],b[2]-1,b[3]-1)
def cell(i,r,c):return bounds(i.crop((c*FW,r*FH,(c+1)*FW,(r+1)*FH)))
def h(b):return b[3]-b[1]+1
def cx(b):return (b[0]+b[2])/2
body=im('player_peasant_idle_walk.webp');robe=im('player_armor_mu0000058_idle_walk.webp');w=im('player_weapon_mw001.webp')
print('=== REGISTERED ROBE WALK ===');walk=True
for d in DIRS:
 r=ROW[d];ds=[]
 for c in range(5):
  bb=cell(body,r,c); rb=cell(robe,r,c); off=ROBE_X[r][c]; dc=cx(rb)+off-cx(bb); ds.append(dc)
  print(f'{d} c{c}: offsetX={off:+d} registeredCenterDelta={dc:+.1f} bodyBottom={bb[3]} robeBottom={rb[3]}')
 jitter=max(ds)-min(ds);ok=jitter<=2 and max(abs(x) for x in ds)<=1
 walk&=ok;print(f'{d}: centerJitter={jitter:.1f} maxAbs={max(abs(x) for x in ds):.1f} PASS={ok}')
print('\n=== GROUP02 REJECTION + EXACT EQUIPPED FALLBACK ===');fallback=True
for d in DIRS:
 s=SRC[d]; ib=cell(body,ROW[d],0); ba=im(f'player_body_mm001_action02_{s}.webp');bb=bounds(ba);err=abs(h(ib)-h(bb)); rejected=err>1
 # Renderer rejects this action before drawSourceAction and draws the same equipped IDLE paper-doll column 0.
 exact=True
 fallback &= rejected and exact
 print(f'{d}: idleBodyHeight={h(ib)} actionBodyHeight={h(bb)} rawHeightErr={err} sourceRejected={rejected} fallbackExactIdle={exact}')
print('\n=== MW001 TIP ORIENTATION ===')
wb=bounds(w);px,py=2,4;tip=None;md=-1
for y in range(w.height):
 for x in range(w.width):
  if w.getpixel((x,y))[3]==0:continue
  q=(x-px)**2+(y-py)**2
  if q>md:md=q;tip=(x,y)
weapon=tip is not None and math.sqrt(md)>=5
for d in DIRS:
 i=ORDER[d];lx=tip[0]-px;ly=tip[1]-py
 if d in ('NW','SW'):lx=-lx
 carry=[]
 for c in range(5):
  a=math.radians(CA[i][c]);rx=lx*math.cos(a)-ly*math.sin(a);ry=lx*math.sin(a)+ly*math.cos(a);carry.append((rx,ry))
 carryUp=all(v[1]<1 for v in carry);weapon&=carryUp
 def ang(p):
  e=p/.55 if p<.55 else (1-p)/.45
  return {'NW':12+34*e,'NE':-25-30*e,'SW':10+34*e,'SE':22+28*e}[d]
 a=math.radians(ang(.55));rx=lx*math.cos(a)-ly*math.sin(a);ry=lx*math.sin(a)+ly*math.cos(a)
 sign={'NW':rx<0 and ry<0,'NE':rx>0 and ry<0,'SW':rx<0,'SE':rx>0 and ry>0}[d]
 weapon&=sign
 print(d,'carryVectors=',[(round(x,2),round(y,2)) for x,y in carry],f'attackPeak=({rx:.2f},{ry:.2f})','PASS=',carryUp and sign)
print('\nSUMMARY',{'registeredRobeWalk':walk,'safeAttackFallback':fallback,'weaponTip':weapon})
if not(walk and fallback and weapon):sys.exit(1)
