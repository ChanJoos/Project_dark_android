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
  static final String[] SLOTS={"모자","귀걸이","목걸이","갑옷","무기","장갑",RpgProgressionState.RIGHT_GLOVE_SLOT,"반지",RpgProgressionState.RIGHT_RING_SLOT,"신발","각반","벨트","방패","장식",RpgProgressionState.SECOND_ORNAMENT_SLOT};
  // Coordinates in the user's 522x556 reference crop. Cosmetic-only source cells omitted.
  static final float[][] POS={{268,54},{355,72},{109,72},{109,144},{109,216},{40,288},{427,288},{109,288},{355,288},{268,360},{109,360},{355,360},{40,360},{355,144},{427,144}};
  static final float EQUIP_X=224,EQUIP_Y=35,EQUIP_SCALE=.85f;
  static RectF equipmentClose(){return new RectF(630,36,667,72);}
  static RectF equipmentStats(){return new RectF(224+430*.85f,35+54*.85f,224+493*.85f,35+107*.85f);}
  static int[] equipmentValues(RpgProgressionState r){FinalStats f=r.finalStats();return new int[]{f.str,f.intel,f.wis,f.con,f.dex,f.ac};}
  final Map<String,Bitmap> images=new HashMap<>();
  final Map<String,Bitmap> equipmentImages=new HashMap<>();
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  final RpgInventoryPresentation presentation=new RpgInventoryPresentation();
  int page,filter,effectPage,effectPages=1;boolean details,effectDetails;String selectedSlot,hitItem,hitTargetSlot,detailItemId,pickedTargetSlot;
  ItemWindow(Context context){try{JSONObject m=new JSONObject(new String(PresentationAssetBytes.read(context,"item-window/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("crops");Iterator<String> keys=m.keys();while(keys.hasNext()){String k=keys.next();Bitmap b=BitmapFactory.decodeStream(context.getAssets().open(m.getJSONObject(k).getString("path")));if(b==null)throw new IllegalStateException(k);images.put(k,b);}}catch(Exception e){throw new IllegalStateException("Item window skin unavailable",e);}for(String key:new String[]{"panel","slot"})try{Bitmap b=BitmapFactory.decodeStream(context.getAssets().open("equipment-window-v175/"+key+".png"));if(b==null)throw new IllegalStateException(key);equipmentImages.put(key,b);}catch(Exception e){throw new IllegalStateException("Equipment reference skin unavailable",e);}}
  static RectF cell(int i){float x=344+(i%10)*58,y=118+(i/10)*58;return new RectF(x,y,x+58,y+58);}
  static RectF slot(int i){float x=EQUIP_X+POS[i][0]*EQUIP_SCALE,y=EQUIP_Y+POS[i][1]*EQUIP_SCALE;return new RectF(x,y,x+69*EQUIP_SCALE,y+69*EQUIP_SCALE);}
  List<RpgInventoryPresentation.ItemRow> rows(RpgProgressionState r){List<RpgInventoryPresentation.ItemRow> out=new ArrayList<>();for(RpgInventoryPresentation.ItemRow row:presentation.inventoryRows(r))if(filter==0||filter==1&&row.equipSlot!=null&&!row.equipSlot.isEmpty()||filter==2&&(r.isConsumable(row.itemId)||r.isRecall(row.itemId)))out.add(row);return out;}
  void image(Canvas c,String key,RectF r){if(key.equals("all")||key.equals("gear")||key.equals("use")){UiTheme.glyph(c,key.equals("all")?"bag":key.equals("gear")?"gear":"potion",r.centerX(),r.centerY(),8,UiTheme.GOLD);}else UiTheme.slot(c,r,false,false);}
  void txt(Canvas c,String s,float x,float y,float size,int color){UiTheme.text(c,s,x,y,size,UiTheme.ink(color),size>=13);}
  void fit(Canvas c,String s,float x,float y,float w,float size,int color){UiTheme.fit(c,s,x,y,w,size,UiTheme.ink(color),size>=13);}
  void border(Canvas c,RectF r,int color){UiTheme.surface(c,r,0,UiTheme.ink(color),7);}
  void panel(Canvas c,RectF r,String title){UiTheme.panel(c,r,title);}
  void button(Canvas c,RectF r,String s){UiTheme.button(c,r,s,true,s.equals("장착")||s.equals("사용")||s.equals("해제"));}
  void inventory(Canvas c,RpgProgressionState r,String selected,Visuals v){
    UiTheme.scrim(c);panel(c,new RectF(328,48,940,520),"인벤토리");UiTheme.close(c,916,66);
    List<RpgInventoryPresentation.ItemRow> rows=rows(r);int pages=Math.max(1,(rows.size()+49)/50);page=Math.max(0,Math.min(page,pages-1));
    String[] keys={"all","gear","use"},labels={"전체","장비","소비"};
    for(int i=0;i<3;i++){
      RectF tab=new RectF(342+i*70,82,409+i*70,110);UiTheme.surface(c,tab,filter==i?UiTheme.RAISED:UiTheme.BG,filter==i?UiTheme.GOLD:0,0);
      image(c,keys[i],new RectF(348+i*70,87,365+i*70,105));UiTheme.text(c,labels[i],372+i*70,101,11,filter==i?UiTheme.TEXT:UiTheme.MUTED,true);
      if(filter==i)UiTheme.line(c,tab.left+5,109,tab.right-5,109,UiTheme.ACCENT);
    }
    UiTheme.right(c,"보유 아이템  "+presentation.inventoryRows(r).size()+"종",922,101,11,UiTheme.MUTED,false);
    for(int i=0;i<50;i++){RectF b=cell(i);UiTheme.slot(c,b,false,false);if(i<10)txt(c,i==9?"0":String.valueOf(i+1),b.left+3,b.top+9,8,UiTheme.GOLD);int index=page*50+i;if(index>=rows.size())continue;RpgInventoryPresentation.ItemRow row=rows.get(index);v.icon(c,row,b.left+7,b.top+10,43);if(row.quantity>1){String q=row.quantity>9999?"9999+":""+row.quantity;p.setTextSize(11);txt(c,q,b.right-4-p.measureText(q),b.bottom-5,11,0xfffaf5e3);}if(row.equipped)txt(c,"✓",b.right-14,b.top+14,13,0xffe2bd6a);if(row.itemId.equals(selected)&&details)border(c,b,0xffe9c367);}
    UiTheme.line(c,344,428,924,428,UiTheme.LINE);
    UiTheme.text(c,"소지품",347,446,11,UiTheme.MUTED,false);
    UiTheme.right(c,String.format(java.util.Locale.ROOT,"%,d",r.gold())+" G",921,447,13,UiTheme.GOLD,true);
    button(c,new RectF(344,463,400,499),"‹");button(c,new RectF(866,463,923,499),"›");
    UiTheme.center(c,String.format(java.util.Locale.ROOT,"%02d  /  %02d",page+1,pages),634,485,12,UiTheme.TEXT,true);
    UiTheme.center(c,new String[]{"전체 아이템","장비 목록","소비 아이템"}[filter],634,504,10,UiTheme.MUTED,false);
    RpgInventoryPresentation.ItemRow selectedRow=find(r,selected);
    if(details&&selectedRow!=null){
      detailItemId=selected;RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(selected);
      String target=targetSlot(r,d);RpgProgressionState.ItemDefinition old=target==null?null:r.equippedDefinition(target);
      if(d.equippable())comparisonCards(c,r,d,target,v);
      card(c,r,d,old,new RectF(479,228,936,517),d.equippable()?"장비 살펴보기":"아이템 정보",v);
      String label=actionLabel(r,d,selectedRow.equipped);boolean enabled=!RpgProgressionState.pairedSlot(d.equipSlot)||target!=null;
      UiTheme.button(c,new RectF(743,476,893,507),label,enabled,true);
      if(!enabled)UiTheme.text(c,"교체할 장비를 골라주세요",495,495,11,UiTheme.MUTED,false);
      button(c,new RectF(898,234,929,264),"×");
    }
  }
  static RectF compareCell(int i,int count){float width=count==2?193:394;float x=56+i*209;return new RectF(x,383,x+width,447);}
  String targetSlot(RpgProgressionState r,RpgProgressionState.ItemDefinition d){
    if(d==null||!d.equippable())return null;if(!RpgProgressionState.pairedSlot(d.equipSlot))return d.equipSlot;
    String second=RpgProgressionState.secondSlot(d.equipSlot);
    if(d.equipSlot.equals(pickedTargetSlot)||second.equals(pickedTargetSlot))return pickedTargetSlot;
    if(r.equipment().get(d.equipSlot)==null)return d.equipSlot;
    if(r.equipment().get(second)==null)return second;
    return null;
  }
  String actionLabel(RpgProgressionState r,RpgProgressionState.ItemDefinition d,boolean equipped){
    if(!RpgProgressionState.pairedSlot(d.equipSlot))return ItemEffects.actionLabel(d,r,equipped);
    String target=targetSlot(r,d);if(target==null)return "장비 선택";
    String current=r.equipment().get(target);return current==null?"장착":d.itemId.equals(current)?"해제":"교체";
  }
  void comparisonCards(Canvas c,RpgProgressionState r,RpgProgressionState.ItemDefinition d,String target,Visuals v){
    panel(c,new RectF(40,348,467,464),"착용 중인 장비");int count=RpgProgressionState.pairedSlot(d.equipSlot)?2:1;
    for(int i=0;i<count;i++){
      String slot=i==0?d.equipSlot:RpgProgressionState.secondSlot(d.equipSlot);RectF b=compareCell(i,count);
      UiTheme.surface(c,b,slot.equals(target)?UiTheme.RAISED:UiTheme.BG,slot.equals(target)?UiTheme.GOLD:UiTheme.LINE,3);
      RpgProgressionState.ItemDefinition old=r.equippedDefinition(slot);String label=d.equipSlot+(count==2?(i==0?" Ⅰ":" Ⅱ"):"");
      UiTheme.text(c,label,b.left+9,b.top+15,10,UiTheme.MUTED,false);
      if(old==null){UiTheme.equipmentGlyph(c,d.equipSlot,b.left+28,b.top+40,12,UiTheme.LINE);UiTheme.text(c,"빈 칸",b.left+57,b.top+40,12,UiTheme.MUTED,false);}
      else{RpgInventoryPresentation.ItemRow row=find(r,old.itemId);if(row!=null)v.icon(c,row,b.left+7,b.top+18,43);
        UiTheme.fit(c,RpgInventoryPresentation.displayName(old.name),b.left+57,b.top+34,b.width()-65,12,UiTheme.TEXT,true);
        String stats=old.statModifiers.containsKey("MinATK")?"공격 "+mod(old,"MinATK")+"~"+mod(old,"MaxATK"):
          old.statModifiers.containsKey("HP")?"HP "+mod(old,"HP")+"  MP "+mod(old,"MP"):"AC "+mod(old,"AC")+"  DAM "+mod(old,"DAM");
        UiTheme.fit(c,stats,b.left+57,b.top+51,b.width()-65,10,UiTheme.MUTED,false);
      }
    }
  }

  RpgInventoryPresentation.ItemRow find(RpgProgressionState r,String id){for(RpgInventoryPresentation.ItemRow row:presentation.inventoryRows(r))if(row.itemId.equals(id))return row;return null;}
  void card(Canvas c,RpgProgressionState r,RpgProgressionState.ItemDefinition d,RpgProgressionState.ItemDefinition old,RectF b,String title,Visuals v){
    panel(c,b,title);if(d==null){txt(c,"장착한 장비가 없습니다",b.left+26,b.top+95,13,0xffb7b2a4);return;}
    RpgInventoryPresentation.ItemRow row=find(r,d.itemId);if(row!=null)v.icon(c,row,b.left+15,b.top+42,48);fit(c,RpgInventoryPresentation.displayName(d.name),b.left+76,b.top+56,b.width()-115,15,0xffe4dccb);
    String job=!d.jobRestrictionResolved?"직업 조건 확인 중":d.allowedJobCodes.isEmpty()?"공통":String.join(" · ",d.allowedJobCodes);fit(c,job,b.left+76,b.top+76,b.width()-92,11,0xffd5bd7c);
    txt(c,d.requiredLevel==null?"레벨 조건 확인 중":"Lv."+d.requiredLevel+" 이상 장착 가능",b.left+76,b.top+96,12,0xffd0ccbf);if(row!=null)txt(c,r.equipmentSandbox()?"테스트: 레벨·직업 조건 면제":presentation.requirementLabel(row),b.left+76,b.top+115,11,0xffc4b79c);
    button(c,new RectF(b.left+14,b.top+126,b.left+130,b.top+150),"능력치");button(c,new RectF(b.left+142,b.top+126,b.left+268,b.top+150),"효과 · 설명");
    RectF opts=new RectF(b.left+14,b.top+156,b.right-14,b.bottom-48);border(c,opts,UiTheme.LINE);if(effectDetails||!d.equippable()){drawEffects(c,d,opts);return;}int n=0;
    String[] keys={"ATTACK_S","ATTACK_L","AC","HIT","DAM","MAGIC_DEFENSE","HP","MP","STR","INT","WIS","CON","DEX","REGEN"};
    for(String key:keys){
      boolean range=key.startsWith("ATTACK_");String minKey=key.equals("ATTACK_S")?"MinATK":"LMinATK",maxKey=key.equals("ATTACK_S")?"MaxATK":"LMaxATK";
      int value=range?mod(d,maxKey):mod(d,key),prior=range?mod(old,maxKey):mod(old,key);
      if(value==0&&prior==0)continue;
      float yy=opts.top+14+(n%7)*10,xx=opts.left+(n/7)*opts.width()/2;
      String label=key.equals("ATTACK_S")?"무기공격(S)":key.equals("ATTACK_L")?"무기공격(L)":key.equals("MAGIC_DEFENSE")?"마법방어":key.equals("REGEN")?"재생력":key;
      String shown=range?(d.statModifiers.containsKey(maxKey)?mod(d,minKey)+"~"+value:"미확인"):""+value;
      if(key.equals("MAGIC_DEFENSE"))shown+="%";
      txt(c,label,xx+8,yy,10,0xffdcd8cd);txt(c,shown,xx+82,yy,10,0xffebe5d8);
      int delta=value-prior;if(old!=null&&delta!=0){boolean good=key.equals("AC")?delta<0:delta>0;txt(c,(good?"▲ ":"▼ ")+Math.abs(delta),xx+148,yy,9,good?0xff6dda59:0xffed8074);}n++;
    }
    if(n==0)txt(c,d.attackElement!=null||d.defenseElement!=null?"속성 장비":d.equippable()?"수치 자료 확인 중":"추가 능력치 없음",opts.left+9,opts.top+24,12,0xffaaa696);
    if(d.attackElement!=null||d.defenseElement!=null)txt(c,(d.attackElement!=null?"공격 속성 "+d.attackElement:"방어 속성 "+d.defenseElement),opts.left+9,opts.bottom-7,10,0xffd5bd7c);
  }
  void drawEffects(Canvas c,RpgProgressionState.ItemDefinition d,RectF b){
    List<String> lines=new ArrayList<>();p.setTypeface(UiTheme.font(false));p.setTextSize(11);
    for(String para:ItemEffects.description(d)){String line="";for(int i=0;i<para.length();i++){String next=line+para.charAt(i);if(p.measureText(next)>b.width()-20&&!line.isEmpty()){lines.add(line);line=""+para.charAt(i);}else line=next;}if(!line.isEmpty())lines.add(line);}
    int pages=Math.max(1,(lines.size()+4)/5),pg=Math.min(effectPage,pages-1);effectPages=pages;effectPage=pg;
    for(int i=0;i<5&&pg*5+i<lines.size();i++)txt(c,lines.get(pg*5+i),b.left+8,b.top+14+i*12,11,UiTheme.TEXT);
    if(pages>1){txt(c,"‹",b.left+12,b.bottom-3,12,UiTheme.GOLD);txt(c,(pg+1)+" / "+pages,b.centerX()-20,b.bottom-3,10,UiTheme.MUTED);txt(c,"›",b.right-22,b.bottom-3,12,UiTheme.GOLD);}
  }
  boolean detailTouch(float x,float y,float left){
    if(y>=354&&y<=378&&x>=left+14&&x<=left+268){effectDetails=x>=left+142;effectPage=0;return true;}
    if(y>=450&&y<469){if(x>=left+14&&x<left+75){effectPage=Math.max(0,effectPage-1);return true;}if(x>left+300&&x<=left+440){effectPage=Math.min(effectPages-1,effectPage+1);return true;}}
    return false;
  }
  static int mod(RpgProgressionState.ItemDefinition d,String k){return d==null?0:d.statModifiers.getOrDefault(k,0);}
  void equipment(Canvas c,RpgProgressionState r,String selected,Visuals v){
    UiTheme.scrim(c);c.save();c.translate(EQUIP_X,EQUIP_Y);c.scale(EQUIP_SCALE,EQUIP_SCALE);
    p.setFilterBitmap(false);p.setAlpha(255);c.drawBitmap(equipmentImages.get("panel"),0,0,p);
    // Actual paper doll, not the character captured in the reference screenshot.
    v.actor(c,261,310);
    for(int i=0;i<SLOTS.length;i++){
      float x=POS[i][0],y=POS[i][1];RectF b=new RectF(x,y,x+70,y+70);
      c.drawBitmap(equipmentImages.get("slot"),null,b,p);
      String id=r.equipment().get(SLOTS[i]);RpgInventoryPresentation.ItemRow row=find(r,id);
      if(row!=null)v.icon(c,row,x+7,y+7,56);
      else UiTheme.equipmentGlyph(c,RpgProgressionState.baseSlot(SLOTS[i]),x+35,y+35,18,0xff64533c);
      if(SLOTS[i].equals(selectedSlot)){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xffe6bf77);c.drawRect(x+2,y+2,x+68,y+68,p);p.setStyle(Paint.Style.FILL);}
    }
    // Preserve original stat ordering: STR/INT, WIS/CON, DEX/AC.
    int[] values=equipmentValues(r);int[][] at={{147,504},{147,540},{295,504},{295,540},{449,504},{449,540}};
    p.setTypeface(Typeface.create(Typeface.SERIF,Typeface.NORMAL));p.setTextSize(24);p.setTextAlign(Paint.Align.RIGHT);p.setColor(0xfff0e6c9);
    for(int i=0;i<6;i++){float width=p.measureText(String.valueOf(values[i]));p.setTextScaleX(width>37?37/width:1);c.drawText(String.valueOf(values[i]),at[i][0],at[i][1],p);p.setTextScaleX(1);}
    p.setTextAlign(Paint.Align.LEFT);
    // The reference's top-right character button opens the full live stat page.
    // Original character/stat button pixels are retained in the skin.
    c.restore();UiTheme.close(c,649,54);
    if(selectedSlot!=null){
      RectF b=new RectF(690,155,947,514);panel(c,b,selectedSlot.replace("갑옷","옷"));
      RpgProgressionState.ItemDefinition d=r.equippedDefinition(selectedSlot);
      if(d==null){UiTheme.center(c,"빈 장비 슬롯",818,238,14,UiTheme.MUTED,false);}
      else{
        RpgInventoryPresentation.ItemRow row=find(r,d.itemId);if(row!=null)v.icon(c,row,789,196,58);
        UiTheme.fit(c,RpgInventoryPresentation.displayName(d.name),706,279,224,14,UiTheme.TEXT,true);
        UiTheme.fit(c,presentation.requirementLabel(row),706,303,224,11,UiTheme.MUTED,false);
        int line=0;for(Map.Entry<String,Integer> m:d.statModifiers.entrySet()){if(line==8)break;UiTheme.text(c,m.getKey(),707,329+line*16,11,UiTheme.MUTED,false);UiTheme.right(c,(m.getValue()>0?"+":"")+m.getValue(),927,329+line*16,12,UiTheme.TEXT,false);line++;}
        button(c,new RectF(743,476,893,507),"해제");
      }
    }
  }
  Hit inventoryTouch(float x,float y,RpgProgressionState r){hitItem=null;hitTargetSlot=null;if(new RectF(896,48,940,83).contains(x,y))return Hit.CLOSE;
    if(details&&y>=228){
      RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(detailItemId);
      if(d!=null&&RpgProgressionState.pairedSlot(d.equipSlot)){for(int i=0;i<2;i++)if(compareCell(i,2).contains(x,y)){pickedTargetSlot=i==0?d.equipSlot:RpgProgressionState.secondSlot(d.equipSlot);return Hit.CONSUMED;}}
      if(detailTouch(x,y,479))return Hit.CONSUMED;if(new RectF(898,234,936,268).contains(x,y)){details=false;return Hit.CONSUMED;}if(y>=476&&y<=507&&x>=743&&x<=893){
      if(d!=null&&RpgProgressionState.pairedSlot(d.equipSlot)){String target=targetSlot(r,d);if(target==null)return Hit.CONSUMED;hitTargetSlot=target.equals(RpgProgressionState.secondSlot(d.equipSlot))?"RIGHT":"LEFT";}return Hit.ACTION;
    }return Hit.CONSUMED;}
    for(int i=0;i<3;i++)if(new RectF(342+i*70,80,409+i*70,110).contains(x,y)){filter=i;page=0;details=false;return Hit.CONSUMED;}
    List<RpgInventoryPresentation.ItemRow> list=rows(r);int pages=Math.max(1,(list.size()+49)/50);if(y>=463&&y<500){if(x>=344&&x<400)page=Math.max(0,page-1);if(x>=866&&x<924)page=Math.min(pages-1,page+1);return Hit.CONSUMED;}
    for(int i=0;i<50;i++)if(cell(i).contains(x,y)){int n=page*50+i;if(n<list.size()){hitItem=list.get(n).itemId;detailItemId=hitItem;pickedTargetSlot=null;details=true;effectDetails=false;effectPage=0;return Hit.SELECT;}return Hit.CONSUMED;}
    return Hit.CONSUMED;
  }
  Hit equipmentTouch(float x,float y,RpgProgressionState r){hitItem=null;
    if(equipmentClose().contains(x,y))return Hit.CLOSE;
    if(equipmentStats().contains(x,y)||new RectF(224,35+475*.85f,224+522*.85f,35+556*.85f).contains(x,y))return Hit.DETAILS;
    if(new RectF(743,476,893,507).contains(x,y)&&selectedSlot!=null){hitItem=r.equipment().get(selectedSlot);if(hitItem!=null)return Hit.ACTION;}
    for(int i=0;i<SLOTS.length;i++)if(slot(i).contains(x,y)){selectedSlot=SLOTS[i];effectDetails=false;effectPage=0;hitItem=r.equipment().get(selectedSlot);return hitItem==null?Hit.CONSUMED:Hit.SELECT;}
    return Hit.CONSUMED;
  }
}
