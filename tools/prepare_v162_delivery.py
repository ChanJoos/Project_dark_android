"""Reproduce the V162 delivery from already-tested exact Actions artifacts; no runtime rebuild."""
from pathlib import Path
import hashlib,json,zipfile,os,time,urllib.request,xml.etree.ElementTree as ET,re,shutil
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(".");OUT=Path("v162-delivery");OUT.mkdir(exist_ok=True)
SOURCE="6f3ac7b900299330b7e15c1d374f3d56048c86db"
EXPECTED_APK="874cef5f2b2046fc915a90c9577c1228258dfeebc023d8836189d788388bc8a0"
REPO="ChanJoos/Project_dark_android"
def api(path):
    req=urllib.request.Request("https://api.github.com/repos/"+REPO+path,headers={"Authorization":"Bearer "+os.environ["GH_TOKEN"],"Accept":"application/vnd.github+json"})
    with urllib.request.urlopen(req,timeout=30) as r:return json.load(r)
apkroot=Path("input-apk");native=Path("input-native")
apk=next(apkroot.rglob("app-debug.apk"));info=json.loads(next(apkroot.rglob("BUILD_INFO.json")).read_text())
assert info["sourceCommit"]==SOURCE and str(info["runId"])=="38664473610"
assert info["versionCode"]==162 and info["applicationId"]=="com.projectdark.mobile.v162test"
digest=hashlib.sha256(apk.read_bytes()).hexdigest()
assert digest==EXPECTED_APK==info["apkSha256"] and apk.stat().st_size==80462485==info["apkBytes"]
with zipfile.ZipFile(apk) as z:
    assert z.testzip() is None
    ident=json.loads(z.read("assets/build_identity.json"));assert ident["sourceCommit"]==SOURCE
    checked=0
    for root in ["app/src/main/assets","assets/milles/production","assets/pote/production"]:
        for p in Path(root).rglob("*"):
            if not p.is_file() or p.name.startswith("."):continue
            name="assets/"+p.relative_to(root).as_posix()
            if name in z.namelist():assert z.read(name)==p.read_bytes(),name;checked+=1
    for family in ["abel_v139","abel_v140","abel_v141","abel_v142","rucesion_v158"]:
        with zipfile.ZipFile(Path("master/assets")/family/"generated_assets.zip") as pack:
            for name in pack.namelist():
                if name.endswith("/") or any(part in name for part in ["/red_octopus/","/crab/","/berserker/","abel_v140/pong/"]):continue
                assert z.read("assets/"+name)==pack.read(name),name;checked+=1
    with zipfile.ZipFile("app/build/generated/abel-v161-pack/generated_assets.zip") as pack:
        assert len(pack.namelist())==84
        for name in pack.namelist():assert z.read("assets/"+name)==pack.read(name),name;checked+=1
assert checked>1000
counts=dict(tests=0,failures=0,errors=0,skipped=0,suites=0)
for p in native.rglob("TEST-*.xml"):
    e=ET.parse(p).getroot();counts["suites"]+=1
    for k in ["tests","failures","errors","skipped"]:counts[k]+=int(e.attrib.get(k,0))
assert counts==dict(tests=128,failures=0,errors=0,skipped=0,suites=19),counts
profile=json.loads(next(native.rglob("performance.json")).read_text())
shutil.copyfile(apk,OUT/"PROJECT_DARK_V162_PONG_CURSE.apk")
folder=next(native.rglob("curse-v162"))
def sprite(name):return Image.open(folder/(name+".png")).convert("RGBA")
font="app/src/main/assets/ui-fonts/Pretendard-Regular.otf"
f=ImageFont.truetype(font,24);title=ImageFont.truetype(font,32);small=ImageFont.truetype(font,18)
def put(im,dest,box):
    bounds=im.getbbox();im=im.crop(bounds) if bounds else im
    k=min(box[2]/im.width,box[3]/im.height);im=im.resize((round(im.width*k),round(im.height*k)),Image.Resampling.NEAREST)
    dest.paste(im,(box[0]+(box[2]-im.width)//2,box[1]+box[3]-im.height),im)
b=Image.new("RGB",(1350,700),(244,239,228));d=ImageDraw.Draw(b)
d.text((25,20),"퐁퐁이 정상 색상 · 저주 적용과 해제",font=title,fill="black")
d.text((25,75),"원본은 색상 확인용 확대 / 게임 3장은 동일 배율의 실제 Android 렌더",font=small,fill="black")
for x,t in [(45,"원본 정상"),(315,"원본 흐림"),(585,"게임 정상"),(855,"저주 적용"),(1125,"저주 해제")]:d.text((x,140),t,font=f,fill="black")
for x,n in [(20,"pong-normal-9_5s.png"),(290,"pong-faded-16s.png")]:
    im=Image.open(Path("master/source/abel_v162")/n).convert("RGB").resize((240,190),Image.Resampling.NEAREST);b.paste(im,(x,260))
for x,n in [(550,"pong-normal"),(820,"wizard-002"),(1090,"pong-dispelled")]:put(sprite(n),b,(x,230,240,240))
d.text((30,540),"저주 상태가 실제로 적용된 동안만 색이 흐려집니다.",font=f,fill="black")
d.text((30,590),"렌토·바르도·데프레코·프라보 및 광역 계열 / 만료·해제·재생성 시 정상 복원",font=small,fill="black")
d.text((30,625),"관측 색상 기반 보정입니다. 원본의 정확한 팔레트 인덱스·변환식 및 휴대폰 색감 확인은 별도입니다.",font=small,fill="black")
b.save(OUT/"V162_PONG_NORMAL_CURSE_RESTORE.png")
ids=sorted(p.name[:-11] for p in folder.glob("*-normal.png") if p.name.startswith(("ABEL_","LYK_")));assert len(ids)==23
names=dict(re.findall(r'case "([A-Z_]+)":return "([^";]+)"',Path("app/src/main/java/com/projectdark/mobile/AbelCoastMonsters.java").read_text()))
ruc=Path("app/src/main/java/com/projectdark/mobile/RucesionCoastMonsters.java").read_text()
rids=re.findall(r'"(LYK_[A-Z_]+)"',ruc.split("private static final")[0]);rnames=re.findall(r'"([^";]+)"',ruc.split("NAMES={")[1].split("};")[0]);names.update(zip(rids,rnames))
b=Image.new("RGB",(1060,130+len(ids)*300),(244,239,228));d=ImageDraw.Draw(b)
d.text((25,18),"23종 정상 / 저주 · 실제 Android 렌더",font=title,fill="black")
d.text((390,78),"정상",font=f,fill="black");d.text((770,78),"저주 적용",font=f,fill="black")
for i,key in enumerate(ids):
    y=130+i*300;d.rectangle((10,y,1050,y+292),fill=(232,221,199));d.text((20,y+15),names.get(key,key),font=f,fill="black")
    d.text((20,y+53),key,font=ImageFont.truetype(font,12),fill="black")
    for x,state in [(300,"normal"),(680,"curse")]:
        im=sprite(key+"-"+state).resize((360,330),Image.Resampling.NEAREST);b.paste(im,(x,y-35),im)
b.save(OUT/"V162_MONSTERS_NORMAL_CURSE.png")
for name in ["actual-inn-normal","actual-inn-curse","actual-inn-dispelled"]:assert (folder/(name+".png")).exists()
# All three are the actual GameView room path, beyond isolated sprite tests.
b=Image.new("RGB",(1440,320),(244,239,228));d=ImageDraw.Draw(b)
for i,(name,label) in enumerate([("actual-inn-normal","여관 정상"),("actual-inn-curse","여관 저주 적용"),("actual-inn-dispelled","여관 저주 해제")]):
    im=sprite(name).resize((480,270),Image.Resampling.NEAREST);b.paste(im,(480*i,45),im);d.text((480*i+20,10),label,font=f,fill="black")
b.save(OUT/"V162_INN_NORMAL_CURSE_RESTORE.png")
for _ in range(45):
    common=api("/actions/runs/38664479830")
    if common["status"]=="completed":break
    print("Waiting for exact-source common Android regression",flush=True);time.sleep(20)
else:raise RuntimeError("Common source verification still pending")
assert common["head_sha"]==SOURCE and common["conclusion"]=="success",common
artifacts=api("/actions/runs/38664473610/artifacts")["artifacts"]
assert {a["id"] for a in artifacts}=={11940488434,11940239101}
assert all(a["workflow_run"]["head_sha"]==SOURCE for a in artifacts)
proof={"status":"IMPLEMENTED_BUILD_NATIVE_VERIFIED","source":SOURCE,"apk":info,"nativeTests":counts,"packagedSourceFilesChecked":checked,"hostPerformance":profile,
 "github":{"focusedRun":38664473610,"focusedJob":116193095024,"commonPR":38664479830,"commonPRJob":116193114075,"commonPRConclusion":common["conclusion"],"artifactIds":[a["id"] for a in artifacts],"artifactDigests":{a["name"]:a["digest"] for a in artifacts}},
 "physicalPhoneAcceptance":"PENDING","userVisualAcceptance":"PENDING","sourceArchive":"V16184 unchanged frames; normal/faded raw video crops and independent material samples in master/source/abel_v162.",
 "scope":"23 coast species x12poses,10catalog curses, hit overlap, expiry/dispel/respawn/fresh-map restoration; standalone mouse and actual GameView inn scene. Pong original sampled normal+blue curse materials; other species generic fading. Exact original indexed palette/curve unavailable.",
 "delivery":"Prepared on GitHub Actions from exact original tested artifacts because local file executor stopped responding; runtime code was not rebuilt or changed."}
proof["files"]={p.name:{"bytes":p.stat().st_size,"sha256":hashlib.sha256(p.read_bytes()).hexdigest()} for p in OUT.iterdir() if p.is_file()}
(OUT/"V162_VERIFICATION.json").write_text(json.dumps(proof,ensure_ascii=False,indent=2)+"\n")
print("V162_DELIVERY_VERIFICATION="+json.dumps(proof,ensure_ascii=False),flush=True)
