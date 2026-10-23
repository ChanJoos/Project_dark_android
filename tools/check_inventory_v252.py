"""Independent complete-frame/source-hash/binding audit; no screenshot color masking."""
from pathlib import Path
import json,zipfile,hashlib,sys
import numpy as np
from PIL import Image
from horror_original_formats import palette
from build_mage_v241 import epf
R=Path(__file__).resolve().parents[1];S=R/'master/source/items/inventory_v252';O=R/'app/src/main/assets/inventory-v252'
receipt=json.loads((S/'receipt.json').read_text());manifest=json.loads((O/'manifest.json').read_text());sheet=np.array(Image.open(O/'icons.png').convert('RGBA'));assert len(manifest)==receipt['bindingCount']==279
with zipfile.ZipFile(S/'original_inventory.zip') as z:
 assert z.testzip() is None
 for row in receipt['items']:
  name,index,paln,_,_=row['native'];raw=z.read(name);pal=z.read(f'item{paln:03}.pal');assert hashlib.sha256(raw).hexdigest()==row['nativeEpfSha256'];assert hashlib.sha256(pal).hexdigest()==row['nativePaletteSha256']
  _,_,frames=epf(raw);x,y,indices=frames[index];a=palette(pal)[indices];assert [x,y]==row['nativeOrigin'];assert hashlib.sha256(a.tobytes()).hexdigest()==row['rgbaSha256'];cell=manifest[row['itemId']];assert np.array_equal(sheet[cell['y']:cell['y']+cell['h'],cell['x']:cell['x']+cell['w']],a),row['itemId']
  if 'referenceArchive' in row:
   with zipfile.ZipFile(R/row['referenceArchive']) as ref:assert hashlib.sha256(ref.read(row['referenceMember'])).hexdigest()==row['referenceSha256']
  if row['evidence']=='EXACT_NAMED_TABLE_INVENTORY_ICON_AND_CLIENT_PIXELS':
   with zipfile.ZipFile(R/row['referenceArchive']) as ref:
    import io
    b=np.array(Image.open(io.BytesIO(ref.read(row['referenceMember']))).convert('RGB').crop(row['referenceCrop']));mask=a[:,:,3]>0
    assert b.shape[:2]==a.shape[:2];error=float(np.abs(b.astype(int)-a[:,:,:3].astype(int))[mask].mean());assert error<=3 and abs(error-row['comparisonMeanRgbError'])<.001,row['itemId']
assert {'USER_ITEM_20261018_R01881','USER_ITEM_20261018_R05502','IT_REAGENT_CURANUM','IT_REAGENT_KOMADIUM'}<=manifest.keys()
print('V252 original279 complete-frame RGBA, archive/source hashes, named table comparisons and atlas bounds PASS')
