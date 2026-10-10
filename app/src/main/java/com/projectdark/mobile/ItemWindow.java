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
  static final String[] SLOTS={"귀걸이","목걸이","갑옷","모자","반지","무기","방패","장갑","벨트","각반","신발",RpgProgressionState.RIGHT_GLOVE_SLOT,RpgProgressionState.RIGHT_RING_SLOT};
  // Balanced paper doll: two equal rails, centered head and paired lower slots.
  static final float[][] POS={{242,134},{438,134},{242,198},{340,94},{242,390},{242,262},{438,262},{242,326},{438,390},{306,390},{374,390},{438,326},{438,198}};
  final Map<String,Bitmap> images=new HashMap<>();
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  final RpgInventoryPresentation presentation=new RpgInventoryPresentation();
  int page,filter,effectPage,effectPages=1;boolean details,effectDetails;String selectedSlot,hitItem,hitTargetSlot;
  ItemWindow(Context context){try{JSONObject m=new JSONObject(new String(PresentationAssetBytes.read(context,"item-window/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("crops");Iterator<String> keys=m.keys();while(keys.hasNext()){String k=keys.next();Bitmap b=BitmapFactory.decodeStream(context.getAssets().open(m.getJSONObject(k).getString("path")));if(b==null)throw new IllegalStateException(k);images.put(k,b);}}catch(Exception e){throw new IllegalStateException("Item window skin unavailable",e);}}
  static RectF cell(int i){float x=344+(i%10)*58,y=118+(i/10)*58;return new RectF(x,y,x+58,y+58);}
  static RectF slot(int i){return new RectF(POS[i][0],POS[i][1],POS[i][0]+55,POS[i][1]+55);}
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
    RpgInventoryPresentation.ItemRow selectedRow=find(r,selected);if(details&&selectedRow!=null){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(selected);if(d.equippable()){card(c,r,r.equippedDefinition(d.equipSlot),null,new RectF(40,228,467,517),"현재 장착",v);card(c,r,d,r.equippedDefinition(d.equipSlot),new RectF(479,228,936,517),"선택 장비",v);}else card(c,r,d,null,new RectF(479,228,936,517),"아이템 정보",v);if(RpgProgressionState.pairedSlot(d.equipSlot)){
      button(c,new RectF(649,476,765,507),d.itemId.equals(r.equipment().get(d.equipSlot))?"왼쪽 해제":"왼쪽 장착");
      button(c,new RectF(771,476,892,507),d.itemId.equals(r.equipment().get(RpgProgressionState.secondSlot(d.equipSlot)))?"오른쪽 해제":"오른쪽 장착");
    }else button(c,new RectF(743,476,893,507),ItemEffects.actionLabel(d,r,selectedRow.equipped));button(c,new RectF(898,234,929,264),"×");}
  }
  RpgInventoryPresentation.ItemRow find(RpgProgressionState r,String id){for(RpgInventoryPresentation.ItemRow row:presentation.inventoryRows(r))if(row.itemId.equals(id))return row;return null;}
  void card(Canvas c,RpgProgressionState r,RpgProgressionState.ItemDefinition d,RpgProgressionState.ItemDefinition old,RectF b,String title,Visuals v){
    panel(c,b,title);if(d==null){txt(c,"장착한 장비가 없습니다",b.left+26,b.top+95,13,0xffb7b2a4);return;}
    RpgInventoryPresentation.ItemRow row=find(r,d.itemId);if(row!=null)v.icon(c,row,b.left+15,b.top+42,48);fit(c,RpgInventoryPresentation.displayName(d.name),b.left+76,b.top+56,b.width()-115,15,0xffe4dccb);
    String job=!d.jobRestrictionResolved?"직업 조건 확인 중":d.allowedJobCodes.isEmpty()?"공통":String.join(" · ",d.allowedJobCodes);fit(c,job,b.left+76,b.top+76,b.width()-92,11,0xffd5bd7c);
    txt(c,d.requiredLevel==null?"레벨 조건 확인 중":"Lv."+d.requiredLevel+" 이상 장착 가능",b.left+76,b.top+96,12,0xffd0ccbf);if(row!=null)txt(c,r.equipmentSandbox()?"테스트: 레벨·직업 조건 면제":presentation.requirementLabel(row),b.left+76,b.top+115,11,0xffc4b79c);
    button(c,new RectF(b.left+14,b.top+126,b.left+130,b.top+150),"능력치");button(c,new RectF(b.left+142,b.top+126,b.left+268,b.top+150),"효과 · 설명");
    RectF opts=new RectF(b.left+14,b.top+156,b.right-14,b.bottom-48);border(c,opts,UiTheme.LINE);if(effectDetails||!d.equippable()){drawEffects(c,d,opts);return;}int n=0;
    String[] keys={"MinATK","MaxATK","AC","HIT","DAM","MAGIC_DEFENSE","HP","MP","STR","INT","WIS","CON","DEX","REGEN"};
    for(String key:keys){int value=mod(d,key),prior=mod(old,key);if(value==0&&prior==0)continue;float yy=opts.top+14+(n%7)*10,xx=opts.left+(n/7)*opts.width()/2;txt(c,key.equals("MinATK")?"최소 공격":key.equals("MaxATK")?"최대 공격":key.equals("MAGIC_DEFENSE")?"마법방어":key.equals("REGEN")?"재생력":key,xx+8,yy,10,0xffdcd8cd);txt(c,""+value,xx+81,yy,11,0xffebe5d8);int delta=value-prior;if(old!=null&&delta!=0){boolean good=key.equals("AC")?delta<0:delta>0;txt(c,(good?"▲ ":"▼ ")+Math.abs(delta),xx+115,yy,10,good?0xff6dda59:0xffed8074);}n++;}if(n==0)txt(c,d.attackElement!=null||d.defenseElement!=null?"속성 장비":d.equippable()?"수치 자료 확인 중":"추가 능력치 없음",opts.left+9,opts.top+24,12,0xffaaa696);
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
    UiTheme.scrim(c);panel(c,new RectF(224,48,511,519),"장비");UiTheme.close(c,493,66);
    // Draw the centered actor stage before the item cells; it never covers a slot.
    UiTheme.surface(c,new RectF(311,168,424,337),UiTheme.SURFACE,0,14);v.actor(c,367.5f,315);
    UiTheme.center(c,"평민 · Lv."+r.normalLevel(),367.5f,359,11,UiTheme.TEXT,true);
    for(int i=0;i<SLOTS.length;i++){RectF b=slot(i);String id=r.equipment().get(SLOTS[i]);RpgInventoryPresentation.ItemRow row=find(r,id);boolean active=SLOTS[i].equals(selectedSlot);UiTheme.slot(c,b,active,false);if(row!=null){v.icon(c,row,b.left+5,b.top+5,45);}else UiTheme.equipmentGlyph(c,RpgProgressionState.baseSlot(SLOTS[i]),b.centerX(),b.centerY(),17,active?UiTheme.ACCENT:0xffa58a5c);if(RpgProgressionState.pairedSlot(RpgProgressionState.baseSlot(SLOTS[i])))UiTheme.text(c,SLOTS[i].contains("오른")?"우":"좌",b.left+3,b.top+10,8,UiTheme.MUTED,false);}
    // Independent current stats are read from the established FinalStats authority.
    FinalStats f=r.finalStats();String[] keys={"STR","INT","WIS","CON","DEX"};int[] total={f.str,f.intel,f.wis,f.con,f.dex},base={r.str(),r.intel(),r.wis(),r.con(),r.dex()};
    UiTheme.line(c,242,450,493,450,UiTheme.LINE);
    for(int i=0;i<5;i++){float x=i<3?242:381,y=469+(i<3?i:i-3)*18;UiTheme.text(c,keys[i],x,y,10,UiTheme.MUTED,true);int bonus=total[i]-base[i];UiTheme.right(c,String.valueOf(total[i]),x+69,y,12,UiTheme.TEXT,true);if(bonus!=0)UiTheme.right(c,(bonus>0?"+":"")+bonus,x+111,y,10,UiTheme.ACCENT,true);}
    // One continuous information sheet, with aligned readouts and contextual details.
    panel(c,new RectF(533,48,921,519),"장비 정보");
    button(c,new RectF(549,84,797,112),"상세 능력치");UiTheme.glyph(c,"stats",867,93,13,UiTheme.ACCENT);
    String[] summary={"HP","MP","AC","HIT","DAM"};int[] values={f.maxHp,f.maxMp,f.ac,f.hit,f.dam};
    for(int i=0;i<5;i++){float x=553+i*69;UiTheme.center(c,summary[i],x+25,145,10,UiTheme.MUTED,true);UiTheme.center(c,String.valueOf(values[i]),x+25,174,20,UiTheme.TEXT,true);}
    UiTheme.line(c,549,195,905,195,UiTheme.LINE);
    if(selectedSlot!=null){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(r.equipment().get(selectedSlot));card(c,r,d,null,new RectF(533,228,921,517),selectedSlot,v);if(d!=null)button(c,new RectF(743,476,893,507),"해제");}
    else{UiTheme.equipmentGlyph(c,"갑옷",727,305,28,0xff9b7a4f);UiTheme.center(c,"장비를 선택하세요",727,368,16,UiTheme.TEXT,true);UiTheme.center(c,"각 부위의 능력치와 착용 정보를 확인합니다.",727,393,11,UiTheme.MUTED,false);}
  }
  Hit inventoryTouch(float x,float y,RpgProgressionState r){hitItem=null;hitTargetSlot=null;if(new RectF(896,48,940,83).contains(x,y))return Hit.CLOSE;
    if(details&&y>=228){if(detailTouch(x,y,479))return Hit.CONSUMED;if(new RectF(898,234,936,268).contains(x,y)){details=false;return Hit.CONSUMED;}if(y>=476&&y<=507&&x>=649&&x<=892){
      // Selection is held by the controller; derive target from the displayed item below.
      hitTargetSlot=x<771?"LEFT":"RIGHT";return Hit.ACTION;
    }return Hit.CONSUMED;}
    for(int i=0;i<3;i++)if(new RectF(342+i*70,80,409+i*70,110).contains(x,y)){filter=i;page=0;details=false;return Hit.CONSUMED;}
    List<RpgInventoryPresentation.ItemRow> list=rows(r);int pages=Math.max(1,(list.size()+49)/50);if(y>=463&&y<500){if(x>=344&&x<400)page=Math.max(0,page-1);if(x>=866&&x<924)page=Math.min(pages-1,page+1);return Hit.CONSUMED;}
    for(int i=0;i<50;i++)if(cell(i).contains(x,y)){int n=page*50+i;if(n<list.size()){hitItem=list.get(n).itemId;details=true;effectDetails=false;effectPage=0;return Hit.SELECT;}return Hit.CONSUMED;}
    return Hit.CONSUMED;
  }
  Hit equipmentTouch(float x,float y,RpgProgressionState r){hitItem=null;if(selectedSlot!=null&&detailTouch(x,y,533))return Hit.CONSUMED;if(new RectF(476,48,511,84).contains(x,y))return Hit.CLOSE;if(new RectF(549,74,797,112).contains(x,y))return Hit.DETAILS;
    if(new RectF(743,476,893,507).contains(x,y)&&selectedSlot!=null){hitItem=r.equipment().get(selectedSlot);if(hitItem!=null)return Hit.ACTION;}
    for(int i=0;i<SLOTS.length;i++)if(slot(i).contains(x,y)){selectedSlot=SLOTS[i];effectDetails=false;effectPage=0;hitItem=r.equipment().get(selectedSlot);return hitItem==null?Hit.CONSUMED:Hit.SELECT;}return Hit.CONSUMED;
  }
}
