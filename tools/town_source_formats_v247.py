"""Original SPF decoder; DALib layout, row pitch and indexed/RGB565 pixels. No image filtering."""
import struct,sys
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw
from horror_original_formats import Archive,mpf,palette,render_frame
def rgb565(a):
 a=a.astype(np.uint32);return np.stack((((a>>11)&31)*255//31,((a>>5)&63)*255//63,(a&31)*255//31,np.where(a==0,0,255)),axis=-1).astype('uint8')
def spf(b):
 typ=struct.unpack_from('<I',b,8)[0];p=12
 if typ==0:pal=rgb565(np.frombuffer(b[p:p+512],'<u2'));p+=1024
 n=struct.unpack_from('<I',b,p)[0];p+=4;records=[struct.unpack_from('<4H2h5I',b,p+32*i) for i in range(n)];start=p+32*n+4;ims=[]
 for r in records:
  l,t,ri,bo,cx,cy,fl,off,pitch,cnt,sz=r;w,h=ri-l,bo-t
  if w<=0 or h<=0 or pitch==0:ims.append(Image.new('RGBA',(1,1)));continue
  data=b[start+off:start+off+pitch*h]
  a=np.frombuffer(data,'uint8' if typ==0 else '<u2').reshape(h,pitch if typ==0 else pitch//2)[:,:w];rgba=pal[a] if typ==0 else rgb565(a);ims.append(Image.fromarray(rgba))
 return ims
