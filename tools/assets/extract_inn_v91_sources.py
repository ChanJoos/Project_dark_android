"""Extract reusable source furniture/rat pixels. No generated or mirrored actor art.

Input: user's 20260924_163501 recording and early building sheet. Full frames are
reference-only; the renderer loads only separate masked furniture/material crops.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import argparse, hashlib, json, subprocess

p = argparse.ArgumentParser()
p.add_argument('--video', required=True)
p.add_argument('--buildings', required=True)
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
source = root/'assets/milles/source/inn_v91'
out = root/'assets/milles/production/interiors/v91'
source.mkdir(parents=True, exist_ok=True)
out.mkdir(parents=True, exist_ok=True)
sha = lambda f: hashlib.sha256(Path(f).read_bytes()).hexdigest()
frames = {}
for t in (3, 4, 6):
    f = source/f'frame_{t}s.png'
    subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-ss',str(t),'-i',a.video,'-frames:v','1',str(f)],check=True)
    frames[t] = Image.open(f).convert('RGBA')
rows = []
def extract(name, t, box, polygon=None, rat=False):
    # Coordinates recorded on the 1170x540 inspection frame. Crop the full input
    # before nearest-neighbor reduction; source coordinates/hash remain traceable.
    im = frames[t].crop(tuple(n*2 for n in box))
    im.save(source/(name+'_unmasked.png'))
    mask = Image.new('L', im.size, 0)
    if polygon:
        ImageDraw.Draw(mask).polygon([((x-box[0])*2,(y-box[1])*2) for x,y in polygon],fill=255)
    else:
        mask.paste(255,(0,0,*im.size))
    if rat:
        # Gray fur/tail from the actual footage; brown floor is excluded. No
        # recoloring, shape synthesis, rotation, direction mirroring or new pose.
        pix=im.load();m=mask.load()
        for y in range(im.height):
            for x in range(im.width):
                r,g,b,_=pix[x,y]
                m[x,y]=255 if max(r,g,b)-min(r,g,b)<38 and min(r,g,b)>75 else 0
    if name=="inn_counter":
        ImageDraw.Draw(mask).polygon([(x*2,y*2) for x,y in [(95,0),(274,0),(210,40),(180,47)]],fill=0)
        ImageDraw.Draw(mask).rectangle((380*2,25*2,398*2,65*2),fill=0)
    im.putalpha(mask)
    im=im.resize((im.width//2,im.height//2),Image.Resampling.NEAREST)
    f=out/(name+'.png');im.save(f)
    rows.append(dict(name=name,frame=f'frame_{t}s.png',time_seconds=t,crop_xyxy=[n*2 for n in box],polygon=polygon,counter_hole_xy=([(95,0),(274,0),(210,40),(180,47)] if name=='inn_counter' else None),processing='source crop + documented alpha mask + nearest reduction 0.5',sha256=sha(f),size=list(im.size)))

extract('inn_table',3,(508,314,792,438),[(508,390),(560,365),(560,334),(600,314),(617,319),(630,315),(646,316),(647,335),(736,355),(792,375),(671,438)])
extract('inn_table_food',3,(592,163,798,278),[(592,224),(603,197),(610,194),(615,209),(652,196),(649,179),(658,177),(666,195),(694,186),(703,163),(713,166),(723,180),(736,182),(741,198),(770,190),(777,202),(775,223),(798,239),(749,278),(625,251)])
extract('inn_hearth',3,(918,15,1033,135),[(924,43),(989,15),(1006,15),(1009,61),(1033,74),(1033,110),(974,135),(918,115),(918,58)])
extract('inn_counter',3,(480,0,878,131),[(480,0),(675,0),(710,18),(778,0),(878,0),(878,33),(666,131),(480,69)])
extract('inn_wall',3,(205,40,278,126),[(205,75),(278,40),(278,91),(205,126)])
extract('inn_door',3,(276,32,346,127),[(276,69),(346,32),(346,87),(276,127)])
extract('inn_planter',3,(852,25,906,82),[(853,40),(860,28),(881,25),(900,36),(906,53),(895,65),(886,81),(871,76),(865,60)])
extract('mouse_se',3,(307,140,349,159),rat=True)
extract('mouse_nw',3,(182,346,226,364),rat=True)
extract('mouse_ne',3,(155,170,183,186),rat=True)
# Floor is a material sample, not a screen/map plate.
extract('inn_floor',3,(802,292,930,356))

sheet=Image.open(a.buildings).convert('RGBA')
box=(1104,62,1520,408)
building=sheet.crop(box)
poly=[(1190,70),(1390,146),(1390,130),(1408,129),(1437,145),(1434,195),(1498,253),(1505,278),(1487,281),(1503,316),(1503,354),(1450,401),(1280,405),(1120,342),(1120,252),(1107,241),(1113,230),(1145,211),(1110,198),(1115,186),(1177,113),(1183,70)]
mask=Image.new('L',building.size,0)
ImageDraw.Draw(mask).polygon([(x-box[0],y-box[1]) for x,y in poly],fill=255)
building.putalpha(mask)
f=root/'assets/milles/production/buildings/BLD_006_inn_early.png';building.save(f)
source.joinpath('early_buildings.png').write_bytes(Path(a.buildings).read_bytes())
manifest=dict(schema='inn-source-crops-v91',video_name=Path(a.video).name,video_sha256=sha(a.video),video_library_id='libfile_94b8225a622881918893269e3b60de9e',building_library_id='libfile_90d38620534c8191ace7a6af6d3bd0db',building_source_sha256=sha(a.buildings),building_crop=list(box),building_sha256=sha(f),assets=rows,mouse_note='Three observed source silhouettes only. SW retains the observed west-facing pose; missing original direction/action frames are not fabricated. Runtime uses shared AI and source pose translation, not invented original animation.')
(out/'sources.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
