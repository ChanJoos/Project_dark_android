"""Install generated Abel layout and known runtime integration; requires local extracted assets."""
from pathlib import Path
import json,shutil
root=Path(__file__).resolve().parents[1];java=root/'app/src/main/java/com/projectdark/mobile';data=json.loads((root/'app/src/main/assets/abel-video-v147/layout.json').read_text())
lines=['package com.projectdark.mobile.world;','import java.util.*;','public final class AbelVideoLayout {','public static final float SCALE=8f/7f;']
for scene in ['main','east']:
 s=data['scenes'][scene];lines.append('public static final String[] '+scene.upper()+'_TILES={'+','.join(json.dumps(r) for r in s['gridRows'])+'};');lines.append('public static List<AbelTownGeometry.Prop> '+scene+'Props(){List<AbelTownGeometry.Prop> p=new ArrayList<>();')
 for p in s['props']:
  vals=p['sourceWorld']+p['anchor']+p['nativeSize'];lines.append('p.add(new AbelTownGeometry.Prop('+json.dumps(p['asset'])+','+','.join(f'{float(v):.3f}f' for v in vals)+'));')
 lines.append('return Collections.unmodifiableList(p);}')
lines.append('public static final float[][] INN_PROPS={'+','.join('{'+','.join(f'{float(v):.3f}f' for v in p['sourceWorld'])+'}' for p in data['scenes']['inn']['props'])+'};')
lines.append('public static final String[] INN_ASSETS={'+','.join(json.dumps(p['asset']) for p in data['scenes']['inn']['props'])+'};')
lines.append('private AbelVideoLayout(){} }');(java/'world/AbelVideoLayout.java').write_text('\n'.join(lines)+'\n')
for name in ['AbelTownGeometry.java','AbelTownRenderer.java']:shutil.copyfile(root/'tools/abel_v147_templates'/name,java/'world'/name)
p=java/'world/CampaignWorld.java';s=p.read_text().replace('AbelTownGeometry.ID,AbelCoastGeometry.ENTRY','AbelTownGeometry.ID,AbelTownGeometry.EAST_ID,AbelCoastGeometry.ENTRY').replace('AbelTownGeometry.ID.equals(id)','AbelTownGeometry.contains(id)');p.write_text(s)
p=java/'world/PoteCampaignMapDef.java';s=p.read_text().replace('AbelTownGeometry.ID.equals(id)','AbelTownGeometry.contains(id)').replace('AbelTownGeometry.obstacles()','AbelTownGeometry.obstacles(id)').replace('AbelTownGeometry.navigation()','AbelTownGeometry.navigation(id)');old='if(AbelTownGeometry.contains(id))return new PoteCampaignMapDef(id,9,"아벨 마을",64,3008,64,1536,AbelTownGeometry.x(39,39),AbelTownGeometry.y(39,39),AbelTownGeometry.x(42,42),AbelTownGeometry.y(42,42),AbelTownGeometry.x(42,17),AbelTownGeometry.y(42,17),40);';new='if(AbelTownGeometry.contains(id)){boolean east=AbelTownGeometry.EAST_ID.equals(id);int eu=east?20:21,ev=east?57:10,bu=east?22:23,bv=east?55:10,nu=east?55:15,nv=east?33:67;return new PoteCampaignMapDef(id,9,east?"아벨 동마을":"아벨 마을",0,5120,0,2688,AbelTownGeometry.x(eu,ev),AbelTownGeometry.y(eu,ev),AbelTownGeometry.x(bu,bv),AbelTownGeometry.y(bu,bv),AbelTownGeometry.x(nu,nv),AbelTownGeometry.y(nu,nv),40);}';assert old in s or new in s;s=s.replace(old,new);p.write_text(s)
p=java/'GameView.java';s=p.read_text().replace('if(com.projectdark.mobile.world.AbelTownGeometry.ID.equals(state.currentMapId()))abelTownRenderer.draw','if(com.projectdark.mobile.world.AbelTownGeometry.contains(state.currentMapId()))abelTownRenderer.draw');p.write_text(s)
p=java/'RuntimeState.java';s=p.read_text().replace('AbelTownGeometry.x(38,38)','AbelTownGeometry.x(23,10)').replace('AbelTownGeometry.y(38,38)','AbelTownGeometry.y(23,10)').replace('if(com.projectdark.mobile.world.AbelTownGeometry.ID.equals(id)){npcs.add','if(com.projectdark.mobile.world.AbelTownGeometry.contains(id)){if(com.projectdark.mobile.world.AbelTownGeometry.ID.equals(id))npcs.add');p.write_text(s)
print('V147 runtime integration installed; native/build/device verification still required')

p=java/'world/TownInteriorDef.java';s=p.read_text();marker='  props=Collections.unmodifiableList(p);';addon='if(k==Kind.INN&&id.startsWith("abel_interior_")){p.clear();for(int i=0;i<AbelVideoLayout.INN_ASSETS.length;i++){String asset=AbelVideoLayout.INN_ASSETS[i];float wx=128+AbelVideoLayout.INN_PROPS[i][0]*AbelVideoLayout.SCALE,wy=132+AbelVideoLayout.INN_PROPS[i][1]*AbelVideoLayout.SCALE;int tu=Math.max(1,Math.min(columns-2,Math.round(((wx-480)/32+(wy-132)/16)/2))),tv=Math.max(1,Math.min(rows-2,Math.round(((wy-132)/16-(wx-480)/32)/2)));boolean table=asset.startsWith("inn_table");p.add(new Prop("abel_video:"+asset,tu,tv,table?120:32,0,table,table?2:1,table?2:1));}}';
if addon not in s:s=s.replace(marker,addon+'\n'+marker)
p.write_text(s)
p=java/'world/TownInteriorRenderer.java';s=p.read_text();methods=(root/'tools/abel_v147_templates/InnSourceMethods.txt').read_text()
if 'private final Context sourceContext' not in s:
 s=s.replace(' private final InnFurnitureRenderer inn;',' private final Context sourceContext;private final Map<String,Bitmap> sourceImages=new HashMap<>();private final Map<String,JSONObject> sourceInn=new HashMap<>();\n private final InnFurnitureRenderer inn;')
 s=s.replace('public TownInteriorRenderer(Context c){','public TownInteriorRenderer(Context c){sourceContext=c;')
 s=s.replace('public void ground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){','public void ground(Canvas c,WorldRuntimeAdapter w,TownInteriorDef d){if(d.isAbel()&&d.kind==TownInteriorDef.Kind.INN){sourceInnGround(c,w,d);return;}')
 s=s.replace('public void prop(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){','public void prop(Canvas c,WorldRuntimeAdapter w,TownInteriorDef.Prop o){if(o.asset.startsWith("abel_video:")){sourceInnProp(c,w,o.asset.substring(11));return;}')
 s=s.replace('public boolean hasProp(String name){return ','public boolean hasProp(String name){return name.startsWith("abel_video:")||')
 pos=s.rfind('}');s=s[:pos]+methods+'\n'+s[pos:]
p.write_text(s)
