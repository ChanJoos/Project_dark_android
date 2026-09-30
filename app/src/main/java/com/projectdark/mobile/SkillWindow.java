package com.projectdark.mobile;

import android.graphics.*;
import java.util.*;

/** Game skill book: consistent icon/name cards, concrete learning quote and explicit slot editing. */
final class SkillWindow {
  interface Actions {
    void use(SkillBook.Entry entry);boolean canLearn(SkillBook.Entry entry);void learn(SkillBook.Entry entry);
    SkillAcquisition.Quote quote(SkillBook.Entry entry);long gold();boolean save();float cooldown(String id);
    String requirements(SkillBook.Entry entry);void notice(String text);
  }
  static final int PAGE_SIZE=12;
  private static final String[] JOBS={"전체","공통","전사","도적","무도가","마법사","성직자"};
  private static final int TEXT=0xffe9e0d0,MUTED=0xffa79e8c,GOLD=0xffe2bd76,GREEN=0xffa9c99b,RED=0xffdc9b87;
  boolean open,magic,learnedOnly,choosingSlot,archive;
  int flashSlot=-1,messageColor=GREEN;String message="";
  int page,detailPage,detailOffset;
  String selectedId,job="전체";
  private final SkillBook book;private final SkillIconCatalog icons;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF bounds=new RectF(88,28,872,514);
  SkillWindow(SkillBook book,SkillIconCatalog icons){this.book=book;this.icons=icons;}
  void close(){open=false;choosingSlot=false;}
  private SkillBook.Entry selected(){return book.get(selectedId);}
  private List<SkillBook.Entry> rows(){List<SkillBook.Entry> out=new ArrayList<>();for(SkillBook.Entry e:book.list(magic,learnedOnly))if(!"검증용".equals(e.job)&&(archive||e.runtime!=null)&&("전체".equals(job)||job.equals(e.job)))out.add(e);return out;}
  private void reset(){page=0;selectedId=null;detailPage=detailOffset=0;choosingSlot=false;message="";}
  static RectF cell(int i){float x=112+(i%4)*98,y=154+(i/4)*78;return new RectF(x,y,x+92,y+76);}
  private RectF bookSlot(int i){return new RectF(112+i*92,469,198+i*92,505);}
  void draw(Canvas c,Actions a){
    if(!open)return;
    fill(c,0,0,960,540,0x92000000);panel(c,bounds,0xff201f1b,0xff8e7850);
    gradient(c,96,36,864,70,0xff3f382b,0xff25241e);
    label(c,"스킬",112,61,23,GOLD,true);button(c,new RectF(232,37,354,64),archive?"사용 가능 목록":"전체 자료 보기",true);
    right(c,"보유 골드  "+number(a.gold())+" G",814,58,12,TEXT);button(c,new RectF(834,37,859,64),"×",true);
    tab(c,new RectF(112,80,298,109),"기술",!magic);tab(c,new RectF(306,80,498,109),"마법",magic);
    tab(c,new RectF(520,80,677,109),"전체 스킬",!learnedOnly);tab(c,new RectF(685,80,848,109),"배운 스킬",learnedOnly);
    for(int i=0;i<JOBS.length;i++)tab(c,new RectF(112+i*105,118,211+i*105,143),JOBS[i],job.equals(JOBS[i]));
    List<SkillBook.Entry> list=rows();int pages=Math.max(1,(list.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(page,pages-1));
    for(int i=0;i<PAGE_SIZE&&page*PAGE_SIZE+i<list.size();i++)drawCard(c,list.get(page*PAGE_SIZE+i),cell(i),a);
    if(list.isEmpty()){
      center(c,learnedOnly?"아직 배운 스킬이 없습니다":"해당 분류의 스킬이 없습니다",305,252,15,TEXT);
      center(c,learnedOnly?"전체 스킬에서 원하는 스킬을 선택하세요":"다른 분류를 선택하세요",305,277,11,MUTED);
    }
    panel(c,new RectF(520,154,848,396),0xff191c19,0xff514b3d);
    SkillBook.Entry e=selected();
    if(e==null){center(c,"스킬을 선택하세요",684,256,17,TEXT);center(c,"효과와 습득 비용을 확인할 수 있습니다",684,282,11,MUTED);}
    else drawDetail(c,e,a);
    button(c,new RectF(112,410,150,440),"‹",page>0);button(c,new RectF(460,410,498,440),"›",page+1<pages);
    center(c,(page+1)+" / "+pages+"  ·  "+list.size()+"개",305,430,12,MUTED);
    SkillAcquisition.Quote q=e==null?null:a.quote(e);
    boolean supported=e!=null&&e.runtime!=null;
    String learn=e==null?"습득":!supported?"습득 불가":book.learned(e.id)?"습득 완료":q.gold==0?"습득 · 무료":"습득 · "+number(q.gold)+" G";
    button(c,new RectF(520,410,677,440),learn,e!=null&&a.canLearn(e));
    button(c,new RectF(685,410,848,440),choosingSlot?"등록 취소":"퀵슬롯 등록",supported&&book.learned(e.id));
    fitted(c,message.isEmpty()?choosingSlot?"등록할 슬롯을 선택하세요":"퀵슬롯":message,112,460,736,11,message.isEmpty()?MUTED:messageColor,false);
    for(int i=0;i<8;i++){
      RectF r=bookSlot(i);panel(c,r,0xff282822,choosingSlot||i==flashSlot?0xffb69258:0xff514b3d);label(c,""+(i+1),r.left+5,r.top+12,9,MUTED,false);
      SkillBook.Entry sl=book.get(book.slot(i));if(sl==null)center(c,"—",r.centerX(),r.centerY()+5,14,0xff5e6255);
      else{icon(c,sl,new RectF(r.left+28,r.top+3,r.left+58,r.top+33));float cd=a.cooldown(sl.id);if(cd>0){fill(c,r.left,r.top,r.right,r.bottom,0xb5000000);center(c,String.format(Locale.ROOT,"%.1f",cd),r.centerX(),r.centerY()+5,13,TEXT);}}
    }
  }
  private void drawCard(Canvas c,SkillBook.Entry e,RectF r,Actions a){
    boolean active=e.id.equals(selectedId);panel(c,r,active?0xff423a2a:0xff282822,active?0xffd2b278:0xff45453b);
    RectF art=new RectF(r.centerX()-20,r.top+6,r.centerX()+20,r.top+46);icon(c,e,art);
    List<String> names=nameLines(e.name,r.width()-8,11);
    for(int i=0;i<Math.min(2,names.size());i++)center(c,names.get(i),r.centerX(),r.top+60+i*13,11,TEXT);
    if(book.learned(e.id)){label(c,"✓",r.right-13,r.top+14,11,GREEN,true);}
    float remaining=a.cooldown(e.id);if(remaining>0){fill(c,art.left,art.top,art.right,art.bottom,0xb9000000);center(c,String.format(Locale.ROOT,"%.1f",remaining),art.centerX(),art.centerY()+4,12,TEXT);}
  }
  private void drawDetail(Canvas c,SkillBook.Entry e,Actions a){
    SkillAcquisition.Quote q=a.quote(e);icon(c,e,new RectF(536,166,584,214));
    fitted(c,e.name,598,186,236,18,GOLD,true);label(c,e.job+" · "+e.kind,598,207,11,MUTED,false);
    String status=e.runtime==null?"자료 미리보기 · 현재 습득 불가":q.learned?"습득 완료" : q.canLearn?"습득 가능":q.blockers.isEmpty()?"습득 불가":q.blockers.get(0);
    if(q.learned){List<String> slots=new ArrayList<>();for(int i=0;i<8;i++)if(e.id.equals(book.slot(i)))slots.add(""+(i+1));if(!slots.isEmpty())status+=" · 슬롯 "+String.join(", ",slots);}
    fitted(c,status,536,228,296,11,q.learned||q.canLearn?GREEN:RED,false);
    tab(c,new RectF(536,238,678,262),"설명",detailPage==0);tab(c,new RectF(686,238,832,262),"습득 조건",detailPage==1);
    if(detailPage==0){
      List<String> lines=wrap(book.summary(e.id),296,12);c.save();c.clipRect(536,270,832,330);for(int i=0;i<Math.min(4,lines.size());i++)label(c,lines.get(i),536,283+i*15,12,TEXT,false);c.restore();
      SkillDef d=e.runtime;boolean basic="SK_공통_001".equals(e.id);String resource=d==null?"—":""+d.mpCost;
      String cd=d==null?"—":basic?"무기 기준":String.format(Locale.ROOT,"%.1f초",d.cooldown);
      String value=d==null?"—":basic?"무기 기준":d.targetPolicy==SkillDef.TargetPolicy.SELF?""+(d.damage+q.current[2]):""+d.damage;
      metric(c,536,"소모 MP",resource);metric(c,636,"재사용",cd);metric(c,736,d!=null&&d.targetPolicy==SkillDef.TargetPolicy.SELF?"회복량":"기본 위력",value);
      if(d==null)label(c,"습득·결제는 제공하지 않습니다",536,391,10,MUTED,false);
      else{String target=d.targetPolicy==SkillDef.TargetPolicy.SELF?"자신":d.range<=48?"근접한 적":"원거리 적";float remaining=a.cooldown(e.id);label(c,"대상  "+target,536,391,10,MUTED,false);if(remaining>0)right(c,String.format(Locale.ROOT,"%.1f초 후 사용 가능",remaining),832,391,10,GOLD);}
    }else{
      label(c,"요구 스탯 / 현재 스탯",536,280,10,MUTED,false);
      for(int i=0;i<5;i++){float x=536+(i%3)*100,y=286+(i/3)*27;fill(c,x,y,x+96,y+23,0xff262b24);label(c,SkillAcquisition.STATS[i],x+6,y+16,10,MUTED,false);right(c,q.required[i]+" / "+q.current[i],x+90,y+16,11,q.learned?MUTED:q.current[i]>=q.required[i]?GREEN:RED);}
      label(c,q.learned?"습득 완료":"습득 비용",536,352,11,TEXT,true);right(c,q.learned?"추가 결제 없음":number(q.gold)+" G · 보유 "+number(q.ownedGold)+" G",832,352,11,q.learned||q.ownedGold>=q.gold?GREEN:RED);
      if(q.learned)label(c,"추가 아이템 필요 없음",536,377,11,MUTED,false);
      else if(q.materials.isEmpty())label(c,"필요 아이템 없음",536,377,11,MUTED,false);
      else{int line=0;for(SkillAcquisition.Material m:q.materials){fitted(c,m.name+" "+m.required+"개",536,371+line*17,180,11,TEXT,false);right(c,"보유 "+m.owned+"개",832,371+line*17,11,m.owned>=m.required?GREEN:RED);line++;}}
    }
  }
  private void metric(Canvas c,float x,String name,String value){fill(c,x,338,x+96,378,0xff282c25);label(c,name,x+8,352,9,MUTED,false);label(c,value,x+8,371,13,TEXT,true);}
  private void icon(Canvas c,SkillBook.Entry e,RectF r){fill(c,r.left,r.top,r.right,r.bottom,0xff24231d);if(!icons.draw(c,e.id,r)){outline(c,r,0xff595749,1);center(c,"—",r.centerX(),r.centerY()+5,15,MUTED);}}
  boolean touch(float x,float y,Actions a){
    if(!open)return false;if(new RectF(824,28,872,72).contains(x,y)){close();return true;}if(!bounds.contains(x,y))return true;
    if(new RectF(232,37,354,64).contains(x,y)){archive=!archive;reset();return true;}
    if(y>=80&&y<=109){if(x>=112&&x<=498){magic=x>=306;reset();}else if(x>=520&&x<=848){learnedOnly=x>=685;reset();}return true;}
    if(y>=118&&y<=143&&x>=112&&x<848){int i=(int)((x-112)/105);if(i<JOBS.length){job=JOBS[i];reset();}return true;}
    for(int i=0;i<PAGE_SIZE;i++)if(cell(i).contains(x,y)){List<SkillBook.Entry> list=rows();int n=page*PAGE_SIZE+i;if(n<list.size()){selectedId=list.get(n).id;detailPage=detailOffset=0;choosingSlot=false;message="";}return true;}
    if(y>=238&&y<=262&&x>=536&&x<=832){detailPage=x>=686?1:0;return true;}
    if(y>=410&&y<=440){if(x>=112&&x<=150){page=Math.max(0,page-1);selectedId=null;choosingSlot=false;}else if(x>=460&&x<=498){page=Math.min(page+1,Math.max(0,(rows().size()-1)/PAGE_SIZE));selectedId=null;choosingSlot=false;}else if(x>=520&&x<=677){if(selected()!=null&&!book.learned(selectedId)){if(!a.canLearn(selected())){message=a.requirements(selected());messageColor=RED;detailPage=1;return true;}a.learn(selected());messageColor=book.learned(selectedId)?GREEN:RED;message=book.learned(selectedId)?selected().name+" 습득 · 퀵슬롯에 등록하세요":"저장 실패 · 습득 비용을 돌려드렸습니다";}}else if(x>=685&&x<=848){if(selected()!=null&&selected().runtime!=null&&book.learned(selectedId)){choosingSlot=!choosingSlot;message="";}else a.notice("먼저 사용 가능한 스킬을 습득하세요");}return true;}
    for(int i=0;i<8;i++)if(bookSlot(i).contains(x,y)){
      if(choosingSlot&&selected()!=null){org.json.JSONObject before=book.snapshot();if(selectedId.equals(book.slot(i)))book.clearSlot(i);else if(!book.assign(i,selectedId))return true;if(!a.save()){book.restore(before);message="저장 실패 · 등록 취소";messageColor=RED;a.notice(message);return true;}choosingSlot=false;flashSlot=i;messageColor=GREEN;message="슬롯 "+(i+1)+" · "+(book.slot(i)==null?"등록 해제":"등록 완료 · 창을 닫고 사용하세요");a.notice(message);}
      else{SkillBook.Entry e=book.get(book.slot(i));if(e!=null){selectedId=e.id;detailPage=detailOffset=0;}}return true;
    }
    return true;
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
