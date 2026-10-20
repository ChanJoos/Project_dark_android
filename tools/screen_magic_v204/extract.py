from pathlib import Path
from PIL import Image,ImageSequence
import numpy as np,json,hashlib,shutil
root=Path(__file__).resolve().parents[2];src=root/'master/source/screen_magic_v204';out=root/'app/src/main/assets/skill-presentation/screen-magic-v204'
gif=Image.open(src/'meteor-original.gif');base=np.array(gif.convert('RGB')).astype(np.int16)
frames=[];durations=[]
for i,f in enumerate(ImageSequence.Iterator(gif)):
 if i in (0,8):continue # source empty lead/tail are not impact frames
 a=np.array(f.convert('RGB')).astype(np.int16);diff=np.max(np.abs(a-base),axis=2);alpha=np.clip((diff-20)/24,0,1)*255
 rgba=np.dstack([a.astype(np.uint8),alpha.astype(np.uint8)]);im=Image.fromarray(rgba,'RGBA').crop((0,0,294,235));frames.append(im);durations.append(f.info.get('duration',100))
atlas=Image.new('RGBA',(294*4,235*2))
for i,im in enumerate(frames):atlas.paste(im,(i%4*294,i//4*235))
atlas.save(out/'meteor.png')
meta=dict(path='screen-magic-v204/meteor.png',width=294,height=235,columns=4,durationsMs=durations,pivotX=151,pivotY=173,scale=1,blend='NORMAL',atlasSha256=hashlib.sha256((out/'meteor.png').read_bytes()).hexdigest())
(out/'meteor.json').write_text(json.dumps(meta,indent=2)+'\n')
shutil.copyfile(src/'meteor-icon-crop.png',out/'meteor-icon.png')
