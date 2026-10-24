"""Package reviewed immutable Rapid sources without inventing item/BODY bindings."""
from pathlib import Path
import zipfile,json,io
from PIL import Image
root=Path(__file__).resolve().parents[1]
src=root/"master/source/items/rapid_v198"
with zipfile.ZipFile(src/"original_sources.zip") as original,zipfile.ZipFile(src/"generated_assets.zip","w",zipfile.ZIP_DEFLATED) as out:
 for name in sorted(original.namelist()):
  if not name.endswith(".json") or name=="receipt.json":continue
  raw=original.read(name);j=json.loads(raw);identity=Path(name).stem;atlas=original.read(j["a"]);image=Image.open(io.BytesIO(atlas))
  for group,frames in j["sprites"].items():
   assert len({f["f"] for f in frames})==len(frames)
   for f in frames:assert f["sx"]>=0 and f["sy"]>=0 and f["sx"]+f["w"]<=image.width and f["sy"]+f["h"]<=image.height
  out.writestr("source-registration/"+identity+".json",raw);out.writestr("wearable-atlases/"+identity+".webp",atlas)
with zipfile.ZipFile(src/"generated_assets.zip") as out:assert len(out.namelist())==32 and out.testzip() is None
