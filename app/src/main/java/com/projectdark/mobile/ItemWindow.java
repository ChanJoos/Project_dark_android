package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;
import org.json.*;

/** Reference window rendering/input geometry; no inventory, equipment or stat authority. */
final class ItemWindow {
  interface Visuals { void icon(Canvas c,RpgInventoryPresentation.ItemRow row,float x,float y,float size); void actor(Canvas c,float x,float y); }
  enum Hit { NONE,CLOSE,SELECT,ACTION,DETAILS,CONSUMED }
  static final int PAGE_SIZE=50;
  static final String[] SLOTS={"귀걸이","목걸이","갑옷","모자","날개","무기","방패","장갑","벨트","각반","신발"};
  static final float[][] POS={{252,134},{314,134},{252,198},{375,94},{438,134},{314,262},{438,262},{252,326},{438,390},{314,390},{375,390}};
  final Map<String,Bitmap> images=new HashMap<>();
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  final RpgInventoryPresentation presentation=new RpgInventoryPresentation();
  int page,filter;boolean details;String selectedSlot,hitItem;
  ItemWindow(Context context){try{JSONObject m=new JSONObject(new String(PresentationAssetBytes.read(context,"item-window/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("crops");Iterator<String> keys=m.keys();while(keys.hasNext()){String k=keys.next();Bitmap b=BitmapFactory.decodeStream(context.getAssets().open(m.getJSONObject(k).getString("path")));if(b==null)throw new IllegalStateException(k);images.put(k,b);}}catch(Exception e){throw new IllegalStateException("Item window skin unavailable",e);}}
  static RectF cell(int i){float x=344+(i%10)*58,y=118+(i/10)*65;return new RectF(x,y,x+55,y+60);}
  static RectF slot(int i){return new RectF(POS[i][0],POS[i][1],POS[i][0]+55,POS[i][1]+55);}
  List<RpgInventoryPresentation.ItemRow> rows(RpgProgressionState r){List<RpgInventoryPresentation.ItemRow> out=new ArrayList<>();for(RpgInventoryPresentation.ItemRow row:presentation.inventoryRows(r))if(filter==0||filter==1&&row.equipSlot!=null&&!row.equipSlot.isEmpty()||filter==2&&r.isConsumable(row.itemId))out.add(row);return out;}
  void image(Canvas c,String key,RectF r){c.drawBitmap(images.get(key),null,r,p);}
  void txt(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);c.drawText(s,x,y,p);}
  void fit(Canvas c,String s,float x,float y,float w,float size,int color){p.setTextSize(size);while(p.measureText(s)>w&&size>7){size-=.5;p.setTextSize(size);}if(p.measureText(s)>w){while(s.length()>1&&p.measureText(s+"…")>w)s=s.substring(0,s.length()-1);s+="…";}txt(c,s,x,y,size,color);}
  void border(Canvas c,RectF r,int color){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(color);c.drawRect(r,p);p.setStyle(Paint.Style.FILL);}
  void panel(Canvas c,RectF r,String title){image(c,"detail",r);image(c,"top",new RectF(r.left,r.top,r.right,r.top+36));image(c,"left",new RectF(r.left,r.top,r.left+5,r.bottom));image(c,"bottom-detail",new RectF(r.left,r.bottom-5,r.right,r.bottom));border(c,r,0xff77664a);p.setTextSize(17);txt(c,title,r.centerX()-p.measureText(title)/2,r.top+25,17,0xffddd2b5);}
  void button(Canvas c,RectF r,String s){p.setColor(0xff393026);c.drawRect(r,p);border(c,r,0xff9d8259);p.setTextSize(12);txt(c,s,r.centerX()-p.measureText(s)/2,r.centerY()+4,12,0xffe9dcbb);}
  void inventory(Canvas c,RpgProgressionState r,String selected,Visuals v){
    panel(c,new RectF(328,48,940,520),"인벤토리");txt(c,"×",909,75,22,0xffc5a86b);
    List<RpgInventoryPresentation.ItemRow> rows=rows(r);int pages=Math.max(1,(rows.size()+49)/50);page=Math.max(0,Math.min(page,pages-1));
    String[] keys={"all","gear","use"},labels={"전체","장비","소비"};for(int i=0;i<3;i++){image(c,keys[i],new RectF(344+i*70,83,371+i*70,108));txt(c,labels[i],373+i*70,101,10,filter==i?0xffe2c78b:0xff888478);if(filter==i)border(c,new RectF(342+i*70,80,409+i*70,110),0xffb59b66);}
    txt(c,"보유 "+presentation.inventoryRows(r).size()+"종",791,102,11,0xffe0d8c5);
    for(int i=0;i<50;i++){RectF b=cell(i);image(c,"cell",b);border(c,b,0xff514638);int index=page*50+i;if(index>=rows.size())continue;RpgInventoryPresentation.ItemRow row=rows.get(index);v.icon(c,row,b.left+5,b.top+4,45);if(row.quantity>1){String q=row.quantity>9999?"9999+":""+row.quantity;p.setTextSize(11);txt(c,q,b.right-4-p.measureText(q),b.bottom-5,11,0xfffaf5e3);}if(row.equipped)txt(c,"✓",b.right-14,b.top+14,13,0xffe2bd6a);if(row.itemId.equals(selected)&&details)border(c,b,0xffe9c367);}
    button(c,new RectF(344,463,400,499),"‹");button(c,new RectF(866,463,923,499),"›");txt(c,(page+1)+" / "+pages,609,486,12,0xffc9c1ab);txt(c,"Gold  "+r.gold(),700,511,10,0xffc9ac67);
    RpgInventoryPresentation.ItemRow selectedRow=find(r,selected);if(details&&selectedRow!=null){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(selected);if(d.equippable()){card(c,r,r.equippedDefinition(d.equipSlot),null,new RectF(40,228,467,517),"현재 장착",v);card(c,r,d,r.equippedDefinition(d.equipSlot),new RectF(479,228,936,517),"선택 장비",v);}else card(c,r,d,null,new RectF(479,228,936,517),"아이템 정보",v);button(c,new RectF(743,476,893,507),r.isConsumable(selected)?"사용":selectedRow.equipped?"해제":"장착");button(c,new RectF(898,234,929,264),"×");}
  }
  RpgInventoryPresentation.ItemRow find(RpgProgressionState r,String id){for(RpgInventoryPresentation.ItemRow row:presentation.inventoryRows(r))if(row.itemId.equals(id))return row;return null;}
  void card(Canvas c,RpgProgressionState r,RpgProgressionState.ItemDefinition d,RpgProgressionState.ItemDefinition old,RectF b,String title,Visuals v){
    panel(c,b,title);if(d==null){txt(c,"장착한 장비가 없습니다",b.left+26,b.top+95,13,0xffb7b2a4);return;}
    RpgInventoryPresentation.ItemRow row=find(r,d.itemId);if(row!=null)v.icon(c,row,b.left+15,b.top+42,48);fit(c,RpgInventoryPresentation.displayName(d.name),b.left+76,b.top+56,b.width()-115,15,0xffe4dccb);
    String job=!d.jobRestrictionResolved?"직업 조건 확인 중":d.allowedJobCodes.isEmpty()?"공통":String.join(" · ",d.allowedJobCodes);fit(c,job,b.left+76,b.top+76,b.width()-92,11,0xffd5bd7c);
    txt(c,d.requiredLevel==null?"레벨 조건 확인 중":"Lv."+d.requiredLevel+" 이상 장착 가능",b.left+76,b.top+96,12,0xffd0ccbf);if(row!=null)txt(c,presentation.requirementLabel(row),b.left+76,b.top+115,11,0xffc4b79c);
    txt(c,d.equippable()?"기본 옵션":"보유 수량  "+(row==null?0:row.quantity),b.left+16,b.top+145,12,0xffd8ccb3);
    RectF opts=new RectF(b.left+14,b.top+156,b.right-14,b.bottom-48);border(c,opts,0xff6c5c43);int n=0;
    String[] keys={"AC","HIT","DAM","MAGIC_DEFENSE","HP","MP","STR","INT","WIS","CON","DEX"};
    for(String key:keys){int value=mod(d,key),prior=mod(old,key);if(value==0&&prior==0)continue;float yy=opts.top+14+(n%6)*12,xx=opts.left+(n/6)*opts.width()/2;txt(c,key.equals("MAGIC_DEFENSE")?"마법방어":key,xx+8,yy,10,0xffdcd8cd);txt(c,""+value,xx+81,yy,11,0xffebe5d8);int delta=value-prior;if(old!=null&&delta!=0){boolean good=key.equals("AC")?delta<0:delta>0;txt(c,(good?"▲ ":"▼ ")+Math.abs(delta),xx+115,yy,10,good?0xff6dda59:0xffed8074);}n++;}if(n==0)txt(c,"추가 능력치 없음",opts.left+9,opts.top+24,12,0xffaaa696);
  }
  static int mod(RpgProgressionState.ItemDefinition d,String k){return d==null?0:d.statModifiers.getOrDefault(k,0);}
  void equipment(Canvas c,RpgProgressionState r,String selected,Visuals v){
    panel(c,new RectF(224,48,511,519),"내 정보");txt(c,"×",487,74,22,0xffc5a86b);txt(c,"캐릭터 정보",321,90,12,0xffd2c4a8);
    for(int i=0;i<SLOTS.length;i++){RectF b=slot(i);image(c,"paper",b);image(c,"slot",b);String id=r.equipment().get(SLOTS[i]);RpgInventoryPresentation.ItemRow row=find(r,id);if(row!=null){v.icon(c,row,b.left+5,b.top+4,45);txt(c,"✓",b.right-14,b.top+14,13,0xffdabc78);}else fit(c,SLOTS[i],b.left+7,b.centerY()+4,43,10,0xff706b5c);if(SLOTS[i].equals(selectedSlot))border(c,b,0xffeac873);}
    v.actor(c,405,315);txt(c,"평민 · Lv."+r.normalLevel(),349,353,10,0xffd7cab0);
    // Independent current stats are read from the established FinalStats authority.
    FinalStats f=r.finalStats();String[] keys={"STR","INT","WIS","CON","DEX"};int[] total={f.str,f.intel,f.wis,f.con,f.dex},base={r.str(),r.intel(),r.wis(),r.con(),r.dex()};
    for(int i=0;i<5;i++){float x=i<3?242:379,y=459+(i<3?i:i-3)*18;txt(c,keys[i],x,y,10,0xffcaba97);txt(c,total[i]+" ("+(total[i]-base[i])+")",x+39,y,11,0xffece5d3);}
    button(c,new RectF(533,74,781,112),"상세 능력치");panel(c,new RectF(533,123,921,215),"능력치");txt(c,"HP "+f.maxHp+"    MP "+f.maxMp,552,172,12,0xffe2d9c6);txt(c,"AC "+f.ac+"    HIT "+f.hit+"    DAM "+f.dam,552,194,12,0xffe2d9c6);
    if(selectedSlot!=null){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(r.equipment().get(selectedSlot));card(c,r,d,null,new RectF(533,228,921,517),selectedSlot,v);if(d!=null)button(c,new RectF(743,476,893,507),"해제");}
  }
  Hit inventoryTouch(float x,float y,RpgProgressionState r){hitItem=null;if(new RectF(896,48,940,83).contains(x,y))return Hit.CLOSE;
    if(details&&y>=228){if(new RectF(898,234,936,268).contains(x,y)){details=false;return Hit.CONSUMED;}if(new RectF(743,476,893,507).contains(x,y))return Hit.ACTION;return Hit.CONSUMED;}
    for(int i=0;i<3;i++)if(new RectF(342+i*70,80,409+i*70,110).contains(x,y)){filter=i;page=0;details=false;return Hit.CONSUMED;}
    List<RpgInventoryPresentation.ItemRow> list=rows(r);int pages=Math.max(1,(list.size()+49)/50);if(y>=463&&y<500){if(x>=344&&x<400)page=Math.max(0,page-1);if(x>=866&&x<924)page=Math.min(pages-1,page+1);return Hit.CONSUMED;}
    for(int i=0;i<50;i++)if(cell(i).contains(x,y)){int n=page*50+i;if(n<list.size()){hitItem=list.get(n).itemId;details=true;return Hit.SELECT;}return Hit.CONSUMED;}
    return Hit.CONSUMED;
  }
  Hit equipmentTouch(float x,float y,RpgProgressionState r){hitItem=null;if(new RectF(476,48,511,84).contains(x,y))return Hit.CLOSE;if(new RectF(533,74,781,112).contains(x,y))return Hit.DETAILS;
    if(new RectF(743,476,893,507).contains(x,y)&&selectedSlot!=null){hitItem=r.equipment().get(selectedSlot);if(hitItem!=null)return Hit.ACTION;}
    for(int i=0;i<SLOTS.length;i++)if(slot(i).contains(x,y)){selectedSlot=SLOTS[i];hitItem=r.equipment().get(selectedSlot);return hitItem==null?Hit.CONSUMED:Hit.SELECT;}return Hit.CONSUMED;
  }
}
