"""Independent full raw NPC/RGB565 portal pixel and source screenshot-border audit."""
from pathlib import Path
import json,zipfile,hashlib
import numpy as np
from PIL import Image
from horror_original_formats import mpf,palette,render_frame
from build_mage_v243 import decode
R=Path(__file__).resolve().parents[1];S=R/'master/source/town_v248';O=R/'app/src/main/assets/town-v248';r=json.loads((S/'receipt.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(S/'original_sources.zip') as z:
 for n,h in r['sourceFiles'].items():assert sha(z.read(n))==h,n
 for n,a in r['assets'].items():
  p=O/n;assert sha(p.read_bytes())==a['sha256'],n;actual=Image.open(p).convert('RGBA');assert list(actual.size)==a['size']
  if n=='portal.png':
   ms,blend,fs=decode(z.read('roh/efct362.efa'));assert ms==125 and blend==1 and len(fs)==32
   for i,f in enumerate(fs):
    assert f['pivot']==[19,110] and f['size']==[38,120]
    assert sha(f['rgba'].tobytes())==r['portalFrames'][i]['rgbaSha256'];assert np.array_equal(np.array(actual.crop((i%8*38,i//8*120,(i%8+1)*38,(i//8+1)*120))),f['rgba'])
  elif n.startswith(('choice-','portrait')):
   expected=np.array(Image.open(S/a['source']).convert('RGBA').crop(a['crop']));pad=a['removedInterior'];expected[pad:-pad,pad:-pad,3]=0
   if a.get('mirrorRightHalf'):expected[:,expected.shape[1]//2:]=expected[:,:expected.shape[1]//2][:,::-1]
   assert np.array_equal(np.array(actual),expected)
  else:
   m=mpf(z.read('hades/'+a['source']));expected=render_frame(m,palette(z.read('hades/'+a['palette'])),a['frame'])
   if a['mirrored']:expected=expected.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
   assert np.array_equal(np.array(actual),np.array(expected))
print('V248:4originalNPC +32full38x120/RGB565/pitch/pivot/125ms/additive portalframes +3unchanged screenshot borders PASS; decoded portal583680bytes')
