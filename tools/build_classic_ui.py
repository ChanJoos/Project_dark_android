"""Extract the user's original client skin; never paint a replacement illustration."""
from pathlib import Path
from PIL import Image
import hashlib,json
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'master/source/ui/classic_20261009'
OUT=ROOT/'app/src/main/assets/classic-ui'
def main():
 OUT.mkdir(parents=True,exist_ok=True)
 source=SRC/'inventory_weapon.jpg';im=Image.open(source).convert('RGBA')
 # Empty third-row socket and undecorated frame strips. No item/text pixels.
 boxes={'paper':(308,948,330,969),'cell':(298,946,331,975),
        'top':(120,855,574,864),'bottom':(120,978,574,987),
        'left':(84,864,94,976),'right':(577,864,587,976),
        'corner_tl':(84,855,96,867),'corner_tr':(575,855,587,867),
        'corner_bl':(84,975,96,987),'corner_br':(575,975,587,987),
        'header':(349,841,567,850)}
 manifest={'revision':'CLASSIC_UI_V126','source':str(source.relative_to(ROOT)),
           'sourceSha256':hashlib.sha256(source.read_bytes()).hexdigest(),
           'policy':'Exact source crops. Runtime repeats interior texture and stretches only empty border strips. Icons are separate RGBA layers.', 'crops':{}}
 for key,box in boxes.items():
  path=OUT/(key+'.png');im.crop(box).save(path)
  manifest['crops'][key]={'path':'classic-ui/'+path.name,'box':list(box),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
 (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
 print('CLASSIC_SOURCE_SKIN',len(boxes))
if __name__=='__main__':main()
