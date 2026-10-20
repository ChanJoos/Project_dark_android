"""Audit the complete retained recording tour against recovered source cells; never infer unseen parity."""
from pathlib import Path
import sys,json,zipfile,numpy as np,cv2,hashlib
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
recording=Path(sys.argv[1]);coordinates=json.loads(Path(sys.argv[2]).read_text());output=Path(sys.argv[3]);output.mkdir(parents=True,exist_ok=True)
assets=ROOT/'app/build/generated/mine-hunt-v208/mine-hunt-v208'
with zipfile.ZipFile(ROOT/'master/source/mine_hunt_v208/original_sources.zip') as z:raw=z.read('maps/lod2102.map')
grid=np.frombuffer(raw,'<u2').reshape(102,150,3);cache={}
def asset(name):
 if name not in cache:cache[name]=Image.open(assets/(name+'.png')).convert('RGBA')
 return cache[name]
def camera(u0,v0):
 out=Image.new('RGBA',(1120,800),(0,0,0,255));mask=Image.new('L',out.size);objects=[]
 for v in range(max(0,v0-42),min(102,v0+42)):
  for u in range(max(0,u0-42),min(150,u0+42)):
   x=560+(u-u0-v+v0)*28;y=380+(u-u0+v-v0)*14;bg=int(grid[v,u,0])
   if bg and -60<x<1180 and -35<y<835:out.alpha_composite(asset('bg'+str(bg)),(x-28,y-14))
   for side in (1,2):
    f=int(grid[v,u,side])
    if f>12:
     image=asset(f'stc{f:05}');left=x-28+(side-1)*28;top=y+14-image.height
     if left<1120 and left+image.width>0 and top<800 and top+image.height>0:objects.append((y,left,top,image))
 for _,x,y,image in sorted(objects,key=lambda x:x[0]):out.alpha_composite(image,(x,y));mask.paste(255,(x,y,x+image.width,y+image.height),image.getchannel('A'))
 return cv2.cvtColor(np.asarray(out),cv2.COLOR_RGBA2BGR),np.asarray(mask)
cap=cv2.VideoCapture(str(recording));sift=cv2.SIFT_create(nfeatures=6000);rows=[]
for row in coordinates:
 uv=row['coordinate']
 if uv is None:rows.append(dict(second=row['second'],status='HUD_OCR_UNRESOLVED'));continue
 cap.set(cv2.CAP_PROP_POS_MSEC,row['second']*1000);ok,frame=cap.read()
 if not ok:continue
 source,fg=camera(*uv);ka,da=sift.detectAndCompute(source,None);kb,db=sift.detectAndCompute(frame,None);matches=[m for m,n in cv2.BFMatcher().knnMatch(da,db,k=2) if m.distance<.70*n.distance]
 x=np.float32([ka[m.queryIdx].pt for m in matches]);y=np.float32([kb[m.trainIdx].pt for m in matches]);M,inliers=cv2.estimateAffinePartial2D(x,y,method=cv2.RANSAC,ransacReprojThreshold=3,maxIters=10000)
 if M is None:rows.append(dict(second=row['second'],coordinate=uv,status='NO_TRANSFORM'));continue
 keep=inliers.ravel().astype(bool);error=np.linalg.norm(x@M[:,:2].T+M[:,2]-y,axis=1);foreground=sum(bool(good and fg[min(799,max(0,int(pt[1]))),min(1119,max(0,int(pt[0])))]) for good,pt in zip(keep,x));status='OBSERVED_LANDMARK_MATCH' if int(keep.sum())>=30 and foreground>=10 else 'INSUFFICIENT_LANDMARK_EVIDENCE'
 result=dict(second=row['second'],coordinate=uv,status=status,matches=len(matches),inliers=int(keep.sum()),foregroundInliers=foreground,medianErrorPixels=float(np.median(error[keep])));rows.append(result);print(result,flush=True)
 if row['second'] in (0,16,24,40,56,72,88,100,120):cv2.imwrite(str(output/f'source-{row["second"]}.jpg'),source)
report=dict(recordingSha256=hashlib.sha256(recording.read_bytes()).hexdigest(),recordingSeconds=127.974271,sampledEverySeconds=4,regions=rows,limit='Complete128sec retained recording sampled across tour, not full15m32s YouTube video. Media CDN returned195byte SiteUnavailable bodies, not usable video. Unseen regions/current server spawn or warp tables remain unverified.')
(output/'complete_recording_audit.json').write_text(json.dumps(report,indent=2)+'\n');print('MATCHED',sum(r['status']=='OBSERVED_LANDMARK_MATCH' for r in rows),'OF',len(rows))
