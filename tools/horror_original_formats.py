# Original binary format reader, ported from the published DALib MPF/HPF/palette formats.
# Primary reference: https://github.com/eriscorp/dalib (Compression.cs, MpfFile.cs, PaletteTable.cs).
from pathlib import Path
import struct,re,json
import numpy as np
from PIL import Image,ImageDraw
ROOT=Path(__file__).parent
class Archive:
 def __init__(self,name):
  b=Path(name).read_bytes();n=struct.unpack_from('<I',b)[0]-1
  starts=[struct.unpack_from('<I',b,4+17*i)[0] for i in range(n+1)]
  self.entries={b[8+17*i:21+17*i].split(b'\0')[0].decode('ascii').lower():b[starts[i]:starts[i+1]] for i in range(n)}
 def __getitem__(self,key):return self.entries[key.lower()]
def palette(b):return np.column_stack((np.frombuffer(b[:768],np.uint8).reshape(256,3),np.r_[0,np.full(255,255)].astype(np.uint8)))
def table(a,prefix):
 ranges={};over={}
 for n,b in a.entries.items():
  if not n.startswith(prefix) or not n.endswith('.tbl') or re.search(r'\d',n):continue
  for line in b.decode('ascii',errors='ignore').splitlines():
   try:
    v=list(map(int,line.split('//')[0].split()))
    if len(v)==2:over[v[0]]=v[1]
    elif len(v)==3 and v[2]>=0:ranges.update({i:v[2] for i in range(v[0],v[1]+1)})
   except ValueError:pass
 ranges.update(over);return ranges
def decompress(b):
 if b[:4]!=bytes.fromhex('55aa02ff'):return b
 odd=[2*i+1 for i in range(256)];even=[2*i+2 for i in range(256)];pair=[0]*513
 for i in range(256):pair[2*i+1]=pair[2*i+2]=i
 k=7;l=0;out=bytearray()
 while True:
  val=0
  while val<=255:
   if k==7:l+=1;k=0
   else:k+=1
   val=even[val] if b[4+l-1]&(1<<k) else odd[val]
  v3=val;v2=pair[val]
  while v3 and v2:
   i=pair[v2];j=odd[i]
   if j==v2:j=even[i];even[i]=v3
   else:odd[i]=v3
   if odd[v2]==v3:odd[v2]=j
   else:even[v2]=j
   pair[v3]=i;pair[j]=v2;v3=i;v2=pair[v3]
  val-=256
  if val==256:break
  out.append(val)
 return bytes(out)
def mpf(b):
 p=0
 def read(fmt):
  nonlocal p
  v=struct.unpack_from('<'+fmt,b,p);p+=struct.calcsize('<'+fmt);return v[0] if len(v)==1 else v
 if read('i')==-1:
  flags=read('I')
  if flags&4:p+=read('I')*4
 else:p=0
 count=read('B');w,h=read('hh');dl=read('I');walk,wc=read('BB');fmt=read('h')
 if fmt==-1:stand,sc,opt,ratio,attack,ac,a2,c2,a3,c3=read('10B')
 else:p-=2;attack,ac,stand,sc,opt,ratio=read('6B')
 frames=[];pal=0;i=0
 while i<count:
  l,t,r,bo,cx,cy,off=read('6hi')
  if l==t==-1:pal=off;count-=1;continue
  frames.append(dict(left=l,top=t,right=r,bottom=bo,cx=cx,cy=cy,data=b[len(b)-dl+off:len(b)-dl+off+(r-l)*(bo-t)]));i+=1
 return dict(width=w,height=h,walk=walk,walkcount=wc,stand=stand,standcount=sc,attack=attack,attackcount=ac,palette=pal,frames=frames)
def render_frame(m,pal,i):
 f=m['frames'][i];return Image.fromarray(pal[np.frombuffer(f['data'],np.uint8).reshape(f['bottom']-f['top'],f['right']-f['left'])])
