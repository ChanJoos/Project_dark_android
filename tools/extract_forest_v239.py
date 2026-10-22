"""Quilt only unoccluded soil patches from the supplied forest recording.
No whole screenshot, actors, UI or debug grid becomes map terrain.
"""
from pathlib import Path
import argparse,subprocess,hashlib,json
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser();p.add_argument('video',type=Path);a=p.parse_args()
 raw=subprocess.check_output(['ffmpeg','-v','error','-ss','35','-i',str(a.video),'-frames:v','1','-f','rawvideo','-pix_fmt','rgb24','-threads','1','-'])
 frame=Image.fromarray(np.frombuffer(raw,np.uint8).reshape(1080,2340,3))
 boxes=[(755,580,883,676),(1250,573,1378,669),(1310,650,1438,746)]
 patches=[frame.crop(b).resize((96,72),Image.Resampling.NEAREST) for b in boxes]
 out=Image.new('RGB',(384,288));out.paste(patches[0].resize(out.size,Image.Resampling.NEAREST))
 mask=Image.new('L',(96,72));ImageDraw.Draw(mask).rectangle((12,12,83,59),fill=255);mask=mask.filter(ImageFilter.GaussianBlur(5))
 for j in range(-1,7):
  for i in range(-1,8):out.paste(patches[(i*7+j*3)%3],(i*64,j*48),mask)
 # Periodic edge blend prevents a rectangular seam in the repeat shader.
 ar=np.array(out,dtype=float)
 for axis in (0,1):
  for k in range(16):
   f=(16-k)/32
   if axis==0:
    x,y=ar[k].copy(),ar[-1-k].copy();ar[k]=x*(1-f)+y*f;ar[-1-k]=y*(1-f)+x*f
   else:
    x,y=ar[:,k].copy(),ar[:,-1-k].copy();ar[:,k]=x*(1-f)+y*f;ar[:,-1-k]=y*(1-f)+x*f
 dest=ROOT/'assets/pote/production/reference_v239';dest.mkdir(exist_ok=True)
 Image.fromarray(ar.astype('uint8')).save(dest/'forest_floor.png')
 record=dict(revision='V239_CAPTURE_QUILT',evidence='ADAPTED_SOURCE_MATERIAL; topology retained, not original full map',sourceName=a.video.name,sourceSha256=hashlib.sha256(a.video.read_bytes()).hexdigest(),seconds=35,boxes=boxes,sha256=hashlib.sha256((dest/'forest_floor.png').read_bytes()).hexdigest())
 (ROOT/'master/source/pote_v239/terrain.json').write_text(json.dumps(record,indent=2)+'\n')
if __name__=='__main__':main()
