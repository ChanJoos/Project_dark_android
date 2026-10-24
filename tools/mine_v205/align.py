import cv2,numpy as np,json
from PIL import Image
from pathlib import Path
sift=cv2.SIFT_create(nfeatures=3000);mask=np.zeros((480,640),np.uint8);mask[8:395,22:613]=255;mask[140:250,275:350]=0;mask[35:125,:]=0
pos={};prev=None;p=np.zeros(2);bad=[]
for n in range(16,416):
 im=cv2.imread(f'mine205/dense/{n:04}.png'); k,d=sift.detectAndCompute(cv2.cvtColor(im,cv2.COLOR_BGR2GRAY),mask)
 if prev is not None:
  ka,da=prev;ms=[a for a,b in cv2.BFMatcher().knnMatch(d,da,k=2) if a.distance<.7*b.distance];ds=np.array([np.array(ka[m.trainIdx].pt)-k[m.queryIdx].pt for m in ms]);med=np.median(ds,axis=0);g=ds[np.linalg.norm(ds-med,axis=1)<2];delta=np.median(g,axis=0) if len(g)>8 else med
  if len(g)<8:bad.append(n)
  p+=delta
 pos[n]=p.tolist();prev=(k,d)
lo=np.floor(np.min(list(pos.values()),axis=0)).astype(int)-5;hi=np.ceil(np.max(list(pos.values()),axis=0)).astype(int)+[645,485];out=np.zeros((hi[1]-lo[1],hi[0]-lo[0],4),np.uint8)
for n,p in pos.items():
 im=np.array(Image.open(f'mine205/dense/{n:04}.png'));x,y=np.rint(np.array(p)-lo).astype(int);a=out[y:y+480,x:x+640];a[mask>0,:3]=im[mask>0];a[mask>0,3]=255
Image.fromarray(out).save('mine205/dense-mosaic.png');Path('mine205/dense-positions.json').write_text(json.dumps({'pos':pos,'lo':lo.tolist(),'bad':bad}));print(out.shape,bad)
