from pathlib import Path
from PIL import Image,ImageFilter
import numpy as np,json,hashlib
root=Path(__file__).resolve().parents[2];src=root/'master/source/screen_magic_v204';out=root/'app/src/main/assets/skill-presentation/screen-magic-v204'
im=Image.open(src/'curse-original-pink-crop.png').convert('RGB');a=np.array(im).astype(float);r,g,b=a[:,:,0],a[:,:,1],a[:,:,2]
chroma=np.clip((np.minimum(r-g,b-g)-18)/42,0,1)
white=np.clip((np.minimum(np.minimum(r,g),b)-155)/65,0,1)
alpha=np.maximum(chroma,white)
rgba=np.dstack([a.astype(np.uint8),(alpha*255).astype(np.uint8)])
clean=Image.fromarray(rgba,'RGBA').resize((90,120),Image.Resampling.LANCZOS)
# Filter compression noise at native sprite scale; retain observed silhouette and internal bone structure.
rgb=clean.convert('RGB').filter(ImageFilter.UnsharpMask(radius=.5,percent=110,threshold=3));rgb.putalpha(clean.getchannel('A'));clean=rgb
# Source pink family, mapped to the four user-labelled palettes. White bone cores stay unchanged.
for name,shift in [('blue',-58),('yellow',71),('pink',0),('red',28)]:
 hsv=np.array(clean.convert('RGB').convert('HSV')).astype(np.int16);hsv[:,:,0]=(hsv[:,:,0]+shift)%256
 col=Image.fromarray(hsv.astype('uint8'),'HSV').convert('RGB');col.putalpha(clean.getchannel('A'))
 atlas=Image.new('RGBA',(360,120))
 for f,gain in enumerate([.65,1,1,.35]):
  frame=col.copy();frame.putalpha(frame.getchannel('A').point(lambda a:int(a*gain)));atlas.paste(frame,(f*90,0))
 atlas.save(out/('curse-'+name+'.png'))
 meta=dict(path='screen-magic-v204/curse-'+name+'.png',width=90,height=120,columns=4,durationsMs=[120,200,320,240],pivotX=45,pivotY=60,scale=1,blend='NORMAL',atlasSha256=hashlib.sha256((out/('curse-'+name+'.png')).read_bytes()).hexdigest())
 (out/('curse-'+name+'.json')).write_text(json.dumps(meta,indent=2)+'\n')
