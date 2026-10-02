package com.projectdark.mobile;

import android.graphics.*;
import android.view.MotionEvent;
import java.util.*;

/** Reference-matched two-panel book. Grouping consumes Master; input uses the existing economy/save API. */
final class SkillWindow {
  interface Actions {
    void use(SkillBook.Entry entry);boolean canLearn(SkillBook.Entry entry);void learn(SkillBook.Entry entry);
    SkillAcquisition.Quote quote(SkillBook.Entry entry);long gold();boolean save();float cooldown(String id);
    String requirements(SkillBook.Entry entry);void notice(String text);default void testMode(boolean enabled){}
  }
  private static final String[] JOBS={"공통","전사","도적","무도가","마법사","성직자","전체"};
  private static final int TEXT=0xffdbdbce,MUTED=0xff999789,GOLD=0xffc2a85b,GREEN=0xffa9c99b,RED=0xffdc9b87;
  static final RectF LIST=new RectF(35,45,627,532),DETAIL=new RectF(631,45,928,320),VIEWPORT=new RectF(44,104,610,505);
  static final RectF LEARN=new RectF(646,386,774,414),REGISTER=new RectF(784,386,912,414);
  static final RectF CONDITIONS=new RectF(646,289,912,313),ARCHIVE=new RectF(45,505,179,528),TEST=new RectF(185,505,289,528),OWNED=new RectF(441,505,560,528);
  static final RectF KIND=new RectF(646,348,774,376),DETAIL_TAB=new RectF(784,348,912,376);
  boolean open,magic,learnedOnly,choosingSlot,archive,allKinds=true;
  int flashSlot=-1,messageColor=GREEN,page,detailPage,detailOffset;
  String message="",selectedId,job="전체";
  float scroll;
  private float downY,startScroll;private boolean dragging,scrollGesture;
  private final SkillBook book;private final SkillIconCatalog icons;private final SkillWindowSkin skin;
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  SkillWindow(SkillBook book,SkillIconCatalog icons){this.book=book;this.icons=icons;skin=icons.windowSkin();}
  void close(){open=false;choosingSlot=dragging=scrollGesture=false;}
  private SkillBook.Entry selected(){return book.get(selectedId);}
  private void reset(){scroll=0;page=0;selectedId=null;detailPage=detailOffset=0;choosingSlot=false;message="";}
  List<SkillBook.Entry> rows(){List<SkillBook.Entry> result=new ArrayList<>();for(SkillBook.Entry e:book.entries())if(book.previewable(e.id)&&(!learnedOnly||book.learned(e.id))&&(allKinds||e.magic()==magic)&&(book.testAccess()||archive||e.runtime!=null&&book.jobAllowed(e.id))&&("전체".equals(job)||job.equals(e.job)))result.add(e);return result;}
  static String group(SkillBook.Entry e){
    if("순수1차".equals(e.stage))return "순수승급";
    if("1차승급".equals(e.stage))return "승급";
    if(e.circle.matches("[1-5]"))return e.circle+"서클";
    return "기타";
  }
  private static int groupOrder(String group){switch(group){case "1서클":return 1;case "2서클":return 2;case "3서클":return 3;case "4서클":return 4;case "5서클":return 5;case "승급":return 6;case "순수승급":return 7;default:return 8;}}
  static final class Item {final SkillBook.Entry entry;final RectF rect;Item(SkillBook.Entry e,RectF r){entry=e;rect=r;}}
  List<Item> layout(){List<SkillBook.Entry> sorted=rows();sorted.sort(Comparator.comparingInt(e->groupOrder(group(e))));
    List<Integer> positions=new ArrayList<>();List<SkillBook.Entry> promotion=new ArrayList<>();for(int i=0;i<sorted.size();i++)if("도적".equals(sorted.get(i).job)&&"승급".equals(group(sorted.get(i)))){positions.add(i);promotion.add(sorted.get(i));}promotion.sort(Comparator.comparingInt(e->skin.promotionOrder(e.id)));for(int i=0;i<positions.size();i++)sorted.set(positions.get(i),promotion.get(i));
    List<Item> out=new ArrayList<>();float y=132;String previous="";int column=0;
    for(SkillBook.Entry e:sorted){String group=group(e);if(!group.equals(previous)){if(!previous.isEmpty())y+=94;previous=group;column=0;}else if(column==8){column=0;y+=67;}
      float x=50+column*55;out.add(new Item(e,new RectF(x,y,x+49,y+57)));column++;
    }return out;
  }
  float maxScroll(){List<Item> items=layout();return items.isEmpty()?0:Math.max(0,items.get(items.size()-1).rect.bottom+12-VIEWPORT.bottom);}
  RectF rectFor(String id){for(Item i:layout())if(i.entry.id.equals(id)){RectF r=new RectF(i.rect);r.offset(0,-scroll);return r;}return null;}
  void reveal(String id){for(Item i:layout())if(i.entry.id.equals(id)){scroll=Math.max(0,Math.min(maxScroll(),i.rect.top-155));return;}}
  static RectF bookSlot(int i){float x=646+(i%4)*68,y=441+(i/4)*43;return new RectF(x,y,x+62,y+39);}
  static RectF jobTab(int i){return new RectF(44+i*82,77,126+i*82,104);}
  void showJob(String value){job=value;reset();}
  void draw(Canvas c,Actions a){if(!open)return;
    skin.panel(c,LIST,false);skin.panel(c,DETAIL,true);
    // Every job is directly visible and tappable; no hidden tab pages.
    fill(c,40,77,620,103,0x22000000);
    for(int i=0;i<JOBS.length;i++){RectF r=jobTab(i);boolean active=job.equals(JOBS[i]);center(c,JOBS[i],r.centerX(),94,13,active?TEXT:MUTED);if(active)gradient(c,r.left+5,101,r.right-5,103,0x0073632e,0xffc1a257);}
    scroll=Math.max(0,Math.min(scroll,maxScroll()));List<Item> items=layout();c.save();c.clipRect(VIEWPORT);
    String previous="";for(Item i:items){RectF r=new RectF(i.rect);r.offset(0,-scroll);String g=group(i.entry);if(!g.equals(previous)){label(c,g,45,r.top-16,13,TEXT,false);previous=g;}
      if(r.bottom>=VIEWPORT.top&&r.top<=VIEWPORT.bottom)drawCard(c,i.entry,r,a);
    }
    if(items.isEmpty())center(c,learnedOnly?"아직 배운 스킬이 없습니다":"해당 분류의 스킬이 없습니다",325,260,14,TEXT);
    c.restore();skin.draw(c,"scroll-track",new RectF(612,114,625,508));skin.draw(c,"scroll-up",new RectF(612,104,625,116));skin.draw(c,"scroll-down",new RectF(612,505,625,519));
    float maximum=maxScroll(),thumb=maximum==0?387:Math.max(40,387*VIEWPORT.height()/(VIEWPORT.height()+maximum));float ty=118+(maximum==0?0:(scroll/maximum)*(387-thumb));skin.draw(c,"scroll-thumb",new RectF(612,ty,625,ty+thumb));
    fitted(c,archive?"사용 가능 목록":"전체 자료 보기",50,522,128,10,GOLD,false);
    fitted(c,book.testAccess()?"테스트 ON":"테스트 OFF",190,522,98,10,MUTED,false);
    fitted(c,learnedOnly?"배운 스킬":"전체 스킬",446,522,106,10,MUTED,false);right(c,number(a.gold())+" G",607,522,11,GOLD);
    SkillBook.Entry e=selected();if(e==null){center(c,"스킬을 선택하세요",780,170,15,TEXT);}else drawDetail(c,e,a);
    // Mobile actions sit below the original detail window; the two source panels keep their structure.
    button(c,KIND,allKinds?"기술 · 마법":magic?"마법":"기술",true);button(c,DETAIL_TAB,detailPage==0?"습득 조건":"설명",e!=null);
    boolean supported=e!=null&&(e.runtime!=null||book.testAccess());SkillAcquisition.Quote q=e==null?null:a.quote(e);
    button(c,LEARN,e==null?"습득":book.learned(e.id)?"습득 완료":!supported?"습득 불가":q.gold==0?"습득 · 무료":"습득 · "+number(q.gold)+" G",e!=null&&a.canLearn(e));
    button(c,REGISTER,choosingSlot?"등록 취소":"퀵슬롯 등록",supported&&book.learned(e.id));
    fitted(c,message.isEmpty()?choosingSlot?"등록할 슬롯을 선택하세요":"퀵슬롯":message,646,433,266,10,message.isEmpty()?MUTED:messageColor,false);
    for(int i=0;i<8;i++){RectF r=bookSlot(i);fill(c,r.left,r.top,r.right,r.bottom,0xe51c1915);outline(c,r,choosingSlot||i==flashSlot?GOLD:0xff554c3c,1);label(c,""+(i+1),r.left+4,r.top+11,9,MUTED,false);SkillBook.Entry sl=book.get(book.slot(i));if(sl!=null){icon(c,sl,new RectF(r.left+18,r.top+3,r.left+49,r.top+34));float cd=a.cooldown(sl.id);if(cd>0){fill(c,r.left,r.top,r.right,r.bottom,0xb5000000);center(c,seconds(cd),r.centerX(),r.centerY()+4,11,TEXT);}}}
  }
  private void drawCard(Canvas c,SkillBook.Entry e,RectF r,Actions a){fill(c,r.left,r.top,r.right,r.bottom,0xff12100c);skin.draw(c,e.id.equals(selectedId)?"selected-frame":"icon-frame",r);
    RectF art=new RectF(r.left+5,r.top+4,r.right-5,r.top+43);icon(c,e,art);
    if(!book.learned(e.id))fill(c,art.left,art.top,art.right,art.bottom,0xa8000000);
    center(c,book.learned(e.id)?"1/1":"0/1",r.centerX(),r.bottom-5,11,book.learned(e.id)?GOLD:0xff63563b);
    float cd=a.cooldown(e.id);if(cd>0){fill(c,art.left,art.top,art.right,art.bottom,0xb9000000);center(c,seconds(cd),art.centerX(),art.centerY()+4,12,TEXT);}
  }
  private void drawDetail(Canvas c,SkillBook.Entry e,Actions a){icon(c,e,new RectF(647,87,687,123));fitted(c,e.name,695,109,213,14,TEXT,true);
    SkillDef d=e.runtime;org.json.JSONObject captured=skin.capturedDetails(e.id);String cd=d==null?(captured==null?"—":captured.optInt("cooldownSeconds")+" 초"):"SK_공통_001".equals(e.id)?"무기 기준":seconds(d.cooldown)+" 초";
    label(c,"대기시간",646,141,13,TEXT,false);right(c,cd,773,141,13,TEXT);label(c,"시전시간",783,141,13,TEXT,false);right(c,captured==null?"—":captured.optInt("castSeconds")+" 초",911,141,13,TEXT);
    label(c,"숙련도",646,156,13,TEXT,false);right(c,book.proficiency(e.id)+"/100",773,156,13,TEXT);label(c,"소모MP",783,156,13,TEXT,false);right(c,d==null?(captured==null?"—":""+captured.optInt("mpCost")):SkillAbilityCatalog.resources(SkillAbilityCatalog.get(e.id)),911,156,13,TEXT);
    if(detailPage==0){SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(e.id);String summary=ability!=null&&ability.supported()?e.effect+"\n"+ability.description:captured==null?book.summary(e.id):captured.optString("description");List<String> lines=wrap(summary.isEmpty()?e.effect:summary,265,11);c.save();c.clipRect(646,164,912,219);for(int i=0;i<Math.min(4,lines.size());i++)label(c,lines.get(i),646,175+i*13,11,TEXT,false);c.restore();
      fill(c,646,225,912,254,0xff1e1d19);skin.draw(c,"command-box",new RectF(646,225,912,254));center(c,"퀵슬롯에 등록해 주세요.",779,244,12,MUTED);
      String status=book.learned(e.id)?"보유하고 있는 스킬입니다.":a.canLearn(e)?"습득할 수 있는 스킬입니다.":e.runtime==null&&!book.testAccess()?"현재 습득할 수 없는 스킬입니다.":"습득 조건을 확인해 주세요.";
      if(d==null&&captured!=null)label(c,"자료 정보 · 전투 효과 준비 중",646,280,9,MUTED,false);
      fittedCenter(c,status,779,303,270,12,book.learned(e.id)||a.canLearn(e)?GOLD:RED);
    }else{
      SkillAcquisition.Quote q=a.quote(e);label(c,"요구 스탯 / 현재 스탯",646,175,10,MUTED,false);for(int i=0;i<5;i++){float x=646+(i%3)*88,y=193+(i/3)*19;label(c,SkillAcquisition.STATS[i]+" "+q.required[i]+" / "+q.current[i],x,y,10,q.current[i]>=q.required[i]?GREEN:RED,false);}
      label(c,"습득 비용",646,238,11,TEXT,false);right(c,q.learned?"추가 결제 없음":number(q.gold)+" G",911,238,11,GOLD);int line=0;
      for(SkillAcquisition.Material m:q.materials){if(line>=2)break;fitted(c,m.name+" "+m.required+"개 · 보유 "+m.owned,646,255+line++*14,265,11,m.owned>=m.required?GREEN:RED,false);}
      String status=q.learned?"습득 완료":q.blockers.isEmpty()?"습득 가능":q.blockers.get(0);fittedCenter(c,status,779,303,265,11,q.learned||q.canLearn?GREEN:RED);
    }
  }
  private void icon(Canvas c,SkillBook.Entry e,RectF r){if(!icons.drawSquare(c,e.id,r)){fill(c,r.left,r.top,r.right,r.bottom,0xff201d16);center(c,"—",r.centerX(),r.centerY()+4,13,MUTED);}}
  boolean motion(int action,float x,float y){if(!open)return false;
    if(action==MotionEvent.ACTION_DOWN){downY=y;startScroll=scroll;scrollGesture=VIEWPORT.contains(x,y)||new RectF(610,104,627,520).contains(x,y);dragging=false;return false;}
    if(action==MotionEvent.ACTION_MOVE&&scrollGesture){if(Math.abs(y-downY)>5)dragging=true;if(dragging)scroll=Math.max(0,Math.min(maxScroll(),startScroll+downY-y));return true;}
    if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){boolean handled=scrollGesture;scrollGesture=dragging=false;return handled;}return true;
  }
  boolean touch(float x,float y,Actions a){if(!open)return false;
    if(new RectF(593,45,627,77).contains(x,y)||new RectF(900,45,928,77).contains(x,y)){close();return true;}
    if(y>=77&&y<104&&x>=44&&x<618){job=JOBS[(int)((x-44)/82)];reset();return true;}
    if(ARCHIVE.contains(x,y)){archive=!archive;reset();return true;}if(TEST.contains(x,y)){a.testMode(!book.testAccess());reset();return true;}if(OWNED.contains(x,y)){learnedOnly=!learnedOnly;reset();return true;}
    if(KIND.contains(x,y)){if(allKinds){allKinds=false;magic=false;}else if(!magic)magic=true;else allKinds=true;reset();return true;}
    if(DETAIL_TAB.contains(x,y)||CONDITIONS.contains(x,y)){detailPage=1-detailPage;return true;}
    if(x>=610&&x<=627&&y>=104&&y<=520){if(y<=118)scroll=Math.max(0,scroll-67);else if(y>=501)scroll=Math.min(maxScroll(),scroll+67);else scroll=maxScroll()*Math.max(0,Math.min(1,(y-118)/387));return true;}
    if(VIEWPORT.contains(x,y)){for(Item i:layout()){RectF r=new RectF(i.rect);r.offset(0,-scroll);if(r.contains(x,y)){selectedId=i.entry.id;detailPage=detailOffset=0;choosingSlot=false;message="";break;}}return true;}
    if(LEARN.contains(x,y)){SkillBook.Entry e=selected();if(e!=null&&!book.learned(e.id)){if(!a.canLearn(e)){message=a.requirements(e);messageColor=RED;detailPage=1;return true;}a.learn(e);messageColor=book.learned(e.id)?GREEN:RED;message=book.learned(e.id)?e.name+" 습득 · 퀵슬롯에 등록하세요":"저장 실패 · 습득 비용을 돌려드렸습니다";}return true;}
    if(REGISTER.contains(x,y)){SkillBook.Entry e=selected();if(e!=null&&(e.runtime!=null||book.testAccess())&&book.learned(e.id)){choosingSlot=!choosingSlot;message="";}else a.notice("먼저 사용 가능한 스킬을 습득하세요");return true;}
    for(int i=0;i<8;i++)if(bookSlot(i).contains(x,y)){if(choosingSlot&&selected()!=null){org.json.JSONObject before=book.snapshot();String[] beforeTest=book.testSlots();if(selectedId.equals(book.slot(i)))book.clearSlot(i);else if(!book.assign(i,selectedId))return true;if(!a.save()){book.restore(before);book.restoreTestSlots(beforeTest);message="저장 실패 · 등록 취소";messageColor=RED;a.notice(message);return true;}choosingSlot=false;flashSlot=i;messageColor=GREEN;message="슬롯 "+(i+1)+" · "+(book.slot(i)==null?"등록 해제":"등록 완료 · 창을 닫고 사용하세요");a.notice(message);}else{SkillBook.Entry e=book.get(book.slot(i));if(e!=null){selectedId=e.id;detailPage=0;}}return true;}
    return true;
  }
  void drawSlot(Canvas c,RectF r,int index,Actions a){SkillBook.Entry e=book.get(book.slot(index));if(e==null)return;icon(c,e,new RectF(r.left+4,r.top+3,r.right-4,r.bottom-11));fittedCenter(c,e.name,r.centerX(),r.bottom-2,r.width()-4,8,TEXT);float cd=a.cooldown(e.id);if(cd>0){fill(c,r.left,r.top,r.right,r.bottom,0xb9000000);center(c,seconds(cd),r.centerX(),r.centerY()+4,12,TEXT);}}
  private static String seconds(float f){return f==Math.round(f)?""+Math.round(f):String.format(Locale.ROOT,"%.1f",f);}
  private static String number(long n){return String.format(Locale.ROOT,"%,d",n);}
  private void fill(Canvas c,float l,float t,float r,float b,int color){p.setStyle(Paint.Style.FILL);p.setShader(null);p.setColor(color);c.drawRect(l,t,r,b,p);}
  private void label(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(size);p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setColor(color);c.drawText(s,x,y,p);}
  private void center(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);label(c,s,x-p.measureText(s)/2,y,size,color,false);}
  private void right(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);label(c,s,x-p.measureText(s),y,size,color,false);}
  private void fitted(Canvas c,String s,float x,float y,float width,float size,int color,boolean bold){p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setTextSize(size);if(p.measureText(s)>width){int n=p.breakText(s,true,width-p.measureText("…"),null);s=s.substring(0,Math.max(0,n))+"…";}label(c,s,x,y,size,color,bold);}
  private void fittedCenter(Canvas c,String s,float x,float y,float width,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);if(p.measureText(s)>width){int n=p.breakText(s,true,width-p.measureText("…"),null);s=s.substring(0,Math.max(0,n))+"…";}center(c,s,x,y,size,color);}
  private void gradient(Canvas c,float l,float t,float r,float b,int top,int bottom){p.setShader(new LinearGradient(l,t,l,b,top,bottom,Shader.TileMode.CLAMP));c.drawRect(l,t,r,b,p);p.setShader(null);}
  private void outline(Canvas c,RectF r,int color,float width){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setColor(color);c.drawRect(r,p);p.setStyle(Paint.Style.FILL);}
  private void button(Canvas c,RectF r,String s,boolean active){fill(c,r.left,r.top,r.right,r.bottom,0xef242018);outline(c,r,active?0xff82714a:0xff484235,1);center(c,s,r.centerX(),r.centerY()+4,11,active?GOLD:MUTED);}
  private List<String> wrap(String s,float width,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);List<String> out=new ArrayList<>();for(String paragraph:s.split("\n",-1)){String rest=paragraph;do{int n=p.breakText(rest,true,width,null);if(n==0&&!rest.isEmpty())n=1;if(n<rest.length()){int space=rest.lastIndexOf(' ',n);if(space>0)n=space;}out.add(rest.substring(0,n).trim());rest=rest.substring(n).trim();}while(!rest.isEmpty());}return out;}
}
