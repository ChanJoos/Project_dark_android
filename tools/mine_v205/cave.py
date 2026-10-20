import json,numpy as np,cv2
from PIL import Image
j=json.load(open('mine205/dense-positions.json'));ps=j['pos'];ref=261;out=np.zeros((480,640,4),np.uint8)
for n in range(225,266):
 a=np.array(Image.open(f'mine205/dense/{n:04}.png'));mask=np.zeros((480,640),np.uint8);mask[8:395,22:613]=255;mask[140:255,260:359]=0
 dx,dy=np.array(ps[str(n)])-ps[str(ref)];b=cv2.warpAffine(np.dstack((a,mask)),np.float32([[1,0,dx],[0,1,dy]]),(640,480),flags=cv2.INTER_NEAREST);out[b[:,:,3]>0]=b[b[:,:,3]>0]
Image.fromarray(out).save('mine205/cave-scene.png')
