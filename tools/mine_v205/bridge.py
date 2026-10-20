"""Register source frames into a temporary object extraction canvas (never the map)."""
import json,numpy as np
from PIL import Image
j=json.load(open('mine205/dense-positions.json'));pos=j['pos'];ns=range(135,165)
lo=np.floor(np.min([pos[str(n)] for n in ns],axis=0)).astype(int)
hi=np.ceil(np.max([pos[str(n)] for n in ns],axis=0)).astype(int)+[640,480]
out=np.zeros((hi[1]-lo[1],hi[0]-lo[0],4),np.uint8)
for n in ns:
 a=np.array(Image.open(f'mine205/dense/{n:04}.png'));mask=np.zeros((480,640),bool)
 mask[12:396,20:613]=True;mask[140:250,275:350]=False
 x,y=np.rint(np.array(pos[str(n)])-lo).astype(int);v=out[y:y+480,x:x+640]
 v[mask,:3]=a[mask];v[mask,3]=255
Image.fromarray(out).save('mine205/bridge-scene.png')
