"""Audit every recovered sprite/UI/font pixel against the retained raw formats."""
from pathlib import Path
import json,hashlib,zipfile
import numpy as np
from PIL import Image
from horror_original_formats import mpf,palette,render_frame
from town_source_formats_v247 import spf
R=Path(__file__).resolve().parents[1];S=R/'master/source/town_v247';O=R/'app/src/main/assets/town-v247';ref=json.loads((S/'receipt.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(S/'original_sources.zip') as z:
 for n,h in ref['sourceFiles'].items():assert sha(z.read(n))==h,n
 for n,a in ref['assets'].items():
  p=O/n;assert sha(p.read_bytes())==a['sha256'],n;actual=Image.open(p).convert('RGBA');assert list(actual.size)==a['size']
  if 'source' in a:
   m=mpf(z.read('hades/'+a['source']));expected=render_frame(m,palette(z.read('hades/'+a['palette'])),a['frame'])
   if a['mirrored']:expected=expected.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
  elif n.startswith('font-'):
   width=16 if n=='font-ko.png' else 8;data=z.read('Legend/'+('han00.fnt' if width==16 else 'eng00.fnt'));count=len(data)//(width//8*12);expected=Image.new('RGBA',actual.size)
   for i in range(count):
    bits=np.unpackbits(np.frombuffer(data[i*(width//8)*12:(i+1)*(width//8)*12],np.uint8).reshape(12,width//8),axis=1)*255;im=Image.fromarray(np.dstack([np.full((12,width),255,np.uint8)]*3+[bits]));expected.paste(im,(i%32*width,i//32*12))
  else:
   key,index=n[:-4].rsplit('-',1);expected=spf(z.read('setoa/'+key+'.spf'))[int(index)]
  assert np.array_equal(np.array(actual),np.array(expected)),n
print('V247 all',len(ref['assets']),'assets: full source rectangles/palette/row pitch/RGB565/bitmap glyphs byte-pixel PASS')
