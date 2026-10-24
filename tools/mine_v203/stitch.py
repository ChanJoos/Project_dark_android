from pathlib import Path
import cv2,numpy as np,json
from PIL import Image
frames={n:cv2.cvtColor(cv2.imread(f'mine203/frames/native{n:03d}.png'),cv2.COLOR_BGR2RGB) for n in range(69,83)}
sift=cv2.SIFT_create(nfeatures=4000);feat={};mask=np.zeros((480,640),np.uint8);mask[10:385,22:610]=255;mask[150:260,260:360]=0
for n,im in frames.items():feat[n]=sift.detectAndCompute(cv2.cvtColor(im,cv2.COLOR_RGB2GRAY),mask)
positions={69:(0.,0.)};records=[]
for n in range(70,83):
 ka,da=feat[n-1];kb,db=feat[n];pairs=cv2.BFMatcher().knnMatch(db,da,k=2);matches=[x for x,y in pairs if x.distance<.78*y.distance]
 a=np.float32([kb[m.queryIdx].pt for m in matches]);b=np.float32([ka[m.trainIdx].pt for m in matches]);T,good=cv2.estimateAffinePartial2D(a,b,method=cv2.RANSAC,ransacReprojThreshold=2)
 dx,dy=T[0,2],T[1,2];p=positions[n-1];positions[n]=(p[0]+dx,p[1]+dy);records.append([n,len(matches),int(good.sum()),round(dx,1),round(dy,1)])
print(records)
minx=int(min(p[0] for p in positions.values()))-20;miny=int(min(p[1] for p in positions.values()))-20;maxx=int(max(p[0] for p in positions.values()))+660;maxy=int(max(p[1] for p in positions.values()))+480
canvas=np.zeros((maxy-miny,maxx-minx,4),np.uint8)
for n,im in frames.items():
 x,y=map(lambda v:int(round(v)),positions[n]);x-=minx;y-=miny;m=mask.copy();m[0:10]=0
 a=np.dstack((im,m));v=canvas[y:y+480,x:x+640];v[m>0]=a[m>0]
Image.fromarray(canvas).save('mine203/stitched.png');Path('mine203/positions.json').write_text(json.dumps({'min':[minx,miny],'positions':positions,'records':records}));print('SIZE',canvas.shape)
