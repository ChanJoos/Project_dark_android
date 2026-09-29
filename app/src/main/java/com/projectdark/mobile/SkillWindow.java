package com.projectdark.mobile;
import android.graphics.*;
import java.util.*;
/** Mobile skill flow: playable default, explicit archive, large targets and in-window acknowledgement. */
final class SkillWindow {
  interface Actions {
    void use(SkillBook.Entry entry);boolean canLearn(SkillBook.Entry entry);void learn(SkillBook.Entry entry);
    SkillAcquisition.Quote quote(SkillBook.Entry entry);long gold();boolean save();float cooldown(String id);
    String requirements(SkillBook.Entry entry);void notice(String text);
  }
  static final int PAGE_SIZE=6;
  private static final String[] JOBS={"전체","공통","전사","도적","무도가","마법사","성직자"};
  private static final int TEXT=0xfff1e9d8,MUTED=0xffb2aa97,GOLD=0xffedc884,GREEN=0xffb7dca0,RED=0xffefaa91;
  boolean open,magic,learnedOnly,availableOnly,choosingSlot,archive,jobMenu;
  int page,detailPage,detailOffset,flashSlot=-1,messageColor=GREEN;String selectedId,job="전체",message="";
  private final SkillBook book;private final SkillIconCatalog icons;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF bounds=new RectF(40,16,920,534);
  SkillWindow(SkillBook b,SkillIconCatalog i){book=b;icons=i;}
  void close(){open=false;choosingSlot=jobMenu=false;}
  private SkillBook.Entry selected(){return book.get(selectedId);}
  private List<SkillBook.Entry> rows(Actions a){List<SkillBook.Entry> out=new ArrayList<>();for(SkillBook.Entry e:book.list(magic,learnedOnly))if(!"검증용".equals(e.job)&&(archive||e.runtime!=null)&&("전체".equals(job)||job.equals(e.job))&&(!availableOnly||a.canLearn(e)))out.add(e);return out;}
  private void reset(){page=0;selectedId=null;detailPage=detailOffset=0;choosingSlot=false;message="";}
  static RectF cell(int i){float x=64+(i%2)*200,y=174+(i/2)*82;return new RectF(x,y,x+194,y+78);}
  private RectF bookSlot(int i){return new RectF(64+i*104,480,164+i*104,526);}
  private String slotNumbers(String id){List<String> n=new ArrayList<>();for(int i=0;i<8;i++)if(id.equals(book.slot(i)))n.add(""+(i+1));return String.join(",",n);}
  void draw(Canvas c,Actions a){
    if(!open)return;fill(c,0,0,960,540,0xb0000000);panel(c,bounds,0xff20221d,0xff9a8259);
    gradient(c,48,24,912,72,0xff453d2e,0xff282b23);label(c,"스킬",64,57,26,GOLD,true);label(c,archive?"전체 자료 · 미구현 포함":"기술과 마법",132,55,14,MUTED,false);
    right(c,number(a.gold())+" G",634,55,17,TEXT);button(c,new RectF(652,30,806,70),archive?"사용 가능 목록":"전체 자료 보기",true);button(c,new RectF(850,30,896,70),"×",true);
    tab(c,new RectF(64,80,254,120),"기술",!magic);tab(c,new RectF(264,80,458,120),"마법",magic);
    tab(c,new RectF(482,80,614,120),"전체",!learnedOnly&&!availableOnly);tab(c,new RectF(622,80,756,120),"습득 가능",availableOnly);tab(c,new RectF(764,80,896,120),"배운 스킬",learnedOnly);
    button(c,new RectF(64,128,254,166),"분류  "+job+" ▾",true);
    List<SkillBook.Entry> list=rows(a);int pages=Math.max(1,(list.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(page,pages-1));
    button(c,new RectF(264,128,306,166),"‹",page>0);center(c,(page+1)+" / "+pages,361,153,14,MUTED);button(c,new RectF(416,128,458,166),"›",page+1<pages);
    for(int i=0;i<PAGE_SIZE&&page*PAGE_SIZE+i<list.size();i++)card(c,list.get(page*PAGE_SIZE+i),cell(i),a);
    if(list.isEmpty()){center(c,learnedOnly?"아직 배운 스킬이 없습니다":availableOnly?"지금 습득할 스킬이 없습니다":"해당 분류의 스킬이 없습니다",261,270,17,TEXT);center(c,"전체 목록에서 조건을 확인하세요",261,300,14,MUTED);}
    panel(c,new RectF(482,128,896,428),0xff171e19,0xff536049);
    SkillBook.Entry e=selected();if(e==null){center(c,"스킬을 선택하세요",689,262,22,TEXT);center(c,"효과와 습득 비용을 확인할 수 있습니다",689,292,14,MUTED);}else detail(c,e,a);
    boolean supported=e!=null&&e.runtime!=null&&!"검증용".equals(e.job);SkillAcquisition.Quote q=e==null?null:a.quote(e);
    String learn=e==null?"습득":!supported?"습득 불가":q.learned?"습득 완료":q.gold==0?"무료 습득":"습득 · "+number(q.gold)+" G";
    button(c,new RectF(482,438,682,474),learn,e!=null&&a.canLearn(e));button(c,new RectF(690,438,896,474),choosingSlot?"등록 취소":"퀵슬롯 등록",supported&&q.learned);
    fitted(c,message.isEmpty()?choosingSlot?"등록할 슬롯을 선택하세요":"퀵슬롯 · 8칸":message,64,458,394,14,message.isEmpty()?MUTED:messageColor,false);
    for(int i=0;i<8;i++){RectF r=bookSlot(i);panel(c,r,0xff2b3025,choosingSlot||i==flashSlot?GOLD:0xff59624c);label(c,""+(i+1),r.left+6,r.top+17,12,MUTED,false);SkillBook.Entry sl=book.get(book.slot(i));if(sl==null)center(c,"+",r.centerX(),r.centerY()+6,19,0xff67715c);else{icon(c,sl,new RectF(r.left+31,r.top+4,r.left+69,r.top+42));float cd=a.cooldown(sl.id);if(cd>0){fill(c,r.left,r.top,r.right,r.bottom,0xb5000000);center(c,String.format(Locale.ROOT,"%.1f",cd),r.centerX(),r.centerY()+6,17,TEXT);}}}
    if(jobMenu){panel(c,new RectF(64,168,458,448),0xff242b20,GOLD);for(int i=0;i<JOBS.length;i++)tab(c,new RectF(68,170+i*39,454,208+i*39),JOBS[i],job.equals(JOBS[i]));}
  }
  private void card(Canvas c,SkillBook.Entry e,RectF r,Actions a){
    SkillAcquisition.Quote q=a.quote(e);boolean sel=e.id.equals(selectedId);panel(c,r,sel?0xff49432e:0xff2a3026,sel?GOLD:0xff505b45);icon(c,e,new RectF(r.left+10,r.top+11,r.left+66,r.top+67));
    List<String> names=nameLines(e.name,112,14);for(int i=0;i<Math.min(2,names.size());i++)label(c,names.get(i),r.left+76,r.top+25+i*17,14,TEXT,true);
    String n=slotNumbers(e.id),state=e.runtime==null?"미구현":q.learned?n.isEmpty()?"습득 완료":"슬롯 "+n:q.canLearn?"습득 가능":"조건 부족";
    fitted(c,state,r.left+76,r.top+64,112,12,q.learned?GOLD:q.canLearn?GREEN:MUTED,false);float cd=a.cooldown(e.id);if(cd>0){fill(c,r.left+10,r.top+11,r.left+66,r.top+67,0xb9000000);center(c,String.format(Locale.ROOT,"%.1f",cd),r.left+38,r.top+44,17,TEXT);}
  }
  private void detail(Canvas c,SkillBook.Entry e,Actions a){
    SkillAcquisition.Quote q=a.quote(e);icon(c,e,new RectF(502,142,558,198));fitted(c,e.name,578,166,298,22,GOLD,true);label(c,e.job+" · "+e.kind,578,191,14,MUTED,false);
    String status=e.runtime==null?"자료 미리보기 · 현재 습득 불가":q.learned?"습득 완료"+ (slotNumbers(e.id).isEmpty()?"":" · 슬롯 "+slotNumbers(e.id)):q.canLearn?"습득 가능":q.blockers.isEmpty()?"습득 불가":q.blockers.get(0);
    fitted(c,status,502,218,374,14,q.canLearn?GREEN:q.learned?GOLD:e.runtime==null?MUTED:RED,false);
    tab(c,new RectF(502,232,686,270),"효과",detailPage==0);tab(c,new RectF(694,232,876,270),"습득 조건",detailPage==1);
    if(detailPage==0){
      List<String> lines=wrap(book.summary(e.id),374,14);c.save();c.clipRect(502,278,876,343);for(int i=0;i<Math.min(3,lines.size());i++)label(c,lines.get(i),502,294+i*19,14,TEXT,false);c.restore();
      SkillDef d=e.runtime;boolean basic="SK_공통_001".equals(e.id);String value=d==null?"—":basic?"무기 기준":d.targetPolicy==SkillDef.TargetPolicy.SELF?""+(d.damage+q.current[2]):""+d.damage;
      if(d!=null)fitted(c,d.targetPolicy==SkillDef.TargetPolicy.SELF?"회복량 = 기본 회복량 + WIS · 최대 HP까지":d.actionClass==SkillDef.ActionClass.TECHNIQUE?"피해는 공격력과 상대 방어에 따라 달라집니다":"피해는 속성 상성과 상대 방어에 따라 달라집니다",502,341,374,12,MUTED,false);
      metric(c,502,"소모 MP",d==null?"—":""+d.mpCost);metric(c,628,"재사용",d==null?"—":basic?"무기 기준":String.format(Locale.ROOT,"%.1f초",d.cooldown));metric(c,754,d!=null&&d.targetPolicy==SkillDef.TargetPolicy.SELF?"회복량":"기본 위력",value);
      float cd=a.cooldown(e.id);if(d==null)label(c,"습득·결제는 제공하지 않습니다",502,415,13,MUTED,false);else{label(c,"대상  "+(d.targetPolicy==SkillDef.TargetPolicy.SELF?"자신":d.range<=48?"근접 적 1명":"원거리 적 1명"),502,415,13,MUTED,false);if(cd>0)right(c,String.format(Locale.ROOT,"%.1f초 후 사용 가능",cd),876,415,13,GOLD);}
    }else{
      label(c,"요구 / 현재 스탯",502,290,12,MUTED,false);for(int i=0;i<5;i++){float x=502+(i%3)*126,y=300+(i/3)*33;fill(c,x,y,x+122,y+29,0xff2a3429);label(c,SkillAcquisition.STATS[i],x+7,y+20,12,MUTED,false);right(c,q.required[i]+" / "+q.current[i],x+115,y+20,14,q.learned?MUTED:q.current[i]>=q.required[i]?GREEN:RED);}
      label(c,q.learned?"습득 완료":"습득 비용",502,389,14,TEXT,true);right(c,q.learned?"추가 결제 없음":number(q.gold)+" G / 보유 "+number(q.ownedGold)+" G",876,389,14,q.learned||q.ownedGold>=q.gold?GREEN:RED);
      if(q.learned)label(c,"추가 아이템 필요 없음",502,414,13,MUTED,false);else if(q.materials.isEmpty())label(c,"필요 아이템 없음",502,414,13,MUTED,false);else for(SkillAcquisition.Material m:q.materials){label(c,m.name+" "+m.required+"개",502,414,13,TEXT,false);right(c,"보유 "+m.owned+"개 · 멀린 상점",876,414,13,m.owned>=m.required?GREEN:RED);}
    }
  }
  private void metric(Canvas c,float x,String name,String value){fill(c,x,350,x+122,396,0xff2c3528);label(c,name,x+9,367,12,MUTED,false);label(c,value,x+9,389,17,TEXT,true);}
  private void icon(Canvas c,SkillBook.Entry e,RectF r){
    fill(c,r.left,r.top,r.right,r.bottom,0xff3d3627);outline(c,r,0xff88744d,1);float art=Math.min(40,r.width()-8);RectF inner=new RectF(r.centerX()-art/2,r.centerY()-art/2,r.centerX()+art/2,r.centerY()+art/2);
    if(icons.draw(c,e.id,inner))return;
    // Original artwork is absent: use an explicit project vector emblem, never another skill's crop.
    c.save();c.translate(r.centerX(),r.centerY());float unit=r.width()/56;c.scale(unit,unit);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);p.setColor(GOLD);p.setStrokeCap(Paint.Cap.ROUND);
    if("SK_무도가_001".equals(e.id)){c.drawRoundRect(new RectF(-12,-9,12,10),4,4,p);for(int i=0;i<4;i++)c.drawLine(-9+i*6,-9,-9+i*6,0,p);c.drawLine(-8,11,-6,18,p);c.drawLine(8,11,6,18,p);}
    else if(e.runtime!=null&&e.runtime.actionClass==SkillDef.ActionClass.TECHNIQUE){Path blade=new Path();blade.moveTo(-11,12);blade.lineTo(13,-16);blade.lineTo(17,-17);blade.lineTo(17,-12);blade.lineTo(-7,16);blade.close();c.drawPath(blade,p);c.drawLine(-15,9,-3,20,p);c.drawLine(-11,16,-16,22,p);if("SK_도적_007".equals(e.id))c.drawLine(-18,-10,-5,-18,p);}
    else if(e.runtime!=null&&e.runtime.targetPolicy==SkillDef.TargetPolicy.SELF){c.drawCircle(0,0,17,p);c.drawLine(-10,0,10,0,p);c.drawLine(0,-10,0,10,p);}else if(e.runtime!=null){Path bolt=new Path();bolt.moveTo(5,-18);bolt.lineTo(-9,2);bolt.lineTo(0,2);bolt.lineTo(-5,18);bolt.lineTo(12,-4);bolt.lineTo(3,-4);bolt.close();c.drawPath(bolt,p);c.drawCircle(0,0,21,p);}else{c.drawRoundRect(new RectF(-14,-16,14,16),3,3,p);c.drawLine(-8,-7,8,-7,p);c.drawLine(-8,0,8,0,p);c.drawLine(-8,7,4,7,p);}p.setStyle(Paint.Style.FILL);c.restore();
  }
  boolean touch(float x,float y,Actions a){
    if(!open)return false;if(new RectF(850,24,908,76).contains(x,y)){close();return true;}if(!bounds.contains(x,y))return true;
    if(jobMenu){if(x>=64&&x<=458&&y>=170&&y<443){int i=(int)((y-170)/39);if(i<JOBS.length){job=JOBS[i];reset();}}jobMenu=false;return true;}
    if(new RectF(652,30,806,70).contains(x,y)){archive=!archive;reset();return true;}
    if(y>=80&&y<=120){if(x>=64&&x<=458){magic=x>=264;reset();}else if(x>=482&&x<=896){learnedOnly=x>=764;availableOnly=x>=622&&x<756;reset();}return true;}
    if(y>=128&&y<=166){if(x>=64&&x<=254)jobMenu=true;else if(x>=264&&x<=306){page=Math.max(0,page-1);selectedId=null;}else if(x>=416&&x<=458){page=Math.min(page+1,Math.max(0,(rows(a).size()-1)/PAGE_SIZE));selectedId=null;}return true;}
    for(int i=0;i<PAGE_SIZE;i++)if(cell(i).contains(x,y)){List<SkillBook.Entry> list=rows(a);int n=page*PAGE_SIZE+i;if(n<list.size()){selectedId=list.get(n).id;detailPage=detailOffset=0;choosingSlot=false;message="";}return true;}
    if(y>=232&&y<=270&&x>=502&&x<=876){detailPage=x>=694?1:0;return true;}
    if(y>=438&&y<=474){if(x>=482&&x<=682&&selected()!=null&&!book.learned(selectedId)){if(!a.canLearn(selected())){message=a.quote(selected()).canLearn?"저장 상태 확인 후 다시 시도하세요":a.requirements(selected());messageColor=RED;detailPage=1;return true;}a.learn(selected());if(book.learned(selectedId)){message=selected().name+" 습득 · 퀵슬롯에 등록하세요";messageColor=GREEN;}else{message="저장 실패 · 습득 비용을 돌려드렸습니다";messageColor=RED;}}else if(x>=690&&x<=896&&selected()!=null){if(book.learned(selectedId)&&selected().runtime!=null){choosingSlot=!choosingSlot;message="";}else message="사용할 스킬을 먼저 습득하세요";}return true;}
    for(int i=0;i<8;i++)if(bookSlot(i).contains(x,y)){
      if(choosingSlot&&selected()!=null){org.json.JSONObject before=book.snapshot();if(selectedId.equals(book.slot(i)))book.clearSlot(i);else if(!book.assign(i,selectedId))return true;if(!a.save()){book.restore(before);message="저장 실패 · 등록 취소";messageColor=RED;a.notice(message);return true;}choosingSlot=false;flashSlot=i;messageColor=GREEN;message="슬롯 "+(i+1)+" · "+(book.slot(i)==null?"등록 해제":selected().name+" 등록");a.notice(message);}
      else{SkillBook.Entry e=book.get(book.slot(i));if(e!=null){selectedId=e.id;detailPage=detailOffset=0;message=e.name+" · 슬롯 "+(i+1);}}return true;
    }return true;
  }
  void drawSlot(Canvas c,RectF r,int index,Actions a){SkillBook.Entry e=book.get(book.slot(index));if(e==null)return;icon(c,e,new RectF(r.left+4,r.top+3,r.right-4,r.bottom-11));fittedCenter(c,e.name,r.centerX(),r.bottom-2,r.width()-4,8,TEXT);float cd=a.cooldown(e.id);if(cd>0){fill(c,r.left,r.top,r.right,r.bottom,0xb9000000);center(c,String.format(Locale.ROOT,"%.1f",cd),r.centerX(),r.centerY()+4,12,TEXT);}}
  private static String number(long n){return String.format(Locale.ROOT,"%,d",n);}
  private void fill(Canvas c,float l,float t,float r,float b,int color){p.setStyle(Paint.Style.FILL);p.setShader(null);p.setColor(color);c.drawRect(l,t,r,b,p);}
  private void label(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(size);p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setColor(color);c.drawText(s,x,y,p);}
  private void center(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);label(c,s,x-p.measureText(s)/2,y,size,color,false);}
  private void right(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);label(c,s,x-p.measureText(s),y,size,color,false);}
  private void fitted(Canvas c,String s,float x,float y,float width,float size,int color,boolean bold){p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setTextSize(size);if(p.measureText(s)>width){int n=p.breakText(s,true,width-p.measureText("…"),null);s=s.substring(0,Math.max(0,n))+"…";}label(c,s,x,y,size,color,bold);}
  private void fittedCenter(Canvas c,String s,float x,float y,float width,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);if(p.measureText(s)>width){int n=p.breakText(s,true,width-p.measureText("…"),null);s=s.substring(0,Math.max(0,n))+"…";}center(c,s,x,y,size,color);}
  private void gradient(Canvas c,float l,float t,float r,float b,int top,int bottom){p.setShader(new LinearGradient(l,t,l,b,top,bottom,Shader.TileMode.CLAMP));c.drawRect(l,t,r,b,p);p.setShader(null);}
  private void outline(Canvas c,RectF r,int color,float width){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setColor(color);c.drawRect(r,p);p.setStyle(Paint.Style.FILL);}
  private void panel(Canvas c,RectF r,int fill,int edge){fill(c,r.left,r.top,r.right,r.bottom,fill);outline(c,r,edge,1);}
  private void tab(Canvas c,RectF r,String s,boolean active){panel(c,r,active?0xff4b422e:0xff282822,active?0xffb99b62:0xff48483d);center(c,s,r.centerX(),r.centerY()+4,12,active?GOLD:MUTED);}
  private void button(Canvas c,RectF r,String s,boolean active){panel(c,r,active?0xff62502f:0xff292a24,active?0xffc6a565:0xff4c4c40);center(c,s,r.centerX(),r.centerY()+4,12,active?0xfff3ddb3:0xff7f8170);}
  private List<String> nameLines(String s,float width,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);if(p.measureText(s)<=width)return Collections.singletonList(s);int split=(s.length()+1)/2;return Arrays.asList(s.substring(0,split),s.substring(split));}
  private List<String> wrap(String s,float width,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);List<String> out=new ArrayList<>();for(String paragraph:s.split("\n",-1)){String rest=paragraph;do{int n=p.breakText(rest,true,width,null);if(n==0&&!rest.isEmpty())n=1;if(n<rest.length()){int space=rest.lastIndexOf(' ',n);if(space>0)n=space;}out.add(rest.substring(0,n).trim());rest=rest.substring(n).trim();}while(!rest.isEmpty());}return out;}
}
